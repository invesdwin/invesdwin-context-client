package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.icons.PlotIcons;
import de.invesdwin.context.client.swing.rsyntaxtextarea.DynamicRSyntaxTextAreaLayout;

@NotThreadSafe
public class AddSeriesPanelLayout extends JPanel {

    public static final Dimension DIMENSION_TEXTFIELD = new Dimension(50, 28);
    public static final ImageIcon ICON_EXPRESSION = PlotIcons.EXPRESSION.newIcon(14);
    //CHECKSTYLE:OFF
    public final JPanel pnl_expression;
    public final DynamicRSyntaxTextAreaLayout tf_expression;
    public final JButton btn_addExpression;
    public final JTextField tf_search;
    public final JPanel pnl_indicator;
    public final JTable tbl_indicator;
    public final JButton btn_close;
    public final JLabel lbl_expression;
    public final JLabel lbl_search;
    //CHECKSTYLE:ON

    public AddSeriesPanelLayout(final Window window) {
        setLayout(new BorderLayout());

        pnl_expression = new JPanel();
        pnl_expression.setLayout(new BorderLayout(5, 5));
        pnl_expression.setBorder(new CompoundBorder(
                new TitledBorder(null, "Expression", TitledBorder.LEADING, TitledBorder.TOP, null, null),
                new EmptyBorder(0, 5, 5, 5)));
        add(pnl_expression, BorderLayout.NORTH);

        btn_addExpression = new JButton("Add");
        pnl_expression.add(btn_addExpression, BorderLayout.EAST);

        tf_expression = new DynamicRSyntaxTextAreaLayout(window);
        pnl_expression.add(tf_expression, BorderLayout.CENTER);

        lbl_expression = new JLabel("");
        lbl_expression.setIcon(ICON_EXPRESSION);
        pnl_expression.add(lbl_expression, BorderLayout.WEST);

        pnl_indicator = new JPanel();
        pnl_indicator.setLayout(new BorderLayout(5, 5));
        pnl_indicator.setBorder(new CompoundBorder(
                new TitledBorder(null, "Indicator", TitledBorder.LEADING, TitledBorder.TOP, null, null),
                new EmptyBorder(0, 5, 5, 5)));
        add(pnl_indicator, BorderLayout.CENTER);

        final JPanel pnl_search = new JPanel();
        pnl_search.setLayout(new BorderLayout(5, 5));
        tf_search = new JTextField();
        tf_search.setMinimumSize(DIMENSION_TEXTFIELD);
        tf_search.setPreferredSize(tf_search.getMinimumSize());
        pnl_search.add(tf_search, BorderLayout.CENTER);
        pnl_indicator.add(pnl_search, BorderLayout.NORTH);

        lbl_search = new JLabel("");
        lbl_search.setIcon(PlotIcons.SEARCH.newIcon(14));
        pnl_search.add(lbl_search, BorderLayout.WEST);

        final JScrollPane scrl_indicator = new JScrollPane();
        pnl_indicator.add(scrl_indicator, BorderLayout.CENTER);

        tbl_indicator = new JTable(new DefaultTableModel(
                new Object[][] { { "1", "2" }, { "3", "4" }, { "5", "6" }, { "7", "8" }, { "9", "10" }, { "11", "12" },
                        { "13", "14" }, { "15", "16" }, { "17", "18" }, { "19", "20" }, },
                new String[] { "New column", "New column" }));
        tbl_indicator.setEnabled(false);
        tbl_indicator.setShowVerticalLines(false);
        tbl_indicator.setShowHorizontalLines(false);
        tbl_indicator.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl_indicator.setShowGrid(false);
        tbl_indicator.setAutoCreateRowSorter(true);
        scrl_indicator.setViewportView(tbl_indicator);

        final JPanel pnl_close = new JPanel();
        add(pnl_close, BorderLayout.SOUTH);

        btn_close = new JButton("Close");
        pnl_close.add(btn_close);
    }
}