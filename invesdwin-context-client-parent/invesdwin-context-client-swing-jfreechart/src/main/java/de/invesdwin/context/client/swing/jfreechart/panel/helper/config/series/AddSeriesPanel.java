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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.text.WordUtils;
import org.springframework.web.util.HtmlUtils;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.PlotConfigurationHelper;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.SeriesRendererType;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.expression.IExpressionSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.indicator.IIndicatorSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.util.concurrent.MutableReference;
import de.invesdwin.util.error.Throwables;
import de.invesdwin.util.lang.Strings;
import de.invesdwin.util.math.expression.AExpressionVisitor;
import de.invesdwin.util.math.expression.ExpressionVisitorSupport;
import de.invesdwin.util.math.expression.IExpression;
import de.invesdwin.util.math.expression.eval.operation.BinaryOperation;
import de.invesdwin.util.swing.Components;
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
    public static final int TOOLTIP_WORD_WRAP_LIMIT = 120;

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
                Components.setToolTipText(layout.tbl_indicator, selectedValue.getDescription());
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

                        Dialogs.showMessageDialog(layout, "<html><b>Name:</b><br><pre>  " + selectedValue.getName()
                                + "</pre><b>Expression:</b><br><pre>  " + expressionString
                                + "</pre><br><b>Error:</b><br><pre>  " + prepareErrorMessageForTooltip(t) + "</pre>",
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
                final IExpressionSeriesProvider provider = plotConfigurationHelper.getExpressionSeriesProvider();
                final String originalExpressionStr = layout.tf_expression.textArea.getText();
                if (Strings.isBlank(originalExpressionStr)) {
                    logExpressionBlank();
                } else {
                    final IExpression originalExpression;
                    try {
                        originalExpression = provider.parseExpression(originalExpressionStr);
                    } catch (final Throwable t) {
                        logExpressionException(originalExpressionStr, t);
                        return;
                    }
                    addExpressionDebugViaVisitor(provider, originalExpression);
                }
            }

            private void addExpressionDebugViaVisitor(final IExpressionSeriesProvider provider,
                    final IExpression originalExpression) {
                final Set<String> duplicateExpressionFilter = new HashSet<>();
                final MutableReference<SeriesRendererType> originalRendererType = new MutableReference<SeriesRendererType>(
                        SeriesRendererType.Line);
                final AExpressionVisitor visitor = new AExpressionVisitor() {

                    @Override
                    protected void visitOther(final IExpression expression) {
                        final String plotPaneId = provider.getPlotPaneId(expression);
                        addExpressionDebug(expression, plotPaneId, duplicateExpressionFilter, SeriesRendererType.Line);
                    }

                    @Override
                    protected boolean visitMath(final BinaryOperation expression) {
                        final String plotPaneId = provider.getPlotPaneId(expression);
                        addExpressionDebug(expression, plotPaneId, duplicateExpressionFilter, SeriesRendererType.Line);
                        return false;
                    }

                    @Override
                    protected boolean visitComparison(final BinaryOperation expression) {
                        final String plotPaneId = provider.getPlotPaneId(expression);
                        final String rangeAxisIdPrefixLeftRight = "X: ";
                        prefixRangeAxisId(rangeAxisIdPrefixLeftRight, addExpressionDebug(expression.getLeft(),
                                plotPaneId, duplicateExpressionFilter, SeriesRendererType.Line));
                        prefixRangeAxisId(rangeAxisIdPrefixLeftRight, addExpressionDebug(expression.getRight(),
                                plotPaneId, duplicateExpressionFilter, SeriesRendererType.Line));
                        addExpressionDebug(expression, plotPaneId, duplicateExpressionFilter, SeriesRendererType.Step);
                        return false;
                    }

                    private void prefixRangeAxisId(final String prefix, final IPlotSourceDataset dataset) {
                        if (dataset != null) {
                            dataset.setRangeAxisId(prefix + dataset.getRangeAxisId());
                        }
                    }

                    @Override
                    protected boolean visitLogicalCombination(final BinaryOperation expression) {
                        originalRendererType.set(SeriesRendererType.Step);
                        return true;
                    }

                };
                visitor.process(originalExpression);
                final String plotPaneId = provider.getPlotPaneId(originalExpression);
                addExpressionDebug(originalExpression, plotPaneId, duplicateExpressionFilter,
                        originalRendererType.get());
            }
        });
        layout.tf_expression.textArea.getDocument().addDocumentListener(new DocumentListenerSupport() {
            @Override
            protected void update(final DocumentEvent e) {
                validateExpressionAdd(plotConfigurationHelper);
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

    private void validateExpressionAdd(final PlotConfigurationHelper plotConfigurationHelper) {
        final String expression = layout.tf_expression.textArea.getText();
        validateExpressionAdd(layout.lbl_expression, expression, plotConfigurationHelper.getExpressionSeriesProvider());
    }

    public static void validateExpressionAdd(final JLabel lbl_expression, final String expression,
            final IExpressionSeriesProvider provider) {
        if (Strings.isNotBlank(expression)) {
            try {
                final IExpression parsedExpression = provider.parseExpression(expression);
                if (parsedExpression == null) {
                    lbl_expression.setIcon(AddSeriesPanel.ICON_EXPRESSION);
                    Components.setToolTipText(lbl_expression, null);
                    return;
                }
                lbl_expression.setIcon(AddSeriesPanel.ICON_EXPRESSION_PENDING_VALID);
                Components.setToolTipText(lbl_expression, "<html><b>Valid:</b><br><pre>  "
                        + HtmlUtils.htmlEscape(parsedExpression.toString().replace("\n", "\n  ")) + "</pre>");
            } catch (final Throwable t) {
                lbl_expression.setIcon(AddSeriesPanel.ICON_EXPRESSION_PENDING_INVALID);
                Components.setToolTipText(lbl_expression,
                        "<html><b>Error:</b><br><pre>  " + AddSeriesPanel.prepareErrorMessageForTooltip(t) + "</pre>");
            }
        } else {
            lbl_expression.setIcon(AddSeriesPanel.ICON_EXPRESSION);
            Components.setToolTipText(lbl_expression, null);
        }
    }

    public static String prepareErrorMessageForTooltip(final Throwable t) {
        String message = Throwables.concatMessagesShort(t);
        message = WordUtils.wrap(message, TOOLTIP_WORD_WRAP_LIMIT);
        message = message.replace("\n", "\n  ");
        message = HtmlUtils.htmlEscape(message);
        return message;
    }

    private void logExpressionException(final String expressionStr, final Throwable t) {
        LOG.warn("Error adding series for expression [" + expressionStr + "]\n" + Throwables.getFullStackTrace(t));

        Dialogs.showMessageDialog(layout, "<html><b>Expression:</b><br><pre>  " + expressionStr
                + "</pre><br><b>Error:</b><br><pre>  " + prepareErrorMessageForTooltip(t) + "</pre>", "Error",
                Dialogs.ERROR_MESSAGE);
    }

    private void logExpressionBlank() {
        Dialogs.showMessageDialog(layout, "Expression should not be blank.", "Error", Dialogs.ERROR_MESSAGE);
    }

    private IPlotSourceDataset addExpressionDebug(final IExpression expression, final String plotPaneId,
            final Set<String> duplicateExpressionFilter, final SeriesRendererType rendererType) {
        final String expressionStr = expression.toString();
        try {
            if (!duplicateExpressionFilter.add(expressionStr)) {
                return null;
            }
            final IExpressionSeriesProvider provider = plotConfigurationHelper.getExpressionSeriesProvider();
            final IPlotSourceDataset dataset = provider.newInstance(plotConfigurationHelper.getChartPanel(),
                    expressionStr, plotPaneId, rendererType);
            dataset.setExpressionSeriesProvider(provider);
            dataset.setExpressionSeriesArguments(expressionStr);
            dataset.setSeriesTitle(provider.getTitle(expressionStr));
            return dataset;
        } catch (final Throwable t) {
            logExpressionException(expressionStr, t);
            return null;
        }
    }

    private void addExpression() {
        final String expressionStr = layout.tf_expression.textArea.getText();
        if (Strings.isBlank(expressionStr)) {
            logExpressionBlank();
        } else {
            final IExpressionSeriesProvider provider = plotConfigurationHelper.getExpressionSeriesProvider();
            try {
                final IExpression expression = provider.parseExpression(expressionStr);
                final MutableReference<SeriesRendererType> rendererType = new MutableReference<SeriesRendererType>(
                        SeriesRendererType.Line);
                final AExpressionVisitor visitor = new ExpressionVisitorSupport() {
                    @Override
                    protected boolean visitLogicalCombination(final BinaryOperation expression) {
                        rendererType.set(SeriesRendererType.Step);
                        return false;
                    }
                };
                visitor.process(expression);
                final String plotPaneId = provider.getPlotPaneId(expression);
                final IPlotSourceDataset dataset = provider.newInstance(plotConfigurationHelper.getChartPanel(),
                        expressionStr, plotPaneId, rendererType.get());
                dataset.setExpressionSeriesProvider(provider);
                dataset.setExpressionSeriesArguments(expressionStr);
                dataset.setSeriesTitle(provider.getTitle(expressionStr));

                layout.lbl_expression.setIcon(ICON_EXPRESSION);
            } catch (final Throwable t) {
                logExpressionException(expressionStr, t);
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