package de.invesdwin.context.client.swing.component.logviewer;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
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
import javax.swing.text.DefaultCaret;
import javax.swing.text.EditorKit;

import org.apache.commons.io.input.CharSequenceReader;

import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.collections.iterable.ICloseableIterator;
import de.invesdwin.util.concurrent.Executors;
import de.invesdwin.util.concurrent.WrappedScheduledExecutorService;
import de.invesdwin.util.concurrent.reference.MutableReference;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.swing.Dialogs;
import de.invesdwin.util.swing.EventDispatchThreadUtil;
import de.invesdwin.util.swing.listener.ComponentListenerSupport;
import de.invesdwin.util.swing.listener.KeyListenerSupport;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.util.time.date.FDates;
import de.invesdwin.util.time.date.FTimeUnit;
import de.invesdwin.util.time.duration.Duration;

@ThreadSafe
public class LogViewerView extends AView<LogViewerView, JPanel> {

    private static final double DETECT_TRAILING_TOLERANCE_FACTOR = 1.05;
    private static final AtomicLong ACTIVE_LOGS = new AtomicLong();
    @GuardedBy("this.class")
    private static WrappedScheduledExecutorService scheduledExecutor;
    @GuardedBy("this")
    private volatile ILogViewerSource source;
    @GuardedBy("this")
    private FDate logTo;
    @GuardedBy("this")
    private final Set<String> lastLogToMessages = new HashSet<>();
    @GuardedBy("this")
    private boolean background;
    private JEditorPane editor;
    private volatile Future<?> updateFuture;
    private volatile boolean initRequired = true;
    private JScrollPane scrollPane;
    private final Object updatingLock = new Object();
    private boolean prevTrailing = true;
    private boolean prevPrevTrailing = true;

    public LogViewerView(final ILogViewerSource source) {
        this.source = source;
    }

    public static synchronized WrappedScheduledExecutorService getScheduledExecutor() {
        if (scheduledExecutor == null) {
            //reduce cpu load by using max 1 thread
            scheduledExecutor = Executors.newScheduledThreadPool(LogViewerView.class.getSimpleName() + "_SCHEDULER", 1)
                    .setDynamicThreadName(false);
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

    private void init() throws InterruptedException {
        lastLogToMessages.clear();
        background = false;
        if (editor != null) {
            initEditor();
        }
        logTo = null;
        initRequired = false;
    }

    private void initEditor() throws InterruptedException {
        EventDispatchThreadUtil.invokeAndWait(() -> {
            try {
                editor.getDocument().remove(0, editor.getDocument().getLength());
            } catch (final BadLocationException e) {
                throw new RuntimeException(e);
            }
        });

    }

    public ILogViewerSource getSource() {
        //explicitly not synchronized here, else we get deadlocks
        return source;
    }

    public void setSource(final ILogViewerSource source) {
        if (updateFuture != null) {
            updateFuture.cancel(true);
            updateFuture = null;
        }
        LogViewerView.this.source = source;
        initRequired = true;
        initUpdateFuture();
    }

    protected Duration getRefreshInterval() {
        return new Duration(1, FTimeUnit.SECONDS);
    }

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    protected JPanel initComponent() {
        final JPanel panel = new JPanel() {
            @Override
            public void paint(final Graphics g) {
                synchronized (updatingLock) {
                    super.paint(g);
                }
            }
        };
        panel.setDoubleBuffered(true);
        panel.setLayout(new BorderLayout(0, 0));

        this.scrollPane = new JScrollPane();
        panel.add(scrollPane, BorderLayout.CENTER);
        this.editor = Dialogs.newHtmlMessagePane();
        scrollPane.setViewportView(editor);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(final AdjustmentEvent e) {
                prevPrevTrailing = prevTrailing;
                prevTrailing = determineTrailing();
            }
        });

        final DefaultCaret caret = (DefaultCaret) editor.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        this.editor.addKeyListener(new KeyListenerSupport() {

            @Override
            public void keyReleased(final KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.CHAR_UNDEFINED || GuiService.get().isModifierDown()) {
                    return;
                }
                //any character key restores trailing
                editor.setCaretPosition(editor.getDocument().getLength());
            }

        });

        panel.addComponentListener(new ComponentListenerSupport() {
            @Override
            public void componentResized(final ComponentEvent e) {
                if (prevTrailing || prevPrevTrailing) {
                    prevPrevTrailing = true;
                    prevTrailing = true;
                    updateScrollBar(null);
                }
            }

        });

        return panel;
    }

    @Override
    protected void onOpen() {
        ACTIVE_LOGS.incrementAndGet();
        initUpdateFuture();
    }

    private void initUpdateFuture() {
        Assertions.checkNull(updateFuture);

        final Duration refreshInterval = getRefreshInterval();
        updateFuture = getScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    update();
                } catch (final InterruptedException e) {
                    //noop
                }
            }
        }, 0, refreshInterval.longValue(), refreshInterval.getTimeUnit().timeUnitValue());
    }

    @Override
    protected void onClose() {
        final Future<?> updateFutureCopy = updateFuture;
        if (updateFutureCopy != null) {
            updateFutureCopy.cancel(true);
            updateFuture = null;
        }
        ACTIVE_LOGS.decrementAndGet();
        maybeCloseScheduledExecutor();
    }

    private synchronized void update() throws InterruptedException {
        if (!getComponent().isShowing()) {
            return;
        }
        onUpdate();
        if (source == null) {
            return;
        }
        tailLog(logTo);
    }

    private void tailLog(final FDate from) throws InterruptedException {
        if (initRequired) {
            init();
        }
        final ICloseableIterable<LogViewerEntry> entries = source.getLogViewerEntries(from, getMaxTrailingMessages());
        StringBuilder append = new StringBuilder();
        int count = 0;
        final MutableReference<TrailingState> trailingStateRef = new MutableReference<>();
        try (ICloseableIterator<LogViewerEntry> iterator = entries.iterator()) {
            while (true) {
                final LogViewerEntry entry = iterator.next();
                if (!Objects.equals(logTo, entry.getTime()) && (logTo == null || logTo.isBefore(entry.getTime()))) {
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
                        appendMessagesToDocument(append, trailingStateRef);
                        append = new StringBuilder();
                        count = 0;
                    }
                }
            }
        } catch (final NoSuchElementException e) {
            //end reached
        }
        if (count > 0) {
            appendMessagesToDocument(append, trailingStateRef);
        }
    }

    protected Integer getMaxTrailingMessages() {
        return 10000;
    }

    protected void onUpdate() throws InterruptedException {}

    private void appendMessagesToDocument(final StringBuilder message,
            final MutableReference<TrailingState> trailingStateRef) throws InterruptedException {
        EventDispatchThreadUtil.invokeAndWait(() -> {
            if (message.length() > 0) {
                if (editor.getDocument().getLength() > 0) {
                    message.insert(0, "\n");
                }
                try {
                    TrailingState trailingState = trailingStateRef.get();
                    if (trailingState == null) {
                        trailingState = new TrailingState();
                        trailingStateRef.set(trailingState);
                    }
                    synchronized (updatingLock) {
                        getComponent().setIgnoreRepaint(true);
                    }
                    //https://stackoverflow.com/questions/12916192/how-to-know-if-a-jscrollbar-has-reached-the-bottom-of-the-jscrollpane
                    final EditorKit kit = editor.getEditorKit();
                    final int position = editor.getDocument().getLength();
                    kit.read(new CharSequenceReader(message), editor.getDocument(), position);
                    trail(trailingState);

                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void trail(final TrailingState trailingState) {
        updateScrollBarLater(trailingState);
    }

    private boolean determineTrailing() {
        if (editor.getDocument().getLength() == 0) {
            return true;
        }
        final JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        final boolean trailing = scrollBar.getValue() >= scrollBar.getMaximum()
                - (scrollBar.getVisibleAmount() * DETECT_TRAILING_TOLERANCE_FACTOR);
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
        if (entry.getTime() != null) {
            message.append("<b>");
            message.append(FDates.toString(entry.getTime()));
            message.append(":</b> ");
        }
        message.append(entry.getMessage().replace("'", "&#x27;"));
        message.append("</div>");
        final String messageStr = message.toString();
        return messageStr;
    }

    private void updateScrollBarLater(final TrailingState trailingState) {
        EventDispatchThreadUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
                synchronized (updatingLock) {
                    getComponent().validate();
                    updateScrollBar(trailingState);
                    getComponent().setIgnoreRepaint(false);
                    getComponent().repaint();
                }
            }

        });
    }

    private void updateScrollBar(final TrailingState trailingState) {
        final JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        if (trailingState == null || trailingState.trailing) {
            scrollBar.setValue(scrollBar.getMaximum());
        } else {
            scrollBar.setValue(trailingState.scrollBarValueBefore);
        }
    }

    private final class TrailingState {
        private final boolean trailing = determineTrailing();
        private final int scrollBarValueBefore = scrollPane.getVerticalScrollBar().getValue();
    }
}
