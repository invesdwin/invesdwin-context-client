package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.indicator;

import javax.annotation.concurrent.Immutable;

import org.jfree.chart.plot.XYPlot;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.basis.CustomCombinedDomainXYPlot;
import de.invesdwin.context.client.swing.jfreechart.plot.XYPlots;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IndexedDateTimeOHLCDataset;
import de.invesdwin.util.math.expression.IExpression;

@Immutable
public class MasterDatasetIndicatorSeriesProvider implements IIndicatorSeriesProvider {

    public static final String PLOT_PANE_ID_PRICE = "Price";
    private final InteractiveChartPanel chartPanel;

    public MasterDatasetIndicatorSeriesProvider(final InteractiveChartPanel chartPanel) {
        this.chartPanel = chartPanel;
    }

    @Override
    public IPlotSourceDataset newInstance(final InteractiveChartPanel chartPanel, final IExpression[] args) {
        return addIntrumentSeries(chartPanel);
    }

    @Override
    public void modifyDataset(final InteractiveChartPanel chartPanel, final IPlotSourceDataset dataset,
            final IExpression[] args) {
        throw new UnsupportedOperationException(
                "No parameters available, thus cannot modify dataset according to them.");
    }

    @Override
    public String getPlotPaneId() {
        return PLOT_PANE_ID_PRICE;
    }

    @Override
    public IIndicatorSeriesParameter[] getParameters() {
        return NO_PARAMETERS;
    }

    @Override
    public String getName() {
        return "#Instrument(" + chartPanel.getMasterDataset().getSeriesTitle() + ")";
    }

    @Override
    public String getSeriesTitle(final String expression) {
        return chartPanel.getMasterDataset().getSeriesTitle();
    }

    @Override
    public String getExpressionName() {
        return null;
    }

    @Override
    public String getDescription() {
        return "";
    }

    public static IPlotSourceDataset addIntrumentSeries(final InteractiveChartPanel chartPanel) {
        final IndexedDateTimeOHLCDataset masterDataset = chartPanel.getMasterDataset();
        if (masterDataset.getPlot() != null) {
            //already exists
            return masterDataset;
        }

        XYPlot plot = XYPlots.getPlotWithRangeAxisId(chartPanel, masterDataset.getRangeAxisId());
        if (plot == null) {
            plot = chartPanel.newPlot();
        }

        final int datasetIndex = XYPlots.getFreeDatasetIndex(plot);
        plot.setDataset(datasetIndex, masterDataset);
        plot.setRenderer(datasetIndex,
                chartPanel.getPlotConfigurationHelper().getPriceInitialSettings().getPriceRenderer());
        XYPlots.updateRangeAxes(plot);
        masterDataset.setPlot(plot);

        if (!chartPanel.getCombinedPlot().isSubplotVisible(plot)) {
            chartPanel.getCombinedPlot().add(plot, CustomCombinedDomainXYPlot.INITIAL_PLOT_WEIGHT);
        }

        chartPanel.update();

        return chartPanel.getMasterDataset();
    }
}