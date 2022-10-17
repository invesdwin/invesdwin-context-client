package de.invesdwin.context.client.swing.rsyntaxtextarea;

import java.awt.BorderLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ComponentEvent;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.CaretStyle;
import org.fife.ui.rtextarea.RTextScrollPane;

import de.invesdwin.context.client.swing.rsyntaxtextarea.expression.ExpressionRSyntaxTextArea;
import de.invesdwin.context.client.swing.rsyntaxtextarea.expression.ExpressionTokenMaker;
import de.invesdwin.util.math.Integers;
import de.invesdwin.util.swing.Components;
import de.invesdwin.util.swing.HiDPI;
import de.invesdwin.util.swing.listener.ComponentListenerSupport;
import de.invesdwin.util.swing.listener.DocumentListenerSupport;

@NotThreadSafe
public class DynamicRSyntaxTextAreaPanel extends JPanel {

    private static final int MIN_COLUMNS = 1;
    //CHECKSTYLE:OFF
    public final ExpressionRSyntaxTextArea textArea;
    public final RTextScrollPane scrollPane;
    //CHECKSTYLE:OFF
    private final Window window;

    public DynamicRSyntaxTextAreaPanel(final Window window) {
        this.window = window;
        setLayout(new BorderLayout());
        textArea = new ExpressionRSyntaxTextArea();
        textArea.setRows(getMinRows());
        textArea.setColumns(MIN_COLUMNS);
        textArea.setBorder(new JTextField().getBorder());
        textArea.setHighlightCurrentLine(false);
        textArea.setCaretStyle(RSyntaxTextArea.INSERT_MODE, CaretStyle.VERTICAL_LINE_STYLE);
        textArea.setCaretStyle(RSyntaxTextArea.OVERWRITE_MODE, CaretStyle.BLOCK_STYLE);
        textArea.setSyntaxEditingStyle(getSyntaxEditingStyle());
        textArea.setParserDelay(250);
        textArea.setFont(HiDPI.scale(textArea.getFont()));
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
                Components.packHeight(window);
            }

            @Override
            public void componentHidden(final ComponentEvent e) {
                Components.packHeight(window);
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
                        textArea.setRows(getMaxRows());
                        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                    }
                    Components.packHeight(window);
                }
            }

        });

        //disable tab character: https://stackoverflow.com/questions/525855/moving-focus-from-jtextarea-using-tab-key
        textArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        textArea.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
    }

    protected String getSyntaxEditingStyle() {
        return ExpressionTokenMaker.getSyntaxStyle();
    }

    protected int getMinRows() {
        return 1;
    }

    protected int getMaxRows() {
        return 10;
    }

}
