package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.PlotConfigurationHelper;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.SeriesRendererType;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.dialog.expression.ValidatingExpressionData;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.expression.IExpressionSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.indicator.IIndicatorSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.util.collections.loadingcache.ALoadingCache;
import de.invesdwin.util.concurrent.reference.MutableReference;
import de.invesdwin.util.error.Throwables;
import de.invesdwin.util.lang.Strings;
import de.invesdwin.util.math.expression.ExpressionVisitorSupport;
import de.invesdwin.util.math.expression.IExpression;
import de.invesdwin.util.math.expression.eval.IParsedExpression;
import de.invesdwin.util.math.expression.eval.operation.IBinaryOperation;
import de.invesdwin.util.math.expression.eval.operation.Op;
import de.invesdwin.util.math.expression.visitor.ADrawableExpressionVisitor;
import de.invesdwin.util.math.expression.visitor.AExpressionVisitor;
import de.invesdwin.util.math.expression.visitor.ExpressionProperties;
import de.invesdwin.util.swing.Components;
import de.invesdwin.util.swing.Dialogs;
import de.invesdwin.util.swing.listener.DocumentListenerSupport;
import de.invesdwin.util.swing.listener.MouseListenerSupport;
import de.invesdwin.util.swing.listener.MouseMotionListenerSupport;

@NotThreadSafe
public class AddSeriesPanel extends JPanel {

    private static final int MIN_SERIES_PROVIDER_RANK = 1;
    private static final int MIDDLE_SERIES_PROVIDER_RANK = 2;
    private static final int MAX_SERIES_PROVIDER_RANK = 3;

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
                Components.setToolTipText(layout.tbl_indicator, selectedValue.getDescription(), true);
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
                final ALoadingCache<String, Set<String>> duplicateExpressionFilter = new ALoadingCache<String, Set<String>>() {
                    @Override
                    protected Set<String> loadValue(final String key) {
                        return new HashSet<>();
                    }
                };
                final MutableReference<SeriesRendererType> originalRendererType = new MutableReference<SeriesRendererType>(
                        SeriesRendererType.Line);
                final ADrawableExpressionVisitor visitor = new ADrawableExpressionVisitor() {

                    @Override
                    protected void visitOther(final IExpression expression) {
                        final String rangeAxisIdPrefix = "";
                        addExpressionDebug(null, rangeAxisIdPrefix, expression, duplicateExpressionFilter,
                                SeriesRendererType.Line);
                    }

                    @Override
                    protected boolean visitMath(final IBinaryOperation expression) {
                        final String rangeAxisIdPrefix = "";
                        final SeriesRendererType seriesType;
                        if (expression.getOp() == Op.NOT) {
                            seriesType = SeriesRendererType.Step;
                        } else {
                            seriesType = SeriesRendererType.Line;
                        }
                        addExpressionDebug(null, rangeAxisIdPrefix, expression, duplicateExpressionFilter, seriesType);
                        return false;
                    }

                    @Override
                    protected boolean visitComparison(final IBinaryOperation expression) {
                        final String plotPaneId = provider.getPlotPaneId(expression);
                        final String rangeAxisIdPrefixLeftRight = "X: ";
                        final IParsedExpression left = expression.getLeft();
                        addExpressionDebug(plotPaneId, rangeAxisIdPrefixLeftRight, left, duplicateExpressionFilter,
                                SeriesRendererType.Line);
                        final IParsedExpression right = expression.getRight();
                        addExpressionDebug(plotPaneId, rangeAxisIdPrefixLeftRight, right, duplicateExpressionFilter,
                                SeriesRendererType.Line);
                        addExpressionDebug(plotPaneId, "", expression, duplicateExpressionFilter,
                                SeriesRendererType.Step);
                        return false;
                    }

                    @Override
                    protected boolean visitLogicalCombination(final IBinaryOperation expression) {
                        originalRendererType.set(SeriesRendererType.Step);
                        return true;
                    }

                };
                visitor.process(originalExpression);
                final String originalRangeAxisIdPrefix = "";
                addExpressionDebug(null, originalRangeAxisIdPrefix, originalExpression, duplicateExpressionFilter,
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
        if (provider.shouldValidateExpression(expression)) {
            ValidatingExpressionData.validate(provider, lbl_expression, expression);
        } else {
            lbl_expression.setIcon(ValidatingExpressionData.ICON_EXPRESSION);
            Components.setToolTipText(lbl_expression, null, false);
        }
    }

    public static String prepareErrorMessageForTooltip(final Throwable t) {
        String message = Throwables.concatMessagesShort(t);
        message = Strings.wrap(message, ValidatingExpressionData.TOOLTIP_WORD_WRAP_LIMIT);
        message = message.replace("\n", "\n  ");
        message = Strings.escapeHtml4(message);
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

    private IPlotSourceDataset addExpressionDebug(final String plotPaneId, final String rangeAxisIdPrefix,
            final IExpression expression, final ALoadingCache<String, Set<String>> duplicateExpressionFilter,
            final SeriesRendererType rendererType) {
        final IExpression drawable = ExpressionProperties.getDrawable(expression);
        if (drawable != null) {
            final String expressionStr = drawable.toString();
            try {
                final IExpressionSeriesProvider provider = plotConfigurationHelper.getExpressionSeriesProvider();
                final String usedPlotPaneId;
                if (plotPaneId == null) {
                    usedPlotPaneId = provider.getPlotPaneId(expression);
                } else {
                    usedPlotPaneId = plotPaneId;
                }
                final String expressionTitle = provider.getTitle(expressionStr);
                if (duplicateExpressionFilter.get(usedPlotPaneId).add(expressionTitle)) {
                    return null;
                }
                final IPlotSourceDataset dataset = provider.newInstance(plotConfigurationHelper.getChartPanel(),
                        expressionStr, usedPlotPaneId, rendererType);
                dataset.setExpressionSeriesProvider(provider);
                dataset.setExpressionSeriesArguments(expressionStr);
                dataset.setSeriesTitle(expressionTitle);
                final String rangeAxisId = rangeAxisIdPrefix + usedPlotPaneId;
                dataset.setRangeAxisId(rangeAxisId);
                return dataset;
            } catch (final Throwable t) {
                logExpressionException(expressionStr, t);
                return null;
            }
        } else {
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
                    protected boolean visitLogicalCombination(final IBinaryOperation expression) {
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
                final String title = provider.getTitle(expressionStr);
                dataset.setSeriesTitle(title);
                dataset.setRangeAxisId(plotPaneId);

                layout.lbl_expression.setIcon(ValidatingExpressionData.ICON_EXPRESSION);
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
            final ALoadingCache<Integer, List<IIndicatorSeriesProvider>> rank_seriesProviders = new ALoadingCache<Integer, List<IIndicatorSeriesProvider>>() {
                @Override
                protected List<IIndicatorSeriesProvider> loadValue(final Integer key) {
                    return new ArrayList<>();
                }
            };
            for (final IIndicatorSeriesProvider seriesProvider : seriesProviders) {
                final Integer rank = matches(seriesProvider, searchString, searchPattern);
                if (rank != null) {
                    rank_seriesProviders.get(rank).add(seriesProvider);
                }
            }
            for (int i = MIN_SERIES_PROVIDER_RANK; i <= MAX_SERIES_PROVIDER_RANK; i++) {
                final List<IIndicatorSeriesProvider> seriesProviers = rank_seriesProviders.get(i);
                for (final IIndicatorSeriesProvider seriesProvider : seriesProviers) {
                    model.addRow(new Object[] { seriesProvider.getName(),
                            seriesProvider.getExpressionString(seriesProvider.getDefaultValues()) });
                }
            }
        }
        return model;
    }

    private Integer matches(final IIndicatorSeriesProvider seriesProvider, final String searchString,
            final Pattern searchPattern) {
        if (matches(searchString, searchPattern, seriesProvider.getExpressionName())) {
            return MIN_SERIES_PROVIDER_RANK;
        } else if (matches(searchString, searchPattern, seriesProvider.getName())) {
            return MIDDLE_SERIES_PROVIDER_RANK;
        } else if (matches(searchString, searchPattern, seriesProvider.getDescription())) {
            return MAX_SERIES_PROVIDER_RANK;
        }
        return null;
    }

    private boolean matches(final String searchString, final Pattern searchPattern, final String value) {
        return Strings.isNotBlank(value)
                && (Strings.containsIgnoreCase(value, searchString) || searchPattern.matcher(value).matches());
    }
}