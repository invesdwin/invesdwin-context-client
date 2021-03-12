package de.invesdwin.context.client.swing.jfreechart.panel.helper.legend;

import java.awt.Font;
import java.awt.Paint;
import java.text.NumberFormat;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.plot.XYPlots;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.math.Doubles;

@NotThreadSafe
public class HighlightableLegendTitle extends CustomLegendTitle {

    private static final Font LEGEND_FONT = XYPlots.DEFAULT_FONT;
    private static final Font HIGHLIGHTED_LEGEND_FONT = LEGEND_FONT.deriveFont(Font.BOLD);

    private final InteractiveChartPanel chartPanel;

    public HighlightableLegendTitle(final InteractiveChartPanel chartPanel, final LegendItemSource source) {
        super(source);
        this.chartPanel = chartPanel;
    }

    @Override
    protected String newLabel(final LegendItem item) {
        int domainMarkerItem = (int) chartPanel.getPlotCrosshairHelper().getDomainCrosshairMarkerValue();
        if (domainMarkerItem == -1) {
            domainMarkerItem = chartPanel.getMasterDataset().getData().size() - 1;
        }
        if (domainMarkerItem >= 0) {
            final IPlotSourceDataset dataset = (IPlotSourceDataset) item.getDataset();
            final String label = dataset.getSeriesTitle();
            if (label == null) {
                throw new NullPointerException(
                        "seriesTitle should not be null for seriesKey: " + dataset.getSeriesId());
            }
            final int series = item.getSeriesIndex();
            final int lastItem = dataset.getItemCount(series) - 1;
            if (domainMarkerItem > lastItem) {
                domainMarkerItem = lastItem;
            }
            if (!dataset.isLegendValueVisible(series, domainMarkerItem)) {
                return label;
            }
            final XYPlot plot = dataset.getPlot();
            if (plot == chartPanel.getCombinedPlot().getTrashPlot()) {
                return label;
            }
            final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxisForDataset(item.getDatasetIndex());
            if (rangeAxis != null) {
                final NumberFormat rangeAxisFormat = rangeAxis.getNumberFormatOverride();
                return newLabelString(domainMarkerItem, dataset, label, series, rangeAxisFormat);
            } else {
                return super.newLabel(item);
            }
        } else {
            return super.newLabel(item);
        }
    }

    private String newLabelString(final int domainMarkerItem, final IPlotSourceDataset dataset, final String label,
            final int series, final NumberFormat rangeAxisFormat) {
        final XYPlot plot = dataset.getPlot();
        final int datasetindex = XYPlots.getDatasetIndexForDataset(plot, dataset, false);
        final XYItemRenderer renderer = plot.getRenderer(datasetindex);
        if (dataset instanceof OHLCDataset && dataset == chartPanel.getMasterDataset()) {
            final OHLCDataset ohlc = dataset;
            final StringBuilder sb = new StringBuilder(label);
            if (renderer.getDefaultSeriesVisibleInLegend() || renderer.getDefaultItemLabelsVisible()
                    || renderer.isSeriesItemLabelsVisible(series)) {
                final double open = ohlc.getOpenValue(series, domainMarkerItem);
                final double high = ohlc.getHighValue(series, domainMarkerItem);
                final double low = ohlc.getLowValue(series, domainMarkerItem);
                final double close = ohlc.getCloseValue(series, domainMarkerItem);
                if (Doubles.isNaN(open) && Doubles.isNaN(high) && Doubles.isNaN(low) && Doubles.isNaN(close)) {
                    return sb.toString();
                }
                if (open == high && high == low && low == close) {
                    sb.append(" ");
                    //this is bar flat, show it as that
                    sb.append(rangeAxisFormat.format(close));
                } else {
                    sb.append(" O:");
                    sb.append(rangeAxisFormat.format(open));
                    sb.append(" H:");
                    sb.append(rangeAxisFormat.format(high));
                    sb.append(" L:");
                    sb.append(rangeAxisFormat.format(low));
                    sb.append(" C:");
                    sb.append(rangeAxisFormat.format(close));
                }
                sb.append(" T:");
                sb.append(chartPanel.getDomainAxisFormat().formatFromTo(domainMarkerItem));
            }
            return sb.toString();
        } else {
            final StringBuilder sb = new StringBuilder(label);
            if (renderer.getDefaultSeriesVisibleInLegend()) {
                sb.append(" ");
                sb.append(rangeAxisFormat.format(dataset.getYValue(series, domainMarkerItem)));
            } else {
                for (int i = series; i < dataset.getSeriesCount(); i++) {
                    //show values grouped that are selected to be shown, first series is already visible in legend, otherwise this function would not have been called
                    if (i > series && renderer.isSeriesVisibleInLegend(i)) {
                        /*
                         * though a new visible series will stop the grouping
                         * 
                         * though this is normally a misconfiguration, since groupings should happen only once for all
                         * series in a dataset/renderer
                         * 
                         * but we don't want to duplicate the information in that case
                         */
                        break;
                    }
                    if (renderer.getDefaultItemLabelsVisible() || renderer.isSeriesItemLabelsVisible(i)) {
                        sb.append(" ");
                        sb.append(rangeAxisFormat.format(dataset.getYValue(i, domainMarkerItem)));
                    }
                }
            }
            return sb.toString();
        }
    }

    @Override
    protected Font newTextFont(final LegendItem item, final Font textFont) {
        final HighlightedLegendInfo highlightedLegendInfo = chartPanel.getPlotLegendHelper().getHighlightedLegendInfo();
        if (highlightedLegendInfo != null && highlightedLegendInfo.getDatasetIndex() == item.getDatasetIndex()) {
            final IPlotSourceDataset plotSource = (IPlotSourceDataset) item.getDataset();
            if (highlightedLegendInfo.getPlot() == plotSource.getPlot()) {
                return HIGHLIGHTED_LEGEND_FONT;
            }
        }
        return LEGEND_FONT;
    }

    @Override
    protected Paint newFillPaint(final LegendItem item) {
        int domainMarkerItem = (int) chartPanel.getPlotCrosshairHelper().getDomainCrosshairMarkerValue();
        if (domainMarkerItem == -1) {
            domainMarkerItem = chartPanel.getMasterDataset().getData().size() - 1;
        }
        if (domainMarkerItem >= 0) {
            final IPlotSourceDataset dataset = (IPlotSourceDataset) item.getDataset();
            final int datasetIndex = item.getDatasetIndex();
            final XYItemRenderer renderer = dataset.getPlot().getRenderer(datasetIndex);
            if (renderer == null) {
                return super.newFillPaint(item);
            } else if (dataset instanceof OHLCDataset) {
                final OHLCDataset ohlc = dataset;
                if (domainMarkerItem >= ohlc.getItemCount(0)) {
                    domainMarkerItem = ohlc.getItemCount(0) - 1;
                }
                return renderer.getItemPaint(0, domainMarkerItem);
            } else if (dataset instanceof XYDataset) {
                final XYDataset xy = dataset;
                if (domainMarkerItem >= xy.getItemCount(0)) {
                    domainMarkerItem = xy.getItemCount(0) - 1;
                }
                return renderer.getItemPaint(0, domainMarkerItem);
            } else {
                throw UnknownArgumentException.newInstance(Class.class, dataset.getClass());
            }
        } else {
            return super.newFillPaint(item);
        }
    }
}