package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.dialog.expression;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.AddSeriesPanelLayout;
import de.invesdwin.context.client.swing.rsyntaxtextarea.DynamicRSyntaxTextAreaLayout;

@NotThreadSafe
public class ExpressionSettingsPanelLayout extends JPanel {

    //CHECKSTYLE:OFF
    public final DynamicRSyntaxTextAreaLayout tf_expression;
    public final JLabel lbl_expression;
    public final JButton btn_applyExpression;
    //CHECKSTYLE:ON

    public ExpressionSettingsPanelLayout(final Window window) {
        setLayout(new BorderLayout(5, 5));
        tf_expression = new DynamicRSyntaxTextAreaLayout(window);
        add(tf_expression, BorderLayout.CENTER);

        btn_applyExpression = new JButton("Apply");
        add(btn_applyExpression, BorderLayout.EAST);

        lbl_expression = new JLabel("");
        lbl_expression.setIcon(AddSeriesPanelLayout.ICON_EXPRESSION);
        add(lbl_expression, BorderLayout.WEST);
    }

}
