package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.orders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYRangeInfo;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
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
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.AnnotationPlottingDataset;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.factory.ILockCollectionFactory;
import de.invesdwin.util.collections.iterable.ATransformingIterable;
import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.collections.iterable.WrapperCloseableIterable;
import de.invesdwin.util.collections.iterable.skip.ASkippingIterable;
import de.invesdwin.util.concurrent.Executors;
import de.invesdwin.util.concurrent.WrappedExecutorService;
import de.invesdwin.util.concurrent.lock.ILock;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.math.expression.IExpression;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.util.time.date.FDates;
import de.invesdwin.util.time.range.TimeRange;

@NotThreadSafe
public class OrderPlottingDataset extends AbstractXYDataset
        implements IPlotSourceDataset, IIndexedDateTimeXYDataset, XYRangeInfo {

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
    private final ILock itemsLock = ILockCollectionFactory.getInstance(true)
            .newLock(AnnotationPlottingDataset.class.getSimpleName() + "_itemsLock");
    @GuardedBy("itemsLock")
    private final NavigableSet<OrderItem> items = new TreeSet<>();
    @GuardedBy("itemsLock")
    private final Map<String, OrderItem> orderId_item = new HashMap<>();
    private final boolean lastTradeProfit = true;
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
                    final InteractiveChartPanel chartPanel = master.getChartPanel();
                    final TimeRange visibleTimeRange = chartPanel.getTimeRange(range);
                    updateItemsLoaded(false, visibleTimeRange);
                }
            };
            master.registerRangeListener(rangeListener);
            this.slaveDatasetListener = new SlaveLazyDatasetListenerSupport() {
                @Override
                public void afterLoadItems(final boolean async) {
                    final InteractiveChartPanel chartPanel = master.getChartPanel();
                    final TimeRange visibleTimeRange = chartPanel.getVisibleTimeRange();
                    updateItemsLoaded(async, visibleTimeRange);
                }

                @Override
                public void prependItems(final int prependCount) {
                    modifyItemLoadedIndexes(0, prependCount);
                }

                @Override
                public void removeStartItems(final int tooManyBefore) {
                    final InteractiveChartPanel chartPanel = master.getChartPanel();
                    final TimeRange visibleTimeRange = chartPanel.getVisibleTimeRange();
                    updateItemsLoaded(true, visibleTimeRange);
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
    public Range getRangeBounds(final List visibleSeriesKeys, final Range xRange, final boolean includeInterval) {
        //skip this dataset since master is only relevant anyway
        return null;
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
    public int getDateTimeStartAsItemIndex(final int series, final FDate time) {
        return masterDataset.getDateTimeStartAsItemIndex(series, time);
    }

    @Override
    public int getDateTimeEndAsItemIndex(final int series, final FDate time) {
        return masterDataset.getDateTimeEndAsItemIndex(series, time);
    }

    public void addOrUpdate(final OrderPlottingDataItem order) {
        //we need to search for start time, otherwise entries will be plotted one bar too early
        final long firstLoadedKeyMillis = (long) getXValueAsDateTimeStart(0, 0);
        final long lastLoadedKeyMillis = (long) getXValueAsDateTimeStart(0, getItemCount(0) - 1);
        final boolean trailingLoaded = masterDataset.isTrailingLoaded();
        order.updateItemLoaded(firstLoadedKeyMillis, lastLoadedKeyMillis, trailingLoaded, this);
        FDate closeTime = order.getCloseTime();
        if (closeTime == null) {
            closeTime = FDates.MAX_DATE;
        }
        final OrderItem item = new OrderItem(closeTime.millisValue(), order.getOrderId(), order);
        itemsLock.lock();
        try {
            final OrderItem existing = orderId_item.get(order.getOrderId());
            if (existing != null) {
                if (existing.endTimeMillis == item.endTimeMillis) {
                    existing.setOrder(order);
                } else {
                    items.remove(existing);
                    items.add(item);
                    orderId_item.put(order.getOrderId(), item);
                }
            } else {
                items.add(item);
                orderId_item.put(order.getOrderId(), item);
            }
            if (items.size() > TRIM_ORDERS) {
                final Iterator<OrderItem> iterator = items.iterator();
                while (items.size() > MAX_ORDERS) {
                    final OrderItem first = iterator.next();
                    iterator.remove();
                    orderId_item.remove(first.getOrderId());
                }
            }
        } finally {
            itemsLock.unlock();
        }
    }

    public OrderPlottingDataItem get(final String orderId) {
        itemsLock.lock();
        try {
            final OrderItem item = orderId_item.get(orderId);
            if (item == null) {
                return null;
            }
            return item.getOrder();
        } finally {
            itemsLock.unlock();
        }
    }

    public void remove(final String orderId) {
        itemsLock.lock();
        try {
            final OrderItem item = orderId_item.remove(orderId);
            if (item == null) {
                return;
            }
            items.remove(item);
        } finally {
            itemsLock.unlock();
        }
    }

    @Override
    public boolean isLegendValueVisible(final int series, final int item) {
        return false;
    }

    @GuardedBy("itemsLock")
    public ICloseableIterable<OrderPlottingDataItem> getVisibleItems(final int firstItem, final int lastItem) {
        final long fromMillis = (long) masterDataset.getXValueAsDateTimeStart(0, firstItem);
        final List<OrderItem> tail;
        itemsLock.lock();
        try {
            tail = new ArrayList<>(items.tailSet(new OrderItem(fromMillis, "", null)));
        } finally {
            itemsLock.unlock();
        }
        final ATransformingIterable<OrderItem, OrderPlottingDataItem> transforming = new ATransformingIterable<OrderItem, OrderPlottingDataItem>(
                WrapperCloseableIterable.maybeWrap(tail)) {
            @Override
            protected OrderPlottingDataItem transform(final OrderItem value) {
                return value.getOrder();
            }
        };
        return new ASkippingIterable<OrderPlottingDataItem>(transforming) {
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
        itemsLock.lock();
        try {
            for (final OrderItem item : items) {
                item.getOrder().modifyItemLoadedIndexes(fromIndex, addend);
            }
        } finally {
            itemsLock.unlock();
        }
    }

    private void updateItemsLoaded(final boolean forced, final TimeRange visibleTimeRange) {
        //we need to search for start time, otherwise entries will be plotted one bar too early
        final long firstLoadedKeyMillis;
        if (visibleTimeRange != null && visibleTimeRange.getFrom() != null) {
            firstLoadedKeyMillis = visibleTimeRange.getFrom().millisValue();
        } else {
            firstLoadedKeyMillis = (long) getXValueAsDateTimeStart(0, 0);
        }
        final long lastLoadedKeyMillis = (long) getXValueAsDateTimeStart(0, getItemCount(0) - 1);
        if (forced || prevFirstLoadedKeyMillis != firstLoadedKeyMillis
                || prevLastLoadedKeyMillis != lastLoadedKeyMillis) {
            final boolean trailingLoaded = masterDataset.isTrailingLoaded();
            itemsLock.lock();
            try {
                final SortedSet<OrderItem> tail = items.tailSet(new OrderItem(firstLoadedKeyMillis, "", null));
                for (final OrderItem item : tail) {
                    item.getOrder()
                            .updateItemLoaded(firstLoadedKeyMillis, lastLoadedKeyMillis, trailingLoaded,
                                    OrderPlottingDataset.this);
                }
            } finally {
                itemsLock.unlock();
            }
            prevFirstLoadedKeyMillis = firstLoadedKeyMillis;
            prevLastLoadedKeyMillis = lastLoadedKeyMillis;
        }
    }

    private static final class OrderItem implements Comparable<OrderItem> {
        private final String orderId;
        private final long endTimeMillis;
        private final int hashCode;
        private OrderPlottingDataItem order;

        private OrderItem(final long endTimeMillis, final String orderId, final OrderPlottingDataItem order) {
            this.orderId = orderId;
            this.endTimeMillis = endTimeMillis;
            this.hashCode = Objects.hashCode(endTimeMillis, orderId);
            this.order = order;
        }

        public void setOrder(final OrderPlottingDataItem order) {
            this.order = order;
        }

        public String getOrderId() {
            return orderId;
        }

        public OrderPlottingDataItem getOrder() {
            return order;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof OrderItem) {
                final OrderItem cObj = (OrderItem) obj;
                return endTimeMillis == cObj.endTimeMillis && orderId.equals(cObj.orderId);
            }
            return false;
        }

        @Override
        public int compareTo(final OrderItem o) {
            final int compared = Long.compare(endTimeMillis, o.endTimeMillis);
            if (compared != 0) {
                return compared;
            }
            return orderId.compareTo(o.orderId);
        }
    }

}
