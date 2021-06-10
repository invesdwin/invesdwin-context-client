package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jfree.data.Range;

import com.github.benmanes.caffeine.cache.Caffeine;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.PlotZoomHelper;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.listener.IRangeListener;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.item.MasterOHLCDataItem;
import de.invesdwin.context.jfreechart.dataset.TimeRangedOHLCDataItem;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.collections.iterable.ICloseableIterator;
import de.invesdwin.util.concurrent.WrappedExecutorService;
import de.invesdwin.util.concurrent.priority.IPriorityRunnable;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.math.Integers;
import de.invesdwin.util.time.fdate.FDate;

@ThreadSafe
public class MasterLazyDatasetList extends ALazyDatasetList<MasterOHLCDataItem> implements IChartPanelAwareDatasetList {

    private static final int RELOAD_TRAILING_ITEM_COUNT = 100;
    private static final int STEP_ITEM_COUNT_MULTIPLIER = 10;
    private static final int PRELOAD_RANGE_MULTIPLIER = 2;
    /*
     * keep a few more items because we need to keep some buffer to the left and right and we don't want to load data on
     * all the smaller panning actions
     */
    private static final int MAX_STEP_ITEM_COUNT = PlotZoomHelper.MAX_ZOOM_ITEM_COUNT;
    private static final int MAX_ITEM_COUNT = MAX_STEP_ITEM_COUNT * 5;
    private static final int TRIM_ITEM_COUNT = MAX_STEP_ITEM_COUNT * 7;
    private final WrappedExecutorService executor;
    private final WrappedExecutorService loadSlaveItemsExecutor;
    private final IMasterLazyDatasetProvider provider;
    private final Set<ISlaveLazyDatasetListener> slaveDatasetListeners;
    private final Set<IRangeListener> rangeListeners;
    private FDate firstAvailableKeyTo;
    private InteractiveChartPanel chartPanel;
    @GuardedBy("this")
    private FDate lastUpdateTime;
    private volatile int minLowerBound;
    private volatile int maxUpperBound;
    private volatile FDate prevLastAvailableKeyTo;

    public MasterLazyDatasetList(final IMasterLazyDatasetProvider provider, final WrappedExecutorService executor,
            final WrappedExecutorService loadSlaveItemsExecutor) {
        this.provider = provider;

        final ConcurrentMap<ISlaveLazyDatasetListener, Boolean> slaveDatasetListeners = Caffeine.newBuilder()
                .weakKeys()
                .<ISlaveLazyDatasetListener, Boolean> build()
                .asMap();
        this.slaveDatasetListeners = Collections.newSetFromMap(slaveDatasetListeners);
        final ConcurrentMap<IRangeListener, Boolean> limitRangeListeners = Caffeine.newBuilder()
                .weakKeys()
                .<IRangeListener, Boolean> build()
                .asMap();
        this.rangeListeners = Collections.newSetFromMap(limitRangeListeners);
        this.executor = executor;
        this.loadSlaveItemsExecutor = loadSlaveItemsExecutor;
    }

    @Override
    protected MasterOHLCDataItem dummyValue() {
        return MasterOHLCDataItem.DUMMY_VALUE;
    }

    public WrappedExecutorService getExecutor() {
        return executor;
    }

    @Override
    public synchronized void setChartPanel(final InteractiveChartPanel chartPanel) {
        this.chartPanel = chartPanel;
        chartPanel.getPlotZoomHelper().getRangeListeners().add(new LimitRangeListenerImpl());
    }

    @Override
    public synchronized void resetRange() {
        if (getData().isEmpty() || getLastLoadedItem().getEndTime().isBefore(getResetReferenceTime())) {
            newData();
            final InteractiveChartPanel chartPanelCopy = chartPanel;
            final Runnable task = newSyncTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        chartPanelCopy.incrementUpdatingCount(); //prevent flickering
                        try {
                            loadInitialDataMaster(chartPanelCopy);
                            prevLastAvailableKeyTo = null;
                            minLowerBound = 0;
                            maxUpperBound = getData().size() - 1;
                            if (!slaveDatasetListeners.isEmpty()) {
                                for (final ISlaveLazyDatasetListener slave : slaveDatasetListeners) {
                                    slave.loadIinitialItems(false);
                                }
                            }
                        } finally {
                            chartPanelCopy.decrementUpdatingCount();
                        }
                    } catch (final Throwable t) {
                        Err.process(new RuntimeException("Ignoring, chart might have been closed", t));
                    }
                }
            });
            task.run();
            if (!slaveDatasetListeners.isEmpty()) {
                executor.execute(new IPriorityRunnable() {
                    @Override
                    public void run() {
                        try {
                            final List<MasterOHLCDataItem> data = getData();
                            for (int i = 0; i < data.size(); i++) {
                                final FDate key = data.get(i).getEndTime();
                                data.get(i).loadSlaveItems(key);
                            }
                            for (final ISlaveLazyDatasetListener slave : slaveDatasetListeners) {
                                slave.afterLoadItems(true);
                            }
                        } catch (final Throwable t) {
                            Err.process(new RuntimeException("Ignoring, chart might have been closed", t));
                        }
                    }

                    @Override
                    public double getPriority() {
                        return 0;
                    }
                });
            }
        }
    }

    @Override
    public void reloadData() {
        if (getData().isEmpty()) {
            return;
        }
        final FDate from = getFirstLoadedItem().getEndTime();
        final FDate to = getLastLoadedItem().getEndTime();
        reloadData(from, to, null);
    }

    @Override
    public synchronized void reloadData(final FDate from, final FDate to, final Runnable reloadDataFinished) {
        if (getData().isEmpty()) {
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    chartPanel.incrementUpdatingCount(); //prevent flickering
                    try {
                        reloadDataMaster(from, to);
                        prevLastAvailableKeyTo = null;
                        minLowerBound = 0;
                        maxUpperBound = getData().size() - 1;
                        if (!slaveDatasetListeners.isEmpty()) {
                            for (final ISlaveLazyDatasetListener slave : slaveDatasetListeners) {
                                slave.loadIinitialItems(false);
                            }
                        }
                        if (reloadDataFinished != null) {
                            reloadDataFinished.run();
                        }
                    } finally {
                        chartPanel.decrementUpdatingCount();
                    }
                } catch (final Throwable t) {
                    Err.process(new RuntimeException("Ignoring, chart might have been closed", t));
                }
                if (!slaveDatasetListeners.isEmpty()) {
                    executor.execute(new IPriorityRunnable() {
                        @Override
                        public void run() {
                            try {
                                final List<MasterOHLCDataItem> data = getData();
                                for (int i = 0; i < data.size(); i++) {
                                    final FDate key = data.get(i).getEndTime();
                                    data.get(i).loadSlaveItems(key);
                                }
                                for (final ISlaveLazyDatasetListener slave : slaveDatasetListeners) {
                                    slave.afterLoadItems(true);
                                }
                            } catch (final Throwable t) {
                                Err.process(new RuntimeException("Ignoring, chart might have been closed", t));
                            }
                        }

                        @Override
                        public double getPriority() {
                            return 0;
                        }
                    });
                }
            }
        });
    }

    protected Runnable newSyncTask(final Runnable syncTask) {
        return syncTask;
    }

    private void reloadDataMaster(final FDate from, final FDate to) {
        final ICloseableIterable<? extends TimeRangedOHLCDataItem> initialValues = provider.getValues(from, to);
        final List<MasterOHLCDataItem> data = newData();
        try (ICloseableIterator<? extends TimeRangedOHLCDataItem> it = initialValues.iterator()) {
            while (true) {
                final TimeRangedOHLCDataItem next = it.next();
                data.add(new MasterOHLCDataItem(next));
            }
        } catch (final NoSuchElementException e) {
            //end reached
        }
        //append a few more so that trailing is not detected wrong
        final ICloseableIterable<? extends TimeRangedOHLCDataItem> nextValues = provider
                .getNextValues(to.addMilliseconds(1), RELOAD_TRAILING_ITEM_COUNT);
        try (ICloseableIterator<? extends TimeRangedOHLCDataItem> it = nextValues.iterator()) {
            while (true) {
                final TimeRangedOHLCDataItem next = it.next();
                data.add(new MasterOHLCDataItem(next));
            }
        } catch (final NoSuchElementException e) {
            //end reached
        }
    }

    private FDate getResetReferenceTime() {
        if (lastUpdateTime != null && isTrailingRange(chartPanel.getDomainAxis().getRange())) {
            return lastUpdateTime;
        } else {
            return provider.getLastAvailableKeyTo();
        }
    }

    public boolean isTrailingRange(final Range range) {
        return range.getUpperBound() >= (getData().size() - 1);
    }

    private void loadInitialDataMaster(final InteractiveChartPanel chartPanel) {
        final int initialVisibleItemCount = chartPanel.getInitialVisibleItemCount() * PRELOAD_RANGE_MULTIPLIER;
        final FDate lastAvailableKeyTo = provider.getLastAvailableKeyTo();
        if (lastAvailableKeyTo == null) {
            return;
        }
        final ICloseableIterable<? extends TimeRangedOHLCDataItem> initialValues = provider
                .getPreviousValues(lastAvailableKeyTo, initialVisibleItemCount);
        final List<MasterOHLCDataItem> data = getData();
        try (ICloseableIterator<? extends TimeRangedOHLCDataItem> it = initialValues.iterator()) {
            while (true) {
                final TimeRangedOHLCDataItem next = it.next();
                data.add(new MasterOHLCDataItem(next));
            }
        } catch (final NoSuchElementException e) {
            //end reached
        }
    }

    private synchronized Range maybeTrimDataRange(final Range range, final MutableBoolean rangeChanged) {
        Range updatedRange = range;
        final List<MasterOHLCDataItem> data = getData();
        if (data.size() > TRIM_ITEM_COUNT) {
            //trim both ends based on center
            final int centralValueAdj = Integers.max(0, (int) range.getLowerBound())
                    + Integers.min(data.size() - 1, (int) range.getUpperBound()) / 2;
            int tooManyBefore = centralValueAdj - MAX_ITEM_COUNT / 2;
            int tooManyAfter = data.size() - centralValueAdj - MAX_ITEM_COUNT / 2;
            if (tooManyBefore < 0) {
                tooManyAfter += tooManyBefore;
            }
            if (tooManyAfter < 0) {
                tooManyBefore += tooManyAfter;
            }
            if (tooManyBefore > 0 || tooManyAfter > 0) {
                chartPanel.incrementUpdatingCount(); //prevent flickering
                try {
                    if (tooManyBefore > 0) {
                        updatedRange = removeTooManyBefore(range, rangeChanged, data, tooManyBefore);
                    }
                    if (tooManyAfter > 0) {
                        removeTooManyAfter(data, tooManyAfter);
                    }
                    maxUpperBound = data.size() - 1;
                } finally {
                    chartPanel.decrementUpdatingCount();
                }
            }
        }
        return updatedRange;
    }

    private void removeTooManyAfter(final List<MasterOHLCDataItem> data, final int tooManyAfter) {
        chartPanel.incrementUpdatingCount();
        try {
            for (int i = 0; i < tooManyAfter; i++) {
                data.remove(data.size() - 1);
            }
            if (!slaveDatasetListeners.isEmpty()) {
                for (final ISlaveLazyDatasetListener slave : slaveDatasetListeners) {
                    slave.removeEndItems(tooManyAfter);
                }
            }
        } finally {
            chartPanel.decrementUpdatingCount();
        }
    }

    private Range removeTooManyBefore(final Range range, final MutableBoolean rangeChanged,
            final List<MasterOHLCDataItem> data, final int tooManyBefore) {
        chartPanel.incrementUpdatingCount();
        try {
            for (int i = 0; i < tooManyBefore; i++) {
                data.remove(0);
            }
            final Range updatedRange = new Range(range.getLowerBound() - tooManyBefore,
                    range.getUpperBound() - tooManyBefore);
            rangeChanged.setTrue();
            if (!slaveDatasetListeners.isEmpty()) {
                for (final ISlaveLazyDatasetListener slave : slaveDatasetListeners) {
                    slave.removeStartItems(tooManyBefore);
                }
            }
            return updatedRange;
        } finally {
            chartPanel.decrementUpdatingCount();
        }
    }

    public synchronized Range maybeLoadDataRange(final Range range, final MutableBoolean rangeChanged) {
        if (executor.getPendingCount() > 0) {
            //wait for lazy loading to finish
            return range;
        }
        if (getData().isEmpty()) {
            return range;
        }
        final boolean isTrailing = isTrailingRange(range);
        Range updatedRange = range;
        final List<MasterOHLCDataItem> data = getData();
        final int preloadLowerBound = (int) (range.getLowerBound() - range.getLength());
        if (preloadLowerBound < 0) {
            final TimeRangedOHLCDataItem firstLoadedItem = getFirstLoadedItem();
            if (getFirstAvailableKeyTo().isBefore(firstLoadedItem.getEndTime())) {
                //prepend a whole screen additional to the requested items
                final int prependCount = Integers.min(MAX_STEP_ITEM_COUNT,
                        Integers.abs(preloadLowerBound) * STEP_ITEM_COUNT_MULTIPLIER);
                final List<MasterOHLCDataItem> prependItems;
                chartPanel.incrementUpdatingCount(); //prevent flickering
                try {
                    prependItems = prependMaster(prependCount);
                    prependSlaves(prependCount);
                    minLowerBound += prependCount;
                    maxUpperBound += prependCount;
                } finally {
                    chartPanel.decrementUpdatingCount();
                }
                updatedRange = new Range(range.getLowerBound() + prependCount, range.getUpperBound() + prependCount);
                rangeChanged.setTrue();

                final ICloseableIterable<? extends TimeRangedOHLCDataItem> masterPrependValues = provider
                        .getPreviousValues(firstLoadedItem.getEndTime().addMilliseconds(-1), prependCount);
                loadItems(data, prependItems, masterPrependValues);
            }
        }
        //wait for update instead if trailing
        if (!isTrailing) {
            final int preloadUpperBound = (int) (range.getUpperBound() + range.getLength());
            if (preloadUpperBound > data.size()) {
                final TimeRangedOHLCDataItem lastLoadedItem = getLastLoadedItem();
                final FDate lastAvailableKeyTo = provider.getLastAvailableKeyTo();
                if (lastAvailableKeyTo != null && lastAvailableKeyTo.isAfter(lastLoadedItem.getEndTime())
                        && !Objects.equals(prevLastAvailableKeyTo, lastAvailableKeyTo)) {
                    /*
                     * don't check again if the same last available to is used, otherwise an endless loop might occur
                     * because of last loaded item being a millisecond earlier
                     */
                    prevLastAvailableKeyTo = lastAvailableKeyTo;
                    //append a whole screen additional to the requested items
                    final int appendCount = Integers.min(MAX_STEP_ITEM_COUNT,
                            (preloadUpperBound - data.size()) * STEP_ITEM_COUNT_MULTIPLIER);
                    if (appendCount > 0) {
                        final List<MasterOHLCDataItem> appendItems;
                        chartPanel.incrementUpdatingCount(); //prevent flickering
                        try {
                            appendItems = appendMaster(appendCount);
                            appendSlaves(appendCount);
                        } finally {
                            chartPanel.decrementUpdatingCount();
                        }

                        final ICloseableIterable<? extends TimeRangedOHLCDataItem> appendMasterValues = provider
                                .getNextValues(appendItems.get(0).getEndTime(), appendItems.size());
                        loadItems(data, appendItems, appendMasterValues);
                    }
                }
            }
        }
        return updatedRange;
    }

    private FDate getFirstAvailableKeyTo() {
        if (firstAvailableKeyTo == null) {
            firstAvailableKeyTo = provider.getFirstAvailableKeyTo();
        }
        return firstAvailableKeyTo;
    }

    @Override
    public boolean isTrailingLoaded() {
        if (getData().isEmpty()) {
            return false;
        }
        final FDate lastLoadedKeyTo = getLastLoadedItem().getEndTime();
        if (lastLoadedKeyTo == null) {
            return false;
        }
        final FDate lastAvailableKeyTo = provider.getLastAvailableKeyTo();
        if (lastAvailableKeyTo == null) {
            return false;
        }
        return lastAvailableKeyTo.isBeforeOrEqualToNotNullSafe(lastLoadedKeyTo);
    }

    private void loadItems(final List<MasterOHLCDataItem> data, final List<MasterOHLCDataItem> items,
            final ICloseableIterable<? extends TimeRangedOHLCDataItem> masterValues) {
        final double priority = items.get(0).getEndTime().millisValue();
        executor.execute(new IPriorityRunnable() {
            @Override
            public void run() {
                try {
                    int nextItemsIndex = 0;
                    final List<MasterOHLCDataItem> loadedItems = new ArrayList<>();
                    try (ICloseableIterator<? extends TimeRangedOHLCDataItem> it = masterValues.iterator()) {
                        while (true) {
                            final TimeRangedOHLCDataItem next = it.next();
                            final MasterOHLCDataItem appendItem = items.get(nextItemsIndex);
                            appendItem.setOHLC(next);
                            loadedItems.add(appendItem);
                            nextItemsIndex++;
                        }
                    } catch (final NoSuchElementException e) {
                        //end reached
                    }
                    chartPanel.incrementUpdatingCount();
                    try {
                        final int tooManyAfter = items.size() - nextItemsIndex;
                        if (tooManyAfter > 0) {
                            final int removeMasterIndex = data.indexOf(items.get(nextItemsIndex));
                            if (removeMasterIndex >= 0) {
                                synchronized (MasterLazyDatasetList.this) {
                                    for (int i = 0; i < tooManyAfter; i++) {
                                        data.remove(removeMasterIndex);
                                    }
                                    if (!slaveDatasetListeners.isEmpty()) {
                                        for (final ISlaveLazyDatasetListener slave : slaveDatasetListeners) {
                                            slave.removeMiddleItems(removeMasterIndex, tooManyAfter);
                                        }
                                    }
                                }
                            }
                        }
                        if (!slaveDatasetListeners.isEmpty()) {
                            for (final ISlaveLazyDatasetListener slave : slaveDatasetListeners) {
                                slave.afterLoadItems(true);
                            }
                        }
                        minLowerBound = 0;
                        maxUpperBound = data.size() - 1;
                        chartPanel.update();
                    } finally {
                        chartPanel.decrementUpdatingCount();
                    }
                    maybeLoadSlaveItems(priority, loadedItems);
                } catch (final Throwable t) {
                    Err.process(new RuntimeException("Ignoring, chart might have been closed", t));
                }
            }

            @Override
            public double getPriority() {
                return priority;
            }

        });
    }

    private void maybeLoadSlaveItems(final double priority, final List<MasterOHLCDataItem> loadedItems) {
        if (!loadedItems.isEmpty()) {
            loadSlaveItemsExecutor.execute(new IPriorityRunnable() {

                @Override
                public void run() {
                    for (int i = 0; i < loadedItems.size(); i++) {
                        final MasterOHLCDataItem loadedItem = loadedItems.get(i);
                        final FDate key = loadedItem.getEndTime();
                        loadedItem.loadSlaveItems(key);
                    }
                    chartPanel.update();
                }

                @Override
                public double getPriority() {
                    return priority;
                }

            });
        }
    }

    private List<MasterOHLCDataItem> appendMaster(final int appendCount) {
        //invalidate two elements to reload them
        final List<MasterOHLCDataItem> appendItems = new ArrayList<>(appendCount);
        final List<MasterOHLCDataItem> data = getData();
        for (int i = Math.max(0, data.size() - 2); i <= data.size() - 1; i++) {
            final MasterOHLCDataItem existingItem = data.get(i);
            appendItems.add(existingItem);
        }
        final MasterOHLCDataItem lastLoadedItem = data.get(data.size() - 1);
        for (int i = 0; i < appendCount; i++) {
            final MasterOHLCDataItem appendItem = new MasterOHLCDataItem(lastLoadedItem.getStartTime(),
                    lastLoadedItem.getEndTime());
            appendItems.add(appendItem);
            data.add(appendItem);
        }
        return appendItems;
    }

    private void appendSlaves(final int appendCount) {
        if (!slaveDatasetListeners.isEmpty()) {
            for (final ISlaveLazyDatasetListener slave : slaveDatasetListeners) {
                slave.appendItems(appendCount);
            }
        }
    }

    private List<MasterOHLCDataItem> prependMaster(final int prependCount) {
        final List<MasterOHLCDataItem> prependItems = new ArrayList<>(prependCount);
        final List<MasterOHLCDataItem> data = getData();
        final MasterOHLCDataItem firstLoadedItem = data.get(0);
        for (int i = 0; i < prependCount; i++) {
            final MasterOHLCDataItem prependItem = new MasterOHLCDataItem(firstLoadedItem.getStartTime(),
                    firstLoadedItem.getEndTime());
            prependItems.add(prependItem);
        }
        getData().addAll(0, prependItems);
        return prependItems;
    }

    private void prependSlaves(final int prependCount) {
        if (!slaveDatasetListeners.isEmpty()) {
            for (final ISlaveLazyDatasetListener slave : slaveDatasetListeners) {
                slave.prependItems(prependCount);
            }
        }
    }

    public synchronized TimeRangedOHLCDataItem getFirstLoadedItem() {
        return innerGetLoadedItem(0);
    }

    public synchronized TimeRangedOHLCDataItem getLastLoadedItem() {
        return innerGetLoadedItem(getData().size() - 1);
    }

    public synchronized TimeRangedOHLCDataItem getLoadedItem(final int i) {
        return innerGetLoadedItem(Integers.min(i, getData().size() - 1));
    }

    private MasterOHLCDataItem innerGetLoadedItem(final int i) {
        return getData().get(i);
    }

    private final class LimitRangeListenerImpl implements IRangeListener {

        @Override
        public Range beforeLimitRange(final Range range, final MutableBoolean rangeChanged) {
            Range updatedRange = maybeLoadDataRange(range, rangeChanged);
            if (!rangeListeners.isEmpty()) {
                for (final IRangeListener l : rangeListeners) {
                    updatedRange = l.beforeLimitRange(updatedRange, rangeChanged);
                }
            }
            return updatedRange;
        }

        @Override
        public Range afterLimitRange(final Range range, final MutableBoolean rangeChanged) {
            Range updatedRange = maybeTrimDataRange(range, rangeChanged);
            if (!rangeListeners.isEmpty()) {
                for (final IRangeListener l : rangeListeners) {
                    updatedRange = l.afterLimitRange(updatedRange, rangeChanged);
                }
            }
            return updatedRange;
        }

        @Override
        public void onRangeChanged(final Range range) {
            if (!rangeListeners.isEmpty()) {
                for (final IRangeListener l : rangeListeners) {
                    l.onRangeChanged(range);
                }
            }
        }
    }

    public synchronized void registerSlaveDatasetListener(final ISlaveLazyDatasetListener slaveDatasetListener) {
        slaveDatasetListeners.add(slaveDatasetListener);

        executor.execute(new Runnable() { //show task info and load data async
            @Override
            public void run() {
                try {
                    //sync slave data with master
                    slaveDatasetListener.loadIinitialItems(true);
                } catch (final Throwable t) {
                    Err.process(new RuntimeException("Ignoring, chart might have been closed", t));
                }
            }
        });
    }

    public synchronized void registerRangeListener(final IRangeListener rangeListener) {
        rangeListeners.add(rangeListener);
    }

    public synchronized boolean update(final FDate lastTickTime) {
        while (executor.getPendingCount() > 0) {
            //wait for lazy loading to finish
            return false;
        }
        final List<MasterOHLCDataItem> data = getData();
        if (data.isEmpty()) {
            resetRange();
            return !data.isEmpty();
        }
        final Range rangeBefore = chartPanel.getDomainAxis().getRange();
        final boolean isTrailing = isTrailingRange(rangeBefore);
        if (!isTrailing) {
            return false;
        }
        chartPanel.incrementUpdatingCount();
        try {
            return updateTrailingItems(lastTickTime, data, rangeBefore);
        } finally {
            chartPanel.decrementUpdatingCount();
        }
    }

    private boolean updateTrailingItems(final FDate lastTickTime, final List<MasterOHLCDataItem> data,
            final Range rangeBefore) {
        //remove at least two elements
        int lastItemIndex = Math.max(0, data.size() - 3);
        TimeRangedOHLCDataItem lastItem = data.get(lastItemIndex);
        final ICloseableIterable<? extends TimeRangedOHLCDataItem> history = provider.getValues(lastItem.getEndTime(),
                lastTickTime);
        final int firstAppendIndex = lastItemIndex;
        int appendCount = 0;
        int replacedCount = 0;
        try (ICloseableIterator<? extends TimeRangedOHLCDataItem> it = history.iterator()) {
            while (true) {
                final TimeRangedOHLCDataItem item = it.next();
                final MasterOHLCDataItem newItem = new MasterOHLCDataItem(item);
                if (lastItemIndex < data.size()) {
                    lastItem = data.get(lastItemIndex);
                    if (!item.equals(lastItem)) {
                        data.set(lastItemIndex, newItem);
                        lastItem = newItem;
                        replacedCount++;
                    }
                } else if (item.getEndTime().isAfterNotNullSafe(lastItem.getEndTime())) {
                    data.add(newItem);
                    appendCount++;
                    lastItem = newItem;
                }
                lastItemIndex++;
            }
        } catch (final NoSuchElementException ex) {
            // end reached
        }
        if (replacedCount > 0 || appendCount > 0) {
            /*
             * we need to replace at least the last two elements, otherwise if the slave does not draw incomplete bars,
             * the NaN bar will always be appended without the real value appearing
             */
            appendSlaves(appendCount);
            lastUpdateTime = getLastLoadedItem().getEndTime();

            if (appendCount > 0) {
                //trail range
                final Range updatedRange = new Range(rangeBefore.getLowerBound() + appendCount,
                        rangeBefore.getUpperBound() + appendCount);
                chartPanel.getDomainAxis().setRange(updatedRange);
            }

            //load slave items
            //this is actually lastItemIndex+1, so don't use <=
            for (int i = firstAppendIndex; i < lastItemIndex; i++) {
                final MasterOHLCDataItem item = data.get(i);
                item.loadSlaveItems(item.getEndTime());
            }
            if (!slaveDatasetListeners.isEmpty()) {
                for (final ISlaveLazyDatasetListener slave : slaveDatasetListeners) {
                    slave.afterLoadItems(false);
                }
            }
            maxUpperBound = data.size() - 1;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getMinLowerBound() {
        return minLowerBound;
    }

    @Override
    public int getMaxUpperBound() {
        return Integers.min(maxUpperBound, getData().size());
    }

}
