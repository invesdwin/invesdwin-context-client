package de.invesdwin.context.client.swing.jfreechart.panel.helper.legend;

import javax.annotation.concurrent.Immutable;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.plot.XYPlots;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.IPriceLineRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.DisabledXYDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.DisabledXYItemRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.IDatasetSourceXYItemRenderer;
import de.invesdwin.util.assertions.Assertions;

@Immutable
public class HighlightedLegendInfo {

    private final InteractiveChartPanel chartPanel;
    private final int subplotIndex;
    private final XYPlot plot;
    private final CustomLegendTitle title;
    private final int datasetIndex;

    public HighlightedLegendInfo(final InteractiveChartPanel chartPanel, final int subplotIndex, final XYPlot plot,
            final CustomLegendTitle title, final int datasetIndex) {
        this.chartPanel = chartPanel;
        this.subplotIndex = subplotIndex;
        this.plot = plot;
        this.title = title;
        this.datasetIndex = datasetIndex;
    }

    public InteractiveChartPanel getChartPanel() {
        return chartPanel;
    }

    public int getSubplotIndex() {
        return subplotIndex;
    }

    public XYPlot getPlot() {
        return plot;
    }

    public CustomLegendTitle getTitle() {
        return title;
    }

    public int getDatasetIndex() {
        return datasetIndex;
    }

    public String getSeriesId() {
        final IPlotSourceDataset dataset = getDataset();
        return getSeriesId(dataset);
    }

    private String getSeriesId(final IPlotSourceDataset dataset) {
        return String.valueOf(dataset.getSeriesId());
    }

    public String getSeriesTitle() {
        return getDataset().getSeriesTitle();
    }

    public boolean isPriceSeries() {
        return chartPanel.getMasterDataset() == DisabledXYDataset.maybeUnwrap(getDataset());
    }

    public IPlotSourceDataset getDataset() {
        return getDataset(datasetIndex);
    }

    private IPlotSourceDataset getDataset(final int datasetIndex) {
        return (IPlotSourceDataset) plot.getDataset(datasetIndex);
    }

    public IDatasetSourceXYItemRenderer getRenderer() {
        return (IDatasetSourceXYItemRenderer) plot.getRenderer(datasetIndex);
    }

    public void setRenderer(final IDatasetSourceXYItemRenderer renderer) {
        plot.setRenderer(datasetIndex, renderer);
    }

    public boolean isDatasetVisible() {
        return !(getRenderer() instanceof DisabledXYItemRenderer);
    }

    public void setDatasetVisible(final boolean visible) {
        chartPanel.getPlotLegendHelper().setDatasetVisible(plot, datasetIndex, visible);
    }

    public boolean isRemovable() {
        final IPlotSourceDataset dataset = getDataset();
        return isRemovable(dataset);
    }

    private boolean isRemovable(final IPlotSourceDataset dataset) {
        return chartPanel.getPlotLegendHelper().isDatasetRemovable(dataset);
    }

    public void removeSeries() {
        removeSeries(datasetIndex);
        afterRemoveSeries();
    }

    private void removeSeries(final int datasetIndex) {
        final IPlotSourceDataset dataset = getDataset(datasetIndex);
        Assertions.checkTrue(isRemovable(dataset));
        chartPanel.getPlotConfigurationHelper().removeInitialSeriesSettings(getSeriesId(dataset));
        dataset.close();
        XYPlots.removeDataset(plot, datasetIndex);
    }

    private void afterRemoveSeries() {
        XYPlots.updateRangeAxes(chartPanel.getTheme(), plot);
        chartPanel.getPlotLegendHelper().removeEmptyPlotsAndResetTrashPlot();
    }

    public void removeAllSeries() {
        boolean removed = false;
        for (int i = 0; i < plot.getDatasetCount(); i++) {
            final IPlotSourceDataset dataset = getDataset(i);
            if (dataset != null && isRemovable(dataset)) {
                removeSeries(i);
                removed = true;
                i--;
            }
        }
        if (removed) {
            afterRemoveSeries();
        }
    }

    public void setPriceLineVisible(final boolean visible) {
        final IPriceLineRenderer renderer = (IPriceLineRenderer) getRenderer();
        renderer.setPriceLineVisible(visible);
    }

    public boolean isPriceLineVisible() {
        final XYItemRenderer renderer = getRenderer();
        return isPriceLineVisible(renderer);
    }

    public static boolean isPriceLineVisible(final XYItemRenderer renderer) {
        if (renderer instanceof IPriceLineRenderer) {
            final IPriceLineRenderer cRenderer = (IPriceLineRenderer) renderer;
            return cRenderer.isPriceLineVisible();
        } else {
            return false;
        }
    }

    public void setPriceLabelVisible(final boolean visible) {
        final IPriceLineRenderer renderer = (IPriceLineRenderer) getRenderer();
        renderer.setPriceLabelVisible(visible);
    }

    public boolean isPriceLabelVisible() {
        final XYItemRenderer renderer = getRenderer();
        return isPriceLabelVisible(renderer);
    }

    public static boolean isPriceLabelVisible(final XYItemRenderer renderer) {
        if (renderer instanceof IPriceLineRenderer) {
            final IPriceLineRenderer cRenderer = (IPriceLineRenderer) renderer;
            return cRenderer.isPriceLabelVisible();
        } else {
            return false;
        }
    }

}
