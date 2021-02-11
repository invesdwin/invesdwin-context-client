package de.invesdwin.context.client.swing.jfreechart.plot.dataset;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.plot.XYPlot;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.expression.IExpressionSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.indicator.IIndicatorSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.IChartPanelAwareDatasetList;
import de.invesdwin.context.jfreechart.dataset.ListOHLCDataset;
import de.invesdwin.context.jfreechart.dataset.TimeRangedOHLCDataItem;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.math.Integers;
import de.invesdwin.util.math.expression.IExpression;
import de.invesdwin.util.time.fdate.FDate;

@NotThreadSafe
public class IndexedDateTimeOHLCDataset extends ListOHLCDataset
        implements IIndexedDateTimeXYDataset, IPlotSourceDataset {

    private XYPlot plot;
    private Integer precision;
    private String rangeAxisId;
    private String initialPlotPaneId;
    private String seriesTitle;
    private IIndicatorSeriesProvider indicatorSeriesProvider;
    private IExpression[] indicatorSeriesArguments;
    private IExpressionSeriesProvider expressionSeriesProvider;
    private String expressionSeriesArguments;

    public IndexedDateTimeOHLCDataset(final String seriesKey, final List<? extends TimeRangedOHLCDataItem> data) {
        super(seriesKey, data);
        Assertions.checkNotNull(seriesKey);
        this.seriesTitle = seriesKey;
    }

    @Override
    public Number getX(final int series, final int item) {
        return getXValue(series, item);
    }

    @Override
    public double getXValue(final int series, final int item) {
        if (item < 0 || item >= getItemCount(series)) {
            return Double.NaN;
        }
        return item;
    }

    @Override
    public double getXValueAsDateTimeStart(final int series, final int item) {
        final int itemCount = getItemCount(series);
        if (itemCount == 0) {
            return Double.NaN;
        }
        final int usedItem = Integers.between(item, 0, itemCount - 1);
        return getData().get(usedItem).getStartTime().millisValue();
    }

    @Override
    public double getXValueAsDateTimeEnd(final int series, final int item) {
        final int itemCount = getItemCount(series);
        if (itemCount == 0) {
            return Double.NaN;
        }
        final int usedItem = Integers.between(item, 0, itemCount - 1);
        return getData().get(usedItem).getStartTime().millisValue();
    }

    public boolean isTrailingLoaded() {
        final List<? extends TimeRangedOHLCDataItem> data = getData();
        if (data instanceof IChartPanelAwareDatasetList) {
            final IChartPanelAwareDatasetList cData = (IChartPanelAwareDatasetList) data;
            return cData.isTrailingLoaded();
        } else {
            //we are always fully loaded if we don't have a lazy dataset
            return true;
        }
    }

    @Override
    public int getDateTimeEndAsItemIndex(final int series, final FDate time) {
        return bisectDateTimeEnd(getData(), time);
    }

    public static int bisectDateTimeEnd(final List<? extends TimeRangedOHLCDataItem> keys, final FDate skippingKeysAbove) {
        int lo = 0;
        int hi = keys.size();
        while (lo < hi) {
            final int mid = (lo + hi) / 2;
            //if (x < list.get(mid)) {
            final FDate midKey = keys.get(mid).getEndTime();
            final int compareTo = midKey.compareTo(skippingKeysAbove);
            switch (compareTo) {
            case -1:
                lo = mid + 1;
                break;
            case 0:
                return mid;
            case 1:
                hi = mid;
                break;
            default:
                throw UnknownArgumentException.newInstance(Integer.class, compareTo);
            }
        }
        if (lo <= 0) {
            return 0;
        }
        if (lo >= keys.size()) {
            lo = lo - 1;
        }
        final FDate loTime = keys.get(lo).getEndTime();
        if (loTime.isAfterNotNullSafe(skippingKeysAbove)) {
            final int index = lo - 1;
            return index;
        } else {
            return lo;
        }
    }

    @Override
    public XYPlot getPlot() {
        return plot;
    }

    @Override
    public void setPlot(final XYPlot plot) {
        this.plot = plot;
    }

    @Override
    public Integer getPrecision() {
        return precision;
    }

    @Override
    public void setPrecision(final Integer precision) {
        this.precision = precision;
    }

    @Override
    public String getRangeAxisId() {
        return rangeAxisId;
    }

    @Override
    public void setRangeAxisId(final String rangeAxisId) {
        this.rangeAxisId = rangeAxisId;
    }

    @Override
    public boolean isLegendValueVisible(final int series, final int item) {
        return true;
    }

    @Override
    public String getSeriesTitle() {
        return seriesTitle;
    }

    @Override
    public void setSeriesTitle(final String seriesTitle) {
        this.seriesTitle = seriesTitle;
    }

    @Override
    public IIndicatorSeriesProvider getIndicatorSeriesProvider() {
        return indicatorSeriesProvider;
    }

    @Override
    public void setIndicatorSeriesProvider(final IIndicatorSeriesProvider indicatorSeriesProvider) {
        this.indicatorSeriesProvider = indicatorSeriesProvider;
    }

    @Override
    public void setIndicatorSeriesArguments(final IExpression[] indicatorSeriesArguments) {
        this.indicatorSeriesArguments = indicatorSeriesArguments;
    }

    @Override
    public IExpression[] getIndicatorSeriesArguments() {
        return indicatorSeriesArguments;
    }

    @Override
    public IExpressionSeriesProvider getExpressionSeriesProvider() {
        return expressionSeriesProvider;
    }

    @Override
    public void setExpressionSeriesProvider(final IExpressionSeriesProvider expressionSeriesProvider) {
        this.expressionSeriesProvider = expressionSeriesProvider;
    }

    @Override
    public String getExpressionSeriesArguments() {
        return expressionSeriesArguments;
    }

    @Override
    public void setExpressionSeriesArguments(final String expressionSeriesArguments) {
        this.expressionSeriesArguments = expressionSeriesArguments;
    }

    @Override
    public String getInitialPlotPaneId() {
        return initialPlotPaneId;
    }

    @Override
    public void setInitialPlotPaneId(final String initialPlotPaneId) {
        this.initialPlotPaneId = initialPlotPaneId;
    }

    @Override
    public IndexedDateTimeOHLCDataset getMasterDataset() {
        return this;
    }

}