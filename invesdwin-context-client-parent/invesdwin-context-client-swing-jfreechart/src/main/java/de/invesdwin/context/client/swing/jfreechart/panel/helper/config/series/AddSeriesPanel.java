package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;

import org.springframework.web.util.HtmlUtils;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.PlotConfigurationHelper;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.expression.IExpressionSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.indicator.IIndicatorSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.util.error.Throwables;
import de.invesdwin.util.lang.Strings;
import de.invesdwin.util.math.expression.AExpressionVisitor;
import de.invesdwin.util.math.expression.IExpression;
import de.invesdwin.util.math.expression.eval.operation.BinaryOperation;
import de.invesdwin.util.swing.Dialogs;
import de.invesdwin.util.swing.icon.ChangeColorImageFilter;
import de.invesdwin.util.swing.listener.DocumentListenerSupport;
import de.invesdwin.util.swing.listener.MouseListenerSupport;
import de.invesdwin.util.swing.listener.MouseMotionListenerSupport;

@NotThreadSafe
public class AddSeriesPanel extends JPanel {

    public static final Color COLOR_EXPRESSION_PENDING_INVALID = Color.RED;
    public static final Color COLOR_EXPRESSION_PENDING_VALID = Color.GREEN.darker();

    public static final Icon ICON_EXPRESSION = AddSeriesPanelLayout.ICON_EXPRESSION;
    public static final Icon ICON_EXPRESSION_PENDING_INVALID = ChangeColorImageFilter
            .apply(AddSeriesPanelLayout.ICON_EXPRESSION, COLOR_EXPRESSION_PENDING_INVALID);
    public static final Icon ICON_EXPRESSION_PENDING_VALID = ChangeColorImageFilter
            .apply(AddSeriesPanelLayout.ICON_EXPRESSION, COLOR_EXPRESSION_PENDING_VALID);

    private static final org.slf4j.ext.XLogger LOG = org.slf4j.ext.XLoggerFactory.getXLogger(AddSeriesPanel.class);
    private static final Cursor HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private final PlotConfigurationHelper plotConfigurationHelper;

    private final AddSeriesPanelLayout layout;

    public AddSeriesPanel(final PlotConfigurationHelper plotConfigurationHelper, final AddSeriesDialog dialog) {
        this.plotConfigurationHelper = plotConfigurationHelper;
        setLayout(new BorderLayout());

        layout = new AddSeriesPanelLayout(dialog);
        add(layout, BorderLayout.CENTER);

        layout.tbl_indicator.setModel(newTableModel(""));

        layout.tbl_indicator.setCursor(HAND_CURSOR);
        layout.tbl_indicator.addMouseMotionListener(new MouseMotionListenerSupport() {
            @Override
            public void mouseMoved(final MouseEvent e) {
                final int selectedRow = layout.tbl_indicator.rowAtPoint(e.getPoint());
                layout.tbl_indicator.setRowSelectionInterval(selectedRow, selectedRow);
                final String selectedName = (String) layout.tbl_indicator.getModel().getValueAt(selectedRow, 0);
                final IIndicatorSeriesProvider selectedValue = plotConfigurationHelper
                        .getIndicatorSeriesProvider(selectedName);
                layout.tbl_indicator.setToolTipText(selectedValue.getDescription());
            }
        });
        layout.tbl_indicator.addMouseListener(new MouseListenerSupport() {
            @Override
            public void mouseReleased(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    final int selectedRow = layout.tbl_indicator.getSelectedRow();
                    final String selectedName = (String) layout.tbl_indicator.getModel().getValueAt(selectedRow, 0);
                    final IIndicatorSeriesProvider selectedValue = plotConfigurationHelper
                            .getIndicatorSeriesProvider(selectedName);
                    final IExpression[] args = selectedValue.getDefaultValues();
                    final String expressionString = selectedValue.getExpressionString(args);
                    try {
                        final IPlotSourceDataset dataset = selectedValue
                                .newInstance(plotConfigurationHelper.getChartPanel(), args);
                        dataset.setIndicatorSeriesProvider(selectedValue);
                        dataset.setIndicatorSeriesArguments(args);
                        dataset.setSeriesTitle(selectedValue.getTitle(expressionString));
                    } catch (final Throwable t) {
                        LOG.warn("Error adding series [" + selectedValue.getName() + "] with expression ["
                                + expressionString + "]\n" + Throwables.getFullStackTrace(t));

                        Dialogs.showMessageDialog(layout,
                                "<html><b>Name:</b><br><pre>  " + selectedValue.getName()
                                        + "</pre><b>Expression:</b><br><pre>  " + expressionString
                                        + "</pre><br><b>Error:</b><br><pre>  "
                                        + HtmlUtils.htmlEscape(Throwables.concatMessagesShort(t).replace("\n", "\n  "))
                                        + "</pre>",
                                "Error", Dialogs.ERROR_MESSAGE);
                    }
                }
            }
        });

        layout.btn_close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                dialog.close();
            }
        });
        layout.tf_search.getDocument().addDocumentListener(new DocumentListenerSupport() {
            @Override
            protected void update(final DocumentEvent e) {
                layout.tbl_indicator.setModel(newTableModel(layout.tf_search.getText()));
            }
        });
        layout.btn_addExpression.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                addExpression();
            }
        });
        layout.btn_addExpression_popup_debug.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                final IPlotSourceDataset originalDataset = addExpression();
                if (originalDataset != null) {
                    final IExpressionSeriesProvider provider = plotConfigurationHelper.getExpressionSeriesProvider();
                    final IExpression originalExpression = provider
                            .parseExpression(layout.tf_expression.textArea.getText());
                    final Set<String> duplicateExpressionFilter = new HashSet<>();
                    duplicateExpressionFilter.add(originalExpression.toString());
                    final AExpressionVisitor visitor = new AExpressionVisitor() {

                        @Override
                        protected void visitOther(final IExpression expression) {
                            final String plotPaneId = provider.getPlotPaneId(expression);
                            prefixRangeAxisId("DEBUG Other: ",
                                    addExpressionDebug(expression, plotPaneId, duplicateExpressionFilter));
                        }

                        @Override
                        protected boolean visitMath(final BinaryOperation expression) {
                            final String plotPaneId = provider.getPlotPaneId(expression);
                            prefixRangeAxisId("DEBUG Math: ",
                                    addExpressionDebug(expression, plotPaneId, duplicateExpressionFilter));
                            return false;
                        }

                        @Override
                        protected boolean visitComparison(final BinaryOperation expression) {
                            final String plotPaneId = provider.getPlotPaneId(expression);
                            final IPlotSourceDataset dataset = addExpressionDebug(expression, plotPaneId,
                                    duplicateExpressionFilter);
                            if (dataset != null) {
                                prefixRangeAxisId("DEBUG CompResult: ", dataset);
                                prefixRangeAxisId("DEBUG Comparison: ",
                                        addExpressionDebug(expression.getLeft(), plotPaneId, duplicateExpressionFilter));
                                prefixRangeAxisId("DEBUG Comparison: ", addExpressionDebug(expression.getRight(),
                                        plotPaneId, duplicateExpressionFilter));
                            } else if (originalExpression.toString().equals(expression.toString())) {
                                prefixRangeAxisId("DEBUG CompResult: ", originalDataset);
                                prefixRangeAxisId("DEBUG Comparison: ", addExpressionDebug(expression.getLeft(),
                                        originalDataset.getInitialPlotPaneId(), duplicateExpressionFilter));
                                prefixRangeAxisId("DEBUG Comparison: ", addExpressionDebug(expression.getRight(),
                                        originalDataset.getInitialPlotPaneId(), duplicateExpressionFilter));
                            }
                            return false;
                        }

                        private void prefixRangeAxisId(final String prefix, final IPlotSourceDataset dataset) {
                            if (dataset != null) {
                                dataset.setRangeAxisId(prefix + dataset.getSeriesTitle());
                            }
                        }

                        @Override
                        protected boolean visitLogicalCombination(final BinaryOperation expression) {
                            return true;
                        }

                    };
                    visitor.process(originalExpression);
                }
            }
        });
        layout.tf_expression.textArea.getDocument().addDocumentListener(new DocumentListenerSupport() {
            @Override
            protected void update(final DocumentEvent e) {
                final String expression = layout.tf_expression.textArea.getText();
                if (Strings.isNotBlank(expression)) {
                    try {
                        final IExpressionSeriesProvider provider = plotConfigurationHelper
                                .getExpressionSeriesProvider();
                        final IExpression parsedExpression = provider.parseExpression(expression);
                        layout.lbl_expression.setIcon(ICON_EXPRESSION_PENDING_VALID);
                        layout.lbl_expression.setToolTipText("<html><b>Valid:</b><br><pre>  "
                                + HtmlUtils.htmlEscape(parsedExpression.toString().replace("\n", "\n  ")) + "</pre>");
                    } catch (final Throwable t) {
                        layout.lbl_expression.setIcon(ICON_EXPRESSION_PENDING_INVALID);
                        layout.lbl_expression.setToolTipText("<html><b>Error:</b><br><pre>  "
                                + HtmlUtils.htmlEscape(Throwables.concatMessagesShort(t).replace("\n", "\n  "))
                                + "</pre>");
                    }
                } else {
                    layout.lbl_expression.setIcon(ICON_EXPRESSION);
                    layout.lbl_expression.setToolTipText(null);
                }
            }
        });

        if (dialog != null) {
            dialog.getRootPane().setDefaultButton(layout.btn_close);
        }

        if (plotConfigurationHelper.getExpressionSeriesProvider() == null) {
            layout.pnl_expression.setVisible(false);
        }
        if (plotConfigurationHelper.getIndicatorSeriesProviders().isEmpty()) {
            layout.pnl_indicator.setVisible(false);
        }

        plotConfigurationHelper.getExpressionSeriesProvider().configureEditor(layout.tf_expression.textArea);
    }

    private IPlotSourceDataset addExpressionDebug(final IExpression expression, final String plotPaneId,
            final Set<String> duplicateExpressionFilter) {
        final String expressionStr = expression.toString();
        try {
            if (!duplicateExpressionFilter.add(expressionStr)) {
                return null;
            }
            final IExpressionSeriesProvider provider = plotConfigurationHelper.getExpressionSeriesProvider();
            final IPlotSourceDataset dataset = provider.newInstance(plotConfigurationHelper.getChartPanel(),
                    expressionStr, plotPaneId);
            dataset.setExpressionSeriesProvider(provider);
            dataset.setExpressionSeriesArguments(expressionStr);
            dataset.setSeriesTitle(provider.getTitle(expressionStr));
            return dataset;
        } catch (final Throwable t) {
            LOG.warn("Error adding series for expression part [" + expressionStr + "]\n"
                    + Throwables.getFullStackTrace(t));

            Dialogs.showMessageDialog(layout,
                    "<html><b>Expression:</b><br><pre>  " + expressionStr + "</pre><br><b>Error:</b><br><pre>  "
                            + HtmlUtils.htmlEscape(Throwables.concatMessagesShort(t).replace("\n", "\n  ")) + "</pre>",
                    "Error", Dialogs.ERROR_MESSAGE);
            return null;
        }
    }

    private IPlotSourceDataset addExpression() {
        final String expression = layout.tf_expression.textArea.getText();
        if (Strings.isBlank(expression)) {
            Dialogs.showMessageDialog(layout, "Expression should not be blank.", "Error", Dialogs.ERROR_MESSAGE);
            return null;
        } else {
            final IExpressionSeriesProvider provider = plotConfigurationHelper.getExpressionSeriesProvider();
            try {
                final String plotPaneId = provider.getPlotPaneId(provider.parseExpression(expression));
                final IPlotSourceDataset dataset = provider.newInstance(plotConfigurationHelper.getChartPanel(),
                        expression, plotPaneId);
                dataset.setExpressionSeriesProvider(provider);
                dataset.setExpressionSeriesArguments(expression);
                dataset.setSeriesTitle(provider.getTitle(expression));

                layout.lbl_expression.setIcon(ICON_EXPRESSION);
                return dataset;
            } catch (final Throwable t) {
                LOG.warn("Error adding series for expression [" + expression + "]\n" + Throwables.getFullStackTrace(t));

                Dialogs.showMessageDialog(layout,
                        "<html><b>Expression:</b><br><pre>  " + expression + "</pre><br><b>Error:</b><br><pre>  "
                                + HtmlUtils.htmlEscape(Throwables.concatMessagesShort(t).replace("\n", "\n  "))
                                + "</pre>",
                        "Error", Dialogs.ERROR_MESSAGE);
                return null;
            }
        }
    }

    private DefaultTableModel newTableModel(final String search) {
        final DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Name");
        model.addColumn("Expression");
        final Collection<IIndicatorSeriesProvider> seriesProviders = plotConfigurationHelper
                .getIndicatorSeriesProviders();
        if (Strings.isBlank(search)) {
            for (final IIndicatorSeriesProvider seriesProvider : seriesProviders) {
                model.addRow(new Object[] { seriesProvider.getName(),
                        seriesProvider.getExpressionString(seriesProvider.getDefaultValues()) });
            }
        } else {
            final String searchString = search.trim();
            final Pattern searchPattern = Pattern.compile(searchString);
            for (final IIndicatorSeriesProvider seriesProvider : seriesProviders) {
                if (matches(seriesProvider, searchString, searchPattern)) {
                    model.addRow(new Object[] { seriesProvider.getName(),
                            seriesProvider.getExpressionString(seriesProvider.getDefaultValues()) });
                }
            }
        }
        return model;
    }

    private boolean matches(final IIndicatorSeriesProvider seriesProvider, final String searchString,
            final Pattern searchPattern) {
        if (matches(searchString, searchPattern, seriesProvider.getName())) {
            return true;
        } else if (matches(searchString, searchPattern, seriesProvider.getDescription())) {
            return true;
        } else if (matches(searchString, searchPattern, seriesProvider.getExpressionName())) {
            return true;
        }
        return false;
    }

    private boolean matches(final String searchString, final Pattern searchPattern, final String value) {
        return Strings.isNotBlank(value)
                && (Strings.containsIgnoreCase(value, searchString) || searchPattern.matcher(value).matches());
    }
}