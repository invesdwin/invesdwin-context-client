package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.dialog.indicator;

import java.awt.FlowLayout;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.PlotConfigurationHelper;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.dialog.ISettingsPanelActions;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.dialog.indicator.modifier.IParameterSettingsModifier;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.AddSeriesPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.indicator.IIndicatorSeriesParameter;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.indicator.IIndicatorSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.legend.HighlightedLegendInfo;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.error.Throwables;
import de.invesdwin.util.math.expression.IExpression;
import de.invesdwin.util.swing.Dialogs;

@NotThreadSafe
public class IndicatorSettingsPanel extends JPanel implements ISettingsPanelActions {

    private static final org.slf4j.ext.XLogger LOG = org.slf4j.ext.XLoggerFactory.getXLogger(AddSeriesPanel.class);

    private final PlotConfigurationHelper plotConfigurationHelper;
    private final IPlotSourceDataset dataset;
    private final IExpression[] seriesArgumentsBefore;
    private final HighlightedLegendInfo highlighted;

    private final IndicatorSettingsPanelLayout layout;
    private boolean notifyModification = false;

    public IndicatorSettingsPanel(final PlotConfigurationHelper plotConfigurationHelper,
            final HighlightedLegendInfo highlighted, final JDialog dialog) {
        Assertions.checkFalse(highlighted.isPriceSeries());
        Assertions.checkNotNull(highlighted.getDataset().getIndicatorSeriesProvider());

        final FlowLayout flowLayout = (FlowLayout) getLayout();
        flowLayout.setVgap(0);
        setBorder(new CompoundBorder(
                new TitledBorder(null, "Indicator", TitledBorder.LEADING, TitledBorder.TOP, null, null),
                new EmptyBorder(0, 5, 5, 5)));

        this.plotConfigurationHelper = plotConfigurationHelper;
        this.highlighted = highlighted;
        this.dataset = highlighted.getDataset();
        final IIndicatorSeriesParameter[] parameters = dataset.getIndicatorSeriesProvider().getParameters();
        final IParameterSettingsModifier[] modifiers = new IParameterSettingsModifier[parameters.length];
        final IExpression[] args = dataset.getIndicatorSeriesArguments();
        final Runnable modificationListener = new Runnable() {
            @Override
            public void run() {
                if (notifyModification) {
                    ok();
                }
            }
        };
        for (int i = 0; i < parameters.length; i++) {
            final IIndicatorSeriesParameter parameter = parameters[i];
            final IParameterSettingsModifier modifier = parameter.newModifier(modificationListener);
            modifier.setValue(args[i]);
            modifiers[i] = modifier;
        }
        this.seriesArgumentsBefore = args;
        this.layout = new IndicatorSettingsPanelLayout(modifiers);
        add(layout);
        notifyModification = true;
    }

    @Override
    public void reset() {
        final IExpression[] seriesArgumentsInitial = plotConfigurationHelper.getSeriesInitialSettings(highlighted)
                .getIndicatorSeriesArguments();
        setModifierValues(seriesArgumentsInitial);
        if (hasChanges(seriesArgumentsInitial, dataset.getIndicatorSeriesArguments())) {
            apply(seriesArgumentsInitial);
        }
    }

    @Override
    public void cancel() {
        if (hasChanges(seriesArgumentsBefore, dataset.getIndicatorSeriesArguments())) {
            apply(seriesArgumentsBefore);
        }
    }

    @Override
    public void ok() {
        final IExpression[] newSeriesArguments = newSeriesArguments();
        if (hasChanges(dataset.getIndicatorSeriesArguments(), newSeriesArguments)) {
            apply(newSeriesArguments);
        }
    }

    public IExpression[] newSeriesArguments() {
        final IExpression[] args = new IExpression[seriesArgumentsBefore.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = layout.modifiers[i].getValue();
        }
        return args;
    }

    public void apply(final IExpression[] arguments) {
        final IIndicatorSeriesProvider indicatorSeriesProvider = dataset.getIndicatorSeriesProvider();
        final String toExpression = indicatorSeriesProvider.getExpressionString(arguments);
        try {
            indicatorSeriesProvider.modifyDataset(plotConfigurationHelper.getChartPanel(), dataset, arguments);
            dataset.setIndicatorSeriesArguments(arguments);
            dataset.setSeriesTitle(indicatorSeriesProvider.getSeriesTitle(toExpression));
        } catch (final Throwable t) {
            final String fromExpression = dataset.getSeriesTitle();
            LOG.warn("Error modifying series [" + indicatorSeriesProvider.getName() + "] expression from ["
                    + fromExpression + "] to [" + toExpression + "]:\n" + Throwables.getFullStackTrace(t));
            Dialogs.showMessageDialog(this, "<html><b>Name:</b><br><pre>  " + indicatorSeriesProvider.getName()
                    + "</pre><b>Valid Before:</b><br><pre>  " + fromExpression
                    + "</pre><b>Invalid After:</b><br><pre>  " + toExpression + "</pre><br><b>Error:</b><br><pre>  "
                    + AddSeriesPanel.prepareErrorMessageForTooltip(t) + "</pre>", "Invalid Expression",
                    Dialogs.ERROR_MESSAGE);

            final IExpression[] seriesArgumentsValid = dataset.getIndicatorSeriesArguments();
            setModifierValues(seriesArgumentsValid);
        }
    }

    private void setModifierValues(final IExpression[] values) {
        notifyModification = false;
        try {
            for (int i = 0; i < values.length; i++) {
                layout.modifiers[i].setValue(values[i]);
            }
        } finally {
            notifyModification = true;
        }
    }

    private boolean hasChanges(final IExpression[] arguments1, final IExpression[] arguments2) {
        for (int i = 0; i < arguments1.length; i++) {
            if (arguments1[i].newEvaluateDouble().evaluateDouble() != arguments2[i].newEvaluateDouble()
                    .evaluateDouble()) {
                return true;
            }
        }
        return false;
    }

}
