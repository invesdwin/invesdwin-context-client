package de.invesdwin.context.client.swing.rsyntaxtextarea;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ComponentEvent;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.CaretStyle;
import org.fife.ui.rtextarea.RTextScrollPane;

import de.invesdwin.util.math.Integers;
import de.invesdwin.util.swing.listener.ComponentListenerSupport;
import de.invesdwin.util.swing.listener.DocumentListenerSupport;

@NotThreadSafe
public class DynamicRSyntaxTextAreaLayout extends JPanel {

    private static final int MIN_COLUMNS = 1;
    //CHECKSTYLE:OFF
    public final RSyntaxTextArea textArea;
    public final RTextScrollPane scrollPane;
    //CHECKSTYLE:OFF
    private final Window window;

    public DynamicRSyntaxTextAreaLayout(final Window window) {
        this.window = window;
        setLayout(new BorderLayout());
        textArea = new RSyntaxTextArea();
        textArea.setRows(getMinRows());
        textArea.setColumns(MIN_COLUMNS);
        textArea.setBorder(new JTextField().getBorder());
        textArea.setHighlightCurrentLine(false);
        textArea.setFont(new JTextArea().getFont());
        textArea.setCaretStyle(RSyntaxTextArea.INSERT_MODE, CaretStyle.VERTICAL_LINE_STYLE);
        textArea.setCaretStyle(RSyntaxTextArea.OVERWRITE_MODE, CaretStyle.BLOCK_STYLE);
        scrollPane = new RTextScrollPane();
        scrollPane.setViewportView(textArea);
        scrollPane.setLineNumbersEnabled(getMinRows() > 1);
        scrollPane.setFoldIndicatorEnabled(false);
        scrollPane.setIconRowHeaderEnabled(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getHorizontalScrollBar().addComponentListener(new ComponentListenerSupport() {
            @Override
            public void componentShown(final ComponentEvent e) {
                pack();
            }

            @Override
            public void componentHidden(final ComponentEvent e) {
                pack();
            }
        });
        add(scrollPane, BorderLayout.CENTER);

        /**
         * Dynamically adjust height
         */
        textArea.getDocument().addDocumentListener(new DocumentListenerSupport() {

            @Override
            protected void update(final DocumentEvent e) {
                final int lineCount = Integers.max(getMinRows(), textArea.getLineCount());
                final boolean change = lineCount != textArea.getRows();
                if (change) {
                    scrollPane.setLineNumbersEnabled(lineCount > 1);
                    if (lineCount <= getMaxRows()) {
                        textArea.setRows(lineCount);
                        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                    } else {
                        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                    }
                    pack();
                }
            }

        });
    }

    private void pack() {
        if (window != null) {
            synchronized (this) {
                final Dimension minimumSizeBefore = window.getMinimumSize();
                window.setMinimumSize(new Dimension(window.getWidth(), 1));
                window.pack();
                window.setMinimumSize(minimumSizeBefore);
            }
        }
    }

    protected int getMinRows() {
        return 1;
    }

    protected int getMaxRows() {
        return 10;
    }

}
