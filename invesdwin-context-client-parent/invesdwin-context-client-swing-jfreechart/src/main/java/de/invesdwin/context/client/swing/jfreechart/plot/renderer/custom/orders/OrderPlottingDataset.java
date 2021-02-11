package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.orders;

import java.util.Map;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.AbstractXYDataset;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.expression.IExpressionSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.indicator.IIndicatorSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.listener.IRangeListener;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.listener.RangeListenerSupport;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IIndexedDateTimeXYDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IndexedDateTimeOHLCDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.ISlaveLazyDatasetListener;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.MasterLazyDatasetList;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.SlaveLazyDatasetListenerSupport;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.factory.ILockCollectionFactory;
import de.invesdwin.util.collections.iterable.ASkippingIterable;
import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.collections.iterable.WrapperCloseableIterable;
import de.invesdwin.util.concurrent.Executors;
import de.invesdwin.util.concurrent.WrappedExecutorService;
import de.invesdwin.util.math.expression.IExpression;
import de.invesdwin.util.time.fdate.FDate;

@NotThreadSafe
public class OrderPlottingDataset extends AbstractXYDataset implements IPlotSourceDataset, IIndexedDateTimeXYDataset {

    public static final int MAX_ORDERS = 10_000;
    private static final int TRIM_ORDERS = 12_000;

    private final String seriesKey;
    private String seriesTitle;
    private final IndexedDateTimeOHLCDataset masterDataset;
    private Integer precision;
    private XYPlot plot;
    private DatasetGroup group;
    private String initialPlotPaneId;
    private String rangeAxisId;
    private final Map<String, OrderPlottingDataItem> orderId_item = ILockCollectionFactory.getInstance(true)
            .newFastIterableLinkedMap();
    private boolean lastTradeProfit = true;
    private IIndicatorSeriesProvider indicatorSeriesProvider;
    private IExpression[] indicatorSeriesArguments;
    private IExpressionSeriesProvider expressionSeriesProvider;
    private String expressionSeriesArguments;
    private final IRangeListener rangeListener;
    private final ISlaveLazyDatasetListener slaveDatasetListener;
    private final WrappedExecutorService executor;

    private long prevFirstLoadedKeyMillis;
    private long prevLastLoadedKeyMillis;

    public OrderPlottingDataset(final String seriesKey, final IndexedDateTimeOHLCDataset masterDataset) {
        Assertions.checkNotNull(seriesKey);
        this.seriesKey = seriesKey;
        this.seriesTitle = seriesKey;
        this.masterDataset = masterDataset;
        if (masterDataset.getData() instanceof MasterLazyDatasetList) {
            final MasterLazyDatasetList master = (MasterLazyDatasetList) masterDataset.getData();
            //keep references to the listeners so they don't get garbage collected due to the weak refrences inside master
            this.rangeListener = new RangeListenerSupport() {
                @Override
                public void onRangeChanged(final Range range) {
                    updateItemsLoaded(false);
                }
            };
            master.registerRangeListener(rangeListener);
            this.slaveDatasetListener = new SlaveLazyDatasetListenerSupport() {
                @Override
                public void afterLoadItems(final boolean async) {
                    updateItemsLoaded(async);
                }

                @Override
                public void prependItems(final int prependCount) {
                    modifyItemLoadedIndexes(0, prependCount);
                }

                @Override
                public void removeStartItems(final int tooManyBefore) {
                    updateItemsLoaded(true);
                }

                @Override
                public void removeMiddleItems(final int index, final int count) {
                    modifyItemLoadedIndexes(index, -count);
                }
            };
            master.registerSlaveDatasetListener(slaveDatasetListener);
            this.executor = master.getExecutor();
        } else {
            this.rangeListener = null;
            this.slaveDatasetListener = null;
            this.executor = Executors.newDisabledExecutor(OrderPlottingDataset.class.getSimpleName() + "_DISABLED");
        }
    }

    public WrappedExecutorService getExecutor() {
        return executor;
    }

    @Override
    public DatasetGroup getGroup() {
        return group;
    }

    @Override
    public void setGroup(final DatasetGroup group) {
        this.group = group;
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
    public int getItemCount(final int series) {
        return masterDataset.getItemCount(series);
    }

    @Override
    public Number getX(final int series, final int item) {
        return masterDataset.getX(series, item);
    }

    @Override
    public double getXValue(final int series, final int item) {
        return masterDataset.getXValue(series, item);
    }

    @Override
    public Number getY(final int series, final int item) {
        //need to return the price here in orger to get axis scaling done properly
        return masterDataset.getY(series, item);
    }

    @Override
    public double getYValue(final int series, final int item) {
        return masterDataset.getYValue(series, item);
    }

    @Override
    public int getSeriesCount() {
        return 1;
    }

    @Override
    public String getSeriesKey(final int series) {
        return seriesKey;
    }

    @Override
    public double getXValueAsDateTimeStart(final int series, final int item) {
        return masterDataset.getXValueAsDateTimeStart(series, item);
    }

    @Override
    public double getXValueAsDateTimeEnd(final int series, final int item) {
        return masterDataset.getXValueAsDateTimeEnd(series, item);
    }

    @Override
    public int getDateTimeEndAsItemIndex(final int series, final FDate time) {
        return masterDataset.getDateTimeEndAsItemIndex(series, time);
    }

    public void addOrUpdate(final OrderPlottingDataItem item) {
        final long firstLoadedKeyMillis = (long) getXValueAsDateTimeEnd(0, 0);
        final long lastLoadedKeyMillis = (long) getXValueAsDateTimeEnd(0, getItemCount(0) - 1);
        final boolean trailingLoaded = masterDataset.isTrailingLoaded();
        item.updateItemLoaded(firstLoadedKeyMillis, lastLoadedKeyMillis, trailingLoaded, this);
        orderId_item.put(item.getOrderId(), item);
        lastTradeProfit = item.isProfit();
        if (orderId_item.size() > TRIM_ORDERS) {
            while (orderId_item.size() > MAX_ORDERS) {
                final String first = orderId_item.keySet().iterator().next();
                orderId_item.remove(first);
            }
        }
    }

    public void remove(final String orderId) {
        orderId_item.remove(orderId);
    }

    @Override
    public boolean isLegendValueVisible(final int series, final int item) {
        return false;
    }

    public ICloseableIterable<OrderPlottingDataItem> getVisibleItems(final int firstItem, final int lastItem) {
        return new ASkippingIterable<OrderPlottingDataItem>(WrapperCloseableIterable.maybeWrap(orderId_item.values())) {
            @Override
            protected boolean skip(final OrderPlottingDataItem element) {
                return !element.isItemLoaded() || element.getOpenTimeLoadedIndex() > lastItem
                        || element.getCloseTimeLoadedIndex() < firstItem;
            }
        };
    }

    public boolean isLastTradeProfit() {
        return lastTradeProfit;
    }

    @Override
    public IndexedDateTimeOHLCDataset getMasterDataset() {
        return masterDataset;
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
    public Number getHigh(final int series, final int item) {
        return masterDataset.getHigh(series, item);
    }

    @Override
    public double getHighValue(final int series, final int item) {
        return masterDataset.getHighValue(series, item);
    }

    @Override
    public Number getLow(final int series, final int item) {
        return masterDataset.getLow(series, item);
    }

    @Override
    public double getLowValue(final int series, final int item) {
        return masterDataset.getLowValue(series, item);
    }

    @Override
    public Number getOpen(final int series, final int item) {
        return masterDataset.getOpen(series, item);
    }

    @Override
    public double getOpenValue(final int series, final int item) {
        return masterDataset.getOpenValue(series, item);
    }

    @Override
    public Number getClose(final int series, final int item) {
        return masterDataset.getClose(series, item);
    }

    @Override
    public double getCloseValue(final int series, final int item) {
        return masterDataset.getCloseValue(series, item);
    }

    @Override
    public Number getVolume(final int series, final int item) {
        return masterDataset.getVolume(series, item);
    }

    @Override
    public double getVolumeValue(final int series, final int item) {
        return masterDataset.getVolumeValue(series, item);
    }

    @Override
    public String getInitialPlotPaneId() {
        return initialPlotPaneId;
    }

    @Override
    public void setInitialPlotPaneId(final String initialPlotPaneId) {
        this.initialPlotPaneId = initialPlotPaneId;
    }

    private void modifyItemLoadedIndexes(final int fromIndex, final int addend) {
        for (final OrderPlottingDataItem dataItem : orderId_item.values()) {
            dataItem.modifyItemLoadedIndexes(fromIndex, addend);
        }
    }

    private void updateItemsLoaded(final boolean forced) {
        final long firstLoadedKeyMillis = (long) getXValueAsDateTimeEnd(0, 0);
        final long lastLoadedKeyMillis = (long) getXValueAsDateTimeEnd(0, getItemCount(0) - 1);
        if (forced || prevFirstLoadedKeyMillis != firstLoadedKeyMillis
                || prevLastLoadedKeyMillis != lastLoadedKeyMillis) {
            final boolean trailingLoaded = masterDataset.isTrailingLoaded();
            for (final OrderPlottingDataItem dataItem : orderId_item.values()) {
                dataItem.updateItemLoaded(firstLoadedKeyMillis, lastLoadedKeyMillis, trailingLoaded,
                        OrderPlottingDataset.this);
            }
            prevFirstLoadedKeyMillis = firstLoadedKeyMillis;
            prevLastLoadedKeyMillis = lastLoadedKeyMillis;
        }
    }

}
