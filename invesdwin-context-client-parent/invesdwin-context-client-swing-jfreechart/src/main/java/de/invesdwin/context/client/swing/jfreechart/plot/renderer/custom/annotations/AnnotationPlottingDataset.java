package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.AbstractXYDataset;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.expression.IExpressionSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.indicator.IIndicatorSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.listener.IRangeListener;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.listener.RangeListenerSupport;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IndexedDateTimeOHLCDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.ISlaveLazyDatasetListener;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.MasterLazyDatasetList;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.SlaveLazyDatasetListenerSupport;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.item.AAnnotationPlottingDataItem;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.factory.ILockCollectionFactory;
import de.invesdwin.util.collections.iterable.ASkippingIterable;
import de.invesdwin.util.collections.iterable.ATransformingIterable;
import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.collections.iterable.WrapperCloseableIterable;
import de.invesdwin.util.concurrent.Executors;
import de.invesdwin.util.concurrent.WrappedExecutorService;
import de.invesdwin.util.concurrent.lock.ILock;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.math.expression.IExpression;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.util.time.range.TimeRange;

@NotThreadSafe
public class AnnotationPlottingDataset extends AbstractXYDataset implements IAnnotationPlottingDataset {

    public static final int MAX_ANNOTATIONS = 10_000;
    private static final int TRIM_ANNOTATIONS = 12_000;
    private static final int MAX_REMOVED_ANNOTATIONS = 1_000_000;

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
    private final NavigableSet<AnnotationItem> items = new TreeSet<>();
    @GuardedBy("itemsLock")
    private final Map<String, AnnotationItem> annotationId_item = new HashMap<>();
    private final Set<String> removedAnnotationIds = ILockCollectionFactory.getInstance(true).newSet();
    private IIndicatorSeriesProvider indicatorSeriesProvider;
    private IExpression[] indicatorSeriesArguments;
    private IExpressionSeriesProvider expressionSeriesProvider;
    private String expressionSeriesArguments;
    private final IRangeListener rangeListener;
    private final ISlaveLazyDatasetListener slaveDatasetListener;
    private final WrappedExecutorService executor;

    private long prevFirstLoadedKeyMillis;
    private long prevLastLoadedKeyMillis;

    public AnnotationPlottingDataset(final String seriesKey, final IndexedDateTimeOHLCDataset masterDataset) {
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
            this.executor = Executors
                    .newDisabledExecutor(AnnotationPlottingDataset.class.getSimpleName() + "_DISABLED");
        }
    }

    @Override
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
    public int getDateTimeStartAsItemIndex(final int series, final FDate time) {
        return masterDataset.getDateTimeStartAsItemIndex(series, time);
    }

    @Override
    public int getDateTimeEndAsItemIndex(final int series, final FDate time) {
        return masterDataset.getDateTimeEndAsItemIndex(series, time);
    }

    @Override
    public boolean addOrUpdateOrRemove(final AAnnotationPlottingDataItem annotation) {
        if (removedAnnotationIds.contains(annotation.getAnnotationId())) {
            //ignore obsolete annotation
            return true;
        }
        final long firstLoadedKeyMillis = (long) getXValueAsDateTimeEnd(0, 0);
        final long lastLoadedKeyMillis = (long) getXValueAsDateTimeEnd(0, getItemCount(0) - 1);
        final boolean trailingLoaded = masterDataset.isTrailingLoaded();
        annotation.updateItemLoaded(firstLoadedKeyMillis, lastLoadedKeyMillis, trailingLoaded, this);
        final AnnotationItem item = new AnnotationItem(annotation.getEndTime().millisValue(),
                annotation.getAnnotationId(), annotation);
        itemsLock.lock();
        try {
            final AnnotationItem existing = annotationId_item.put(annotation.getAnnotationId(), item);
            if (existing != null) {
                if (existing.endTimeMillis == item.endTimeMillis) {
                    existing.setAnnotation(annotation);
                } else {
                    items.remove(existing);
                    items.add(item);
                }
            } else {
                items.add(item);
            }
            if (items.size() > TRIM_ANNOTATIONS) {
                final Iterator<AnnotationItem> iterator = items.iterator();
                while (items.size() > MAX_ANNOTATIONS) {
                    final AnnotationItem first = iterator.next();
                    iterator.remove();
                    removedAnnotationIds.add(first.getAnnotationId());
                    annotationId_item.remove(first.getAnnotationId());
                }
                if (removedAnnotationIds.size() > MAX_REMOVED_ANNOTATIONS) {
                    removedAnnotationIds.clear();
                }
            }
        } finally {
            itemsLock.unlock();
        }
        return false;
    }

    @Override
    public AAnnotationPlottingDataItem get(final String annotationId) {
        itemsLock.lock();
        try {
            final AnnotationItem item = annotationId_item.get(annotationId);
            if (item == null) {
                return null;
            }
            return item.getAnnotation();
        } finally {
            itemsLock.unlock();
        }
    }

    @Override
    public void remove(final String annotationId) {
        itemsLock.lock();
        try {
            final AnnotationItem item = annotationId_item.remove(annotationId);
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

    @Override
    public ICloseableIterable<AAnnotationPlottingDataItem> getVisibleItems(final int firstItem, final int lastItem) {
        final long fromMillis = (long) masterDataset.getXValueAsDateTimeStart(0, firstItem);
        final List<AnnotationItem> tail;
        itemsLock.lock();
        try {
            tail = new ArrayList<>(items.tailSet(new AnnotationItem(fromMillis, "", null)));
        } finally {
            itemsLock.unlock();
        }
        final ATransformingIterable<AnnotationItem, AAnnotationPlottingDataItem> transforming = new ATransformingIterable<AnnotationItem, AAnnotationPlottingDataItem>(
                WrapperCloseableIterable.maybeWrap(tail)) {
            @Override
            protected AAnnotationPlottingDataItem transform(final AnnotationItem value) {
                return value.getAnnotation();
            }
        };
        return new ASkippingIterable<AAnnotationPlottingDataItem>(transforming) {
            @Override
            protected boolean skip(final AAnnotationPlottingDataItem element) {
                return !element.isItemLoaded() || element.getStartTimeLoadedIndex() > lastItem
                        || element.getEndTimeLoadedIndex() < firstItem;
            }
        };
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
            for (final AnnotationItem item : items) {
                item.getAnnotation().modifyItemLoadedIndexes(fromIndex, addend);
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
            firstLoadedKeyMillis = (long) getXValueAsDateTimeEnd(0, 0);
        }
        final long lastLoadedKeyMillis = (long) getXValueAsDateTimeEnd(0, getItemCount(0) - 1);
        if (forced || prevFirstLoadedKeyMillis != firstLoadedKeyMillis
                || prevLastLoadedKeyMillis != lastLoadedKeyMillis) {
            final boolean trailingLoaded = masterDataset.isTrailingLoaded();
            itemsLock.lock();
            try {
                final SortedSet<AnnotationItem> tail = items
                        .tailSet(new AnnotationItem(firstLoadedKeyMillis, "", null));
                for (final AnnotationItem item : tail) {
                    item.getAnnotation()
                            .updateItemLoaded(firstLoadedKeyMillis, lastLoadedKeyMillis, trailingLoaded,
                                    AnnotationPlottingDataset.this);
                }
            } finally {
                itemsLock.unlock();
            }
            prevFirstLoadedKeyMillis = firstLoadedKeyMillis;
            prevLastLoadedKeyMillis = lastLoadedKeyMillis;
        }
    }

    private static final class AnnotationItem implements Comparable<AnnotationItem> {
        private final String annotationId;
        private final long endTimeMillis;
        private final int hashCode;
        private AAnnotationPlottingDataItem annotation;

        private AnnotationItem(final long endTimeMillis, final String annotationId,
                final AAnnotationPlottingDataItem annotation) {
            this.annotationId = annotationId;
            this.endTimeMillis = endTimeMillis;
            this.hashCode = Objects.hashCode(endTimeMillis, annotationId);
            this.annotation = annotation;
        }

        public void setAnnotation(final AAnnotationPlottingDataItem annotation) {
            this.annotation = annotation;
        }

        public String getAnnotationId() {
            return annotationId;
        }

        public AAnnotationPlottingDataItem getAnnotation() {
            return annotation;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof AnnotationItem) {
                final AnnotationItem cObj = (AnnotationItem) obj;
                return endTimeMillis == cObj.endTimeMillis && annotationId.equals(cObj.annotationId);
            }
            return false;
        }

        @Override
        public int compareTo(final AnnotationItem o) {
            final int compared = Long.compare(endTimeMillis, o.endTimeMillis);
            if (compared != 0) {
                return compared;
            }
            return annotationId.compareTo(o.annotationId);
        }
    }

}
