package de.invesdwin.context.client.swing.component.logviewer;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;

import org.apache.commons.io.input.CharSequenceReader;

import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.collections.iterable.ICloseableIterator;
import de.invesdwin.util.concurrent.Executors;
import de.invesdwin.util.concurrent.WrappedScheduledExecutorService;
import de.invesdwin.util.swing.Dialogs;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.util.time.fdate.FDate;
import de.invesdwin.util.time.fdate.FDates;
import de.invesdwin.util.time.fdate.FTimeUnit;

@ThreadSafe
public class LogViewerView extends AView<LogViewerView, JPanel> {

    private static final AtomicLong ACTIVE_LOGS = new AtomicLong();
    @GuardedBy("this.class")
    private static WrappedScheduledExecutorService scheduledExecutor;
    @GuardedBy("this")
    private ILogViewerSource source;
    @GuardedBy("this")
    private FDate logTo;
    @GuardedBy("this")
    private final Set<String> lastLogToMessages = new HashSet<>();
    @GuardedBy("this")
    private boolean background;
    private JEditorPane editor;
    private volatile Future<?> updateFuture;
    private volatile Future<?> initFuture;
    private JScrollPane scrollPane;

    public LogViewerView(final ILogViewerSource source) {
        this.source = source;
        init();
    }

    public static synchronized WrappedScheduledExecutorService getScheduledExecutor() {
        if (scheduledExecutor == null) {
            //reduce cpu load by using max 1 thread
            scheduledExecutor = Executors.newScheduledThreadPool(LogViewerView.class.getSimpleName() + "_SCHEDULER", 1)
                    .withDynamicThreadName(false);
        }
        return scheduledExecutor;
    }

    private static synchronized void maybeCloseScheduledExecutor() {
        if (ACTIVE_LOGS.get() == 0L) {
            if (scheduledExecutor != null) {
                scheduledExecutor.shutdownNow();
                scheduledExecutor = null;
            }
        }
    }

    private void init() {
        lastLogToMessages.clear();
        background = false;
        if (editor != null) {
            initEditor();
        }
        logTo = null;
    }

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    private void initEditor() {
        try {
            editor.getDocument().remove(0, editor.getDocument().getLength());
        } catch (final BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    public ILogViewerSource getSource() {
        return source;
    }

    public void setSource(final ILogViewerSource source) {
        if (updateFuture != null) {
            initFuture = getScheduledExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    synchronized (LogViewerView.this) {
                        LogViewerView.this.source = source;
                        init();
                        update();
                        initFuture = null;
                    }
                }
            });
        } else {
            synchronized (this) {
                LogViewerView.this.source = source;
                init();
            }
        }
    }

    protected Duration getRefreshInterval() {
        return new Duration(1, FTimeUnit.SECONDS);
    }

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    protected JPanel initComponent() {
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 0));

        this.scrollPane = new JScrollPane();
        panel.add(scrollPane, BorderLayout.CENTER);

        this.editor = Dialogs.newHtmlMessagePane();
        scrollPane.setViewportView(editor);
        return panel;
    }

    @Override
    protected void onOpen() {
        Assertions.checkNull(updateFuture);
        ACTIVE_LOGS.incrementAndGet();
        final Duration refreshInterval = getRefreshInterval();
        updateFuture = getScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                update();
            }
        }, 0, refreshInterval.longValue(), refreshInterval.getTimeUnit().timeUnitValue());
    }

    @Override
    protected void onClose() {
        Assertions.checkNotNull(updateFuture);
        ACTIVE_LOGS.decrementAndGet();
        if (initFuture != null) {
            initFuture.cancel(true);
            initFuture = null;
        }
        updateFuture.cancel(true);
        updateFuture = null;
        maybeCloseScheduledExecutor();
    }

    private synchronized void update() {
        if (!getComponent().isShowing()) {
            return;
        }
        onUpdate();
        if (source == null) {
            return;
        }
        final FDate from;
        if (logTo == null) {
            from = FDate.MIN_DATE;
        } else {
            from = logTo;
        }
        tailLog(from);
    }

    private void tailLog(final FDate from) {
        final ICloseableIterable<LogViewerEntry> entries = source.getLogViewerEntries(from, null);
        StringBuilder append = new StringBuilder();
        int count = 0;
        final int caretPositionBefore = editor.getCaretPosition();
        final boolean trailing = isTrailing();
        try (ICloseableIterator<LogViewerEntry> iterator = entries.iterator()) {
            while (true) {
                final LogViewerEntry entry = iterator.next();
                if (logTo == null || logTo.isBefore(entry.getTime())) {
                    lastLogToMessages.clear();
                    logTo = entry.getTime();
                }
                if (lastLogToMessages.add(entry.getMessage())) {
                    final String divStr = entryToDiv(entry);
                    if (append.length() > 0) {
                        append.append("\n");
                    }
                    append.append(divStr);
                    count++;
                    if (count >= 100) {
                        appendMessagesToDocument(append, caretPositionBefore, trailing);
                        append = new StringBuilder();
                        count = 0;
                        try {
                            FTimeUnit.MILLISECONDS.sleep(1);
                        } catch (final InterruptedException e) {
                            //allow interrupt and slow down a bit
                            return;
                        }
                    }
                }
            }
        } catch (final NoSuchElementException e) {
            //end reached
        }
        if (count > 0) {
            appendMessagesToDocument(append, caretPositionBefore, trailing);
            try {
                //allow interrupt and slow down a bit
                FTimeUnit.MILLISECONDS.sleep(1);
            } catch (final InterruptedException e) {
                return;
            }
        }
    }

    protected void onUpdate() {}

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    private void appendMessagesToDocument(final StringBuilder message, final int caretPositionBefore,
            final boolean trailing) {
        if (message.length() > 0) {
            if (editor.getDocument().getLength() > 0) {
                message.insert(0, "\n");
            }
            try {
                //https://stackoverflow.com/questions/12916192/how-to-know-if-a-jscrollbar-has-reached-the-bottom-of-the-jscrollpane
                final EditorKit kit = editor.getEditorKit();
                kit.read(new CharSequenceReader(message), editor.getDocument(), editor.getDocument().getLength());
                if (!trailing) {
                    editor.setCaretPosition(Math.min(caretPositionBefore, editor.getDocument().getLength()));
                } else {
                    editor.setCaretPosition(editor.getDocument().getLength());
                }
            } catch (final BadLocationException e) {
                throw new RuntimeException(e);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isTrailing() {
        if (editor.getDocument().getLength() == 0) {
            return true;
        }
        if (editor.getCaretPosition() >= editor.getDocument().getLength() - 100) {
            return true;
        }
        final JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        final boolean trailing = !scrollBar.isShowing()
                || scrollBar.getValue() >= scrollBar.getMaximum() - scrollBar.getVisibleAmount();
        return trailing;
    }

    private String entryToDiv(final LogViewerEntry entry) {
        final StringBuilder message = new StringBuilder();
        message.append("<div style=\"");
        if (entry.isError()) {
            message.append("color: red;");
        } else {
            message.append("color: black;");
        }
        background = !background;
        if (background) {
            message.append("background-color: #F2F2F2");
        } else {
            message.append("background-color: white");
        }
        message.append("\">");
        message.append("<b>");
        message.append(FDates.toString(entry.getTime()));
        message.append(":</b> ");
        message.append(entry.getMessage().replace("'", "&#x27;"));
        message.append("</div>");
        final String messageStr = message.toString();
        return messageStr;
    }
}
