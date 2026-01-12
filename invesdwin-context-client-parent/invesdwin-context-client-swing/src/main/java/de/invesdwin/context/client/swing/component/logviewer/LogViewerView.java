package de.invesdwin.context.client.swing.component.logviewer;

import java.awt.BorderLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

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
import de.invesdwin.util.collections.iterable.buffer.BufferingIterator;
import de.invesdwin.util.collections.iterable.buffer.IBufferingIterator;
import de.invesdwin.util.concurrent.Executors;
import de.invesdwin.util.concurrent.Threads;
import de.invesdwin.util.concurrent.WrappedExecutorService;
import de.invesdwin.util.concurrent.WrappedScheduledExecutorService;
import de.invesdwin.util.concurrent.future.Futures;
import de.invesdwin.util.concurrent.pool.AReferenceCountedObjectPool;
import de.invesdwin.util.concurrent.pool.IObjectPool;
import de.invesdwin.util.concurrent.reference.MutableReference;
import de.invesdwin.util.concurrent.taskinfo.provider.TaskInfoRunnable;
import de.invesdwin.util.error.Throwables;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.swing.Components;
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

    public static final IObjectPool<WrappedScheduledExecutorService> SCHEDULED_EXECUTOR_POOL = new AReferenceCountedObjectPool<WrappedScheduledExecutorService>() {

        @Override
        public void invalidateObject(final WrappedScheduledExecutorService element) {
            element.shutdownNow();
        }

        @Override
        protected WrappedScheduledExecutorService newObject() {
            //reduce cpu load by using max 1 thread
            return Executors.newScheduledThreadPool(LogViewerView.class.getSimpleName() + "_SCHEDULED", 1)
                    .setDynamicThreadName(false);
        }
    };
    public static final IObjectPool<WrappedExecutorService> LIMITED_EXECUTOR_POOL = new AReferenceCountedObjectPool<WrappedExecutorService>() {

        @Override
        public void invalidateObject(final WrappedExecutorService element) {
            element.shutdownNow();
        }

        @Override
        protected WrappedExecutorService newObject() {
            //reduce cpu load by using max 1 thread
            return Executors.newFixedThreadPool(LogViewerView.class.getSimpleName() + "_LIMITED", 1)
                    .setDynamicThreadName(false);
        }
    };
    private static final double DETECT_TRAILING_TOLERANCE_FACTOR = 1.05;
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
    @GuardedBy("self")
    private final List<Future<?>> updateFutures = new ArrayList<>();
    private volatile boolean initRequired = true;
    private JScrollPane scrollPane;
    private final AtomicInteger appendingMessagesCount = new AtomicInteger();
    private boolean prevTrailing = true;
    private boolean prevPrevTrailing = true;
    private WrappedScheduledExecutorService scheduledExecutor;
    private WrappedExecutorService limitedExecutor;

    public LogViewerView(final ILogViewerSource source) {
        this.source = source;
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
        onClose();
        LogViewerView.this.source = source;
        initRequired = true;
        onOpen();
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
                final int selectionStart = editor.getSelectionStart();
                final int selectionEnd = editor.getSelectionEnd();
                if (selectionStart == selectionEnd) {
                    //any character key restores trailing
                    editor.setCaretPosition(editor.getDocument().getLength());
                }
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
    protected synchronized void onOpen() {
        Assertions.checkNull(updateFuture);
        Assertions.checkNull(scheduledExecutor);
        Assertions.checkNull(limitedExecutor);

        final Duration refreshInterval = getRefreshInterval();
        scheduledExecutor = SCHEDULED_EXECUTOR_POOL.borrowObject();
        limitedExecutor = LIMITED_EXECUTOR_POOL.borrowObject();
        updateFuture = scheduledExecutor.scheduleAtFixedRate(new Runnable() {
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
    protected synchronized void onClose() {
        final Future<?> updateFutureCopy = updateFuture;
        if (updateFutureCopy != null) {
            updateFutureCopy.cancel(true);
            updateFuture = null;
        }
        final WrappedScheduledExecutorService scheduledExecutorCopy = scheduledExecutor;
        if (scheduledExecutorCopy != null) {
            SCHEDULED_EXECUTOR_POOL.returnObject(scheduledExecutorCopy);
            scheduledExecutor = null;
        }
        final WrappedExecutorService updateExecutorCopy = limitedExecutor;
        if (updateExecutorCopy != null) {
            LIMITED_EXECUTOR_POOL.returnObject(updateExecutorCopy);
            limitedExecutor = null;
        }
    }

    private synchronized void update() throws InterruptedException {
        if (!getComponent().isShowing()) {
            return;
        }
        onUpdate();
        if (source == null) {
            return;
        }
        if (initRequired) {
            init();
        }
        tailLog(logTo);
    }

    private void tailLog(final FDate from) throws InterruptedException {
        final ICloseableIterable<LogViewerEntry> entries = source.getLogViewerEntries(from, getMaxTrailingMessages());
        StringBuilder append = new StringBuilder();
        int count = 0;
        final MutableReference<TrailingState> trailingStateRef = new MutableReference<>();

        final WrappedExecutorService limitedExecutorCopy = limitedExecutor;
        if (limitedExecutorCopy == null) {
            return;
        }
        try (IBufferingIterator<Future<?>> futures = new BufferingIterator<>()) {
            try {
                try (ICloseableIterator<LogViewerEntry> iterator = entries.iterator()) {
                    while (true) {
                        final LogViewerEntry entry = iterator.next();
                        if (!Objects.equals(logTo, entry.getTime())
                                && (logTo == null || logTo.isBefore(entry.getTime()))) {
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
                                Threads.throwIfInterrupted();
                                final TaskInfoRunnable task = appendMessagesToDocumentAsync(append, trailingStateRef);
                                futures.add(limitedExecutorCopy.submit(task));
                                append = new StringBuilder();
                                count = 0;
                            }
                        }
                    }
                } catch (final NoSuchElementException e) {
                    //end reached
                }
                if (count > 0) {
                    Threads.throwIfInterrupted();
                    if (futures.isEmpty()) {
                        //skip task info for short updates
                        appendMessagesToDocument(append, trailingStateRef);
                    } else {
                        final TaskInfoRunnable task = appendMessagesToDocumentAsync(append, trailingStateRef);
                        futures.add(limitedExecutorCopy.submit(task));
                    }
                }
                Futures.wait(futures);
            } catch (final Throwable t) {
                while (!futures.isEmpty()) {
                    final Future<?> future = futures.next();
                    future.cancel(true);
                }
                if (!Throwables.isCausedByInterrupt(t)) {
                    throw Throwables.propagate(t);
                }
            }
        }
    }

    private TaskInfoRunnable appendMessagesToDocumentAsync(final StringBuilder append,
            final MutableReference<TrailingState> trailingStateRef) {
        final StringBuilder messages = append;
        final TaskInfoRunnable task = TaskInfoRunnable.of("Loading logs",
                () -> appendMessagesToDocument(messages, trailingStateRef));
        return task;
    }

    protected Integer getMaxTrailingMessages() {
        return 10_000;
    }

    protected void onUpdate() throws InterruptedException {}

    private void appendMessagesToDocument(final StringBuilder message,
            final MutableReference<TrailingState> trailingStateRef) {
        if (message.length() == 0) {
            return;
        }
        while (appendingMessagesCount.get() > 1) {
            FTimeUnit.MILLISECONDS.sleepNoInterrupt(1);
        }
        appendingMessagesCount.incrementAndGet();
        EventDispatchThreadUtil.invokeLater(() -> {
            try {
                final int selectionStartBefore = editor.getSelectionStart();
                final int selectionEndBefore = editor.getSelectionEnd();
                if (editor.getDocument().getLength() > 0) {
                    message.insert(0, "\n");
                }
                TrailingState trailingState = trailingStateRef.get();
                if (trailingState == null) {
                    trailingState = new TrailingState();
                    trailingStateRef.set(trailingState);
                }
                getComponent().setIgnoreRepaint(true);
                //https://stackoverflow.com/questions/12916192/how-to-know-if-a-jscrollbar-has-reached-the-bottom-of-the-jscrollpane
                final EditorKit kit = editor.getEditorKit();
                final int position = editor.getDocument().getLength();
                kit.read(new CharSequenceReader(message), editor.getDocument(), position);
                trail(trailingState, selectionStartBefore, selectionEndBefore);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            } finally {
                appendingMessagesCount.decrementAndGet();
                getComponent().setIgnoreRepaint(false);
                getComponent().repaint();
            }
        });
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

    private void trail(final TrailingState trailingState, final int selectionStartBefore,
            final int selectionEndBefore) {
        getComponent().validate();
        if (selectionStartBefore != selectionEndBefore) {
            //keep selection
            Components.setSelectionStart(editor, selectionStartBefore);
            Components.setSelectionEnd(editor, selectionEndBefore);
        } else {
            //trail
            updateScrollBar(trailingState);
        }
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

        private final boolean trailing;
        private final int scrollBarValueBefore;

        private TrailingState() {
            this.trailing = determineTrailing();
            this.scrollBarValueBefore = scrollPane.getVerticalScrollBar().getValue();
        }

    }
}
