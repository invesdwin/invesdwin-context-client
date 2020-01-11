package de.invesdwin.context.client.swing.component.logviewer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;

import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.collections.iterable.ICloseableIterator;
import de.invesdwin.util.swing.Dialogs;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.util.time.fdate.FDate;
import de.invesdwin.util.time.fdate.FDates;
import de.invesdwin.util.time.fdate.FTimeUnit;

@ThreadSafe
public class LogViewerView extends AView<LogViewerView, JPanel> {

    private final Timer timer;
    @GuardedBy("this")
    private ILogViewerSource source;
    @GuardedBy("this")
    private FDate logTo;
    @GuardedBy("this")
    private final Set<String> lastLogToMessages = new HashSet<>();
    @GuardedBy("this")
    private boolean background;
    private JEditorPane editor;

    public LogViewerView(final ILogViewerSource source) {
        this.source = source;
        this.timer = new Timer(getTimerInterval().intValue(FTimeUnit.MILLISECONDS), new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                update();
            }
        });
        init();
    }

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    private void init() {
        lastLogToMessages.clear();
        background = false;
        if (editor != null) {
            try {
                editor.getDocument().remove(0, editor.getDocument().getLength());
            } catch (final BadLocationException e) {
                throw new RuntimeException(e);
            }
        }
        logTo = null;
    }

    public ILogViewerSource getSource() {
        return source;
    }

    public synchronized void setSource(final ILogViewerSource source) {
        this.source = source;
        this.init();
    }

    private Duration getTimerInterval() {
        return new Duration(1, FTimeUnit.SECONDS);
    }

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    protected JPanel initComponent() {
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 0));

        final JScrollPane scrollPane = new JScrollPane();
        panel.add(scrollPane, BorderLayout.CENTER);

        this.editor = Dialogs.newHtmlMessagePane();
        scrollPane.setViewportView(editor);
        return panel;
    }

    @Override
    protected void onOpen() {
        timer.start();
    }

    @Override
    protected void onClose() {
        timer.stop();
    }

    private void update() {
        if (!getComponent().isShowing()) {
            return;
        }
        tailLog();
    }

    protected synchronized void tailLog() {
        if (source == null) {
            return;
        }
        final FDate from;
        if (logTo == null) {
            from = FDate.MIN_DATE;
        } else {
            from = logTo;
        }
        final StringBuilder prepend = new StringBuilder();
        final ICloseableIterable<LogViewerEntry> entries = source.getLogViewerEntries(from, null);
        try (ICloseableIterator<LogViewerEntry> iterator = entries.iterator()) {
            while (true) {
                final LogViewerEntry entry = iterator.next();
                final String messageStr = entryToString(entry);
                if (lastLogToMessages.add(messageStr)) {
                    if (prepend.length() > 0) {
                        prepend.insert(0, "\n");
                    }
                    prepend.insert(0, messageStr);
                }
            }
        } catch (final NoSuchElementException e) {
            //end reached
        }
        if (editor.getDocument().getLength() > 0) {
            prepend.insert(0, "\n");
        }
        try {
            editor.getDocument().insertString(0, prepend.toString(), null);
        } catch (final BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    private String entryToString(final LogViewerEntry entry) {
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
        if (logTo == null || logTo.isBefore(entry.getTime())) {
            lastLogToMessages.clear();
            logTo = entry.getTime();
        }
        final String messageStr = message.toString();
        return messageStr;
    }
}
