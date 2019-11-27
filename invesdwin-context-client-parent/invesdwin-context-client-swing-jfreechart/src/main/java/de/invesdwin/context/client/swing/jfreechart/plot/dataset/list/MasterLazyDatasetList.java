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
import org.jfree.data.xy.OHLCDataItem;

import com.github.benmanes.caffeine.cache.Caffeine;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.PlotZoomHelper;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.listener.IRangeListener;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.collections.iterable.ICloseableIterator;
import de.invesdwin.util.math.Integers;
import de.invesdwin.util.time.fdate.FDate;

@ThreadSafe
public class MasterLazyDatasetList extends ALazyDatasetList<OHLCDataItem> implements IChartPanelAwareDatasetList {

    public static final OHLCDataItem DUMMY_VALUE = new OHLCDataItem(FDate.MIN_DATE.dateValue(), Double.NaN, Double.NaN,
            Double.NaN, Double.NaN, Double.NaN);
    private static final int PRELOAD_RANGE_MULTIPLIER = 2;
    /*
     * keep a few more items because we need to keep some buffer to the left and right and we don't want to load data on
     * all the smaller panning actions
     */
    private static final int MAX_ITEM_COUNT = PlotZoomHelper.MAX_ZOOM_ITEM_COUNT * 5;
    private static final int TRIM_ITEM_COUNT = PlotZoomHelper.MAX_ZOOM_ITEM_COUNT * 7;
    private final IMasterLazyDatasetProvider provider;
    private final Set<ISlaveLazyDatasetListener> slaveDatasetListeners;
    private final Set<IRangeListener> rangeListeners;
    private final FDate firstAvailableKey;
    private InteractiveChartPanel chartPanel;
    @GuardedBy("this")
    private FDate lastUpdateTime;

    public MasterLazyDatasetList(final IMasterLazyDatasetProvider provider) {
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
        System.out.println(
                "TODO: make master async too, first prefill with NaN, then fill with real values async (doing the slaves at the same time)");
        this.firstAvailableKey = provider.getFirstAvailableKey();
    }

    @Override
    protected OHLCDataItem dummyValue() {
        return DUMMY_VALUE;
    }

    @Override
    public synchronized void setChartPanel(final InteractiveChartPanel chartPanel) {
        this.chartPanel = chartPanel;
        chartPanel.getPlotZoomHelper().getRangeListeners().add(new LimitRangeListenerImpl());
    }

    @Override
    public synchronized void resetRange() {
        final boolean empty = getData().isEmpty();
        if (empty || getLastLoadedKey().isBefore(getResetReferenceTime())) {
            if (!empty) {
                newData();
            }
            loadInitialDataMaster();
            if (!slaveDatasetListeners.isEmpty()) {
                for (final ISlaveLazyDatasetListener slave : slaveDatasetListeners) {
                    slave.loadInitial();
                }
            }
        }
    }

    public synchronized void reloadData() {
        if (getData().isEmpty()) {
            return;
        }
        reloadDataMaster();
        if (!slaveDatasetListeners.isEmpty()) {
            for (final ISlaveLazyDatasetListener slave : slaveDatasetListeners) {
                slave.loadInitial();
            }
        }
    }

    private void reloadDataMaster() {
        final ICloseableIterable<? extends OHLCDataItem> initialValues = provider.getValues(getFirstLoadedKey(),
                getLastLoadedKey());
        final List<OHLCDataItem> data = newData();
        try (ICloseableIterator<? extends OHLCDataItem> it = initialValues.iterator()) {
            while (true) {
                final OHLCDataItem next = it.next();
                data.add(next);
            }
        } catch (final NoSuchElementException e) {
            //end reached
        }
    }

    private FDate getResetReferenceTime() {
        if (lastUpdateTime != null && isTrailing(chartPanel.getDomainAxis().getRange())) {
            return lastUpdateTime;
        } else {
            return provider.getLastAvailableKey();
        }
    }

    public synchronized boolean isTrailing(final Range range) {
        return range.getUpperBound() >= (getData().size() - 1);
    }

    private void loadInitialDataMaster() {
        final int initialVisibleItemCount = chartPanel.getInitialVisibleItemCount() * PRELOAD_RANGE_MULTIPLIER;
        final ICloseableIterable<? extends OHLCDataItem> initialValues = provider
                .getPreviousValues(provider.getLastAvailableKey(), initialVisibleItemCount);
        final List<OHLCDataItem> data = getData();
        try (ICloseableIterator<? extends OHLCDataItem> it = initialValues.iterator()) {
            while (true) {
                final OHLCDataItem next = it.next();
                data.add(next);
            }
        } catch (final NoSuchElementException e) {
            //end reached
        }
    }

    private Range maybeTrimDataRange(final Range range, final MutableBoolean rangeChanged) {
        Range updatedRange = range;
        final List<OHLCDataItem> data = getData();
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
            if (tooManyBefore > 0) {
                for (int i = 0; i < tooManyBefore; i++) {
                    data.remove(0);
                }
                updatedRange = new Range(range.getLowerBound() - tooManyBefore, range.getUpperBound() - tooManyBefore);
                rangeChanged.setTrue();
                if (!slaveDatasetListeners.isEmpty()) {
                    for (final ISlaveLazyDatasetListener slave : slaveDatasetListeners) {
                        slave.removeStart(tooManyBefore);
                    }
                }
            }
            if (tooManyAfter > 0) {
                for (int i = 0; i < tooManyAfter; i++) {
                    data.remove(data.size() - 1);
                }
                if (!slaveDatasetListeners.isEmpty()) {
                    for (final ISlaveLazyDatasetListener slave : slaveDatasetListeners) {
                        slave.removeEnd(tooManyAfter);
                    }
                }
            }
        }
        return updatedRange;
    }

    public synchronized Range maybeLoadDataRange(final Range range, final MutableBoolean rangeChanged) {
        final boolean isTrailing = isTrailing(range);
        Range updatedRange = range;
        final int preloadLowerBound = (int) (range.getLowerBound() - range.getLength());
        final List<OHLCDataItem> data = getData();
        if (preloadLowerBound < 0) {
            final FDate firstLoadedKey = getFirstLoadedKey();
            if (firstAvailableKey.isBefore(firstLoadedKey)) {
                //prepend a whole screen additional to the requested items
                int prependCount = Integers.abs(preloadLowerBound);
                prependCount = prependMaster(prependCount, firstLoadedKey);
                prependSlaves(prependCount);
                updatedRange = new Range(range.getLowerBound() + prependCount, range.getUpperBound() + prependCount);
                rangeChanged.setTrue();
            }
        }
        final int preloadUpperBound = (int) (range.getUpperBound() + range.getLength());
        if (preloadUpperBound > data.size()) {
            final FDate lastLoadedKey = getLastLoadedKey();
            if (provider.getLastAvailableKey().isAfter(lastLoadedKey)) {
                //append a whole screen additional to the requested items
                int appendCount = preloadUpperBound - data.size();
                if (appendCount > 0) {
                    appendCount = appendMaster(appendCount);
                    appendSlaves(appendCount);
                    if (isTrailing) {
                        updatedRange = new Range(range.getLowerBound() + appendCount,
                                range.getUpperBound() + appendCount);
                        rangeChanged.setTrue();
                    }
                }
            }
        }
        return updatedRange;
    }

    private int appendMaster(final int appendCount) {
        //remove at least two elements
        final List<OHLCDataItem> data = getData();
        if (data.size() > 1) {
            data.remove(data.size() - 1);
        }
        final OHLCDataItem lastItemRemoved = data.remove(data.size() - 1);
        final ICloseableIterable<? extends OHLCDataItem> masterPrependValues = provider
                .getNextValues(FDate.valueOf(lastItemRemoved.getDate()), appendCount + 2);
        //remove last two values to replace them
        int countAdded = 0;
        try (ICloseableIterator<? extends OHLCDataItem> it = masterPrependValues.iterator()) {
            while (true) {
                final OHLCDataItem next = it.next();
                if (countAdded == 0) {
                    Assertions.checkEquals(lastItemRemoved.getDate(), next.getDate());
                }
                data.add(next);
                countAdded++;
            }
        } catch (final NoSuchElementException e) {
            //end reached
        }
        return countAdded;
    }

    private void appendSlaves(final int appendCount) {
        if (!slaveDatasetListeners.isEmpty()) {
            for (final ISlaveLazyDatasetListener slave : slaveDatasetListeners) {
                slave.append(appendCount);
            }
        }
    }

    private int prependMaster(final int prependCount, final FDate firstLoadedKey) {
        final List<OHLCDataItem> prependItems = new ArrayList<>(prependCount);
        final ICloseableIterable<? extends OHLCDataItem> masterPrependValues = provider
                .getPreviousValues(firstLoadedKey.addMilliseconds(-1), prependCount);
        try (ICloseableIterator<? extends OHLCDataItem> it = masterPrependValues.iterator()) {
            while (true) {
                final OHLCDataItem next = it.next();
                prependItems.add(next);
            }
        } catch (final NoSuchElementException e) {
            //end reached
        }
        final int prependItemsSize = prependItems.size();
        getData().addAll(0, prependItems);
        return prependItemsSize;
    }

    private void prependSlaves(final int prependCount) {
        if (!slaveDatasetListeners.isEmpty()) {
            for (final ISlaveLazyDatasetListener slave : slaveDatasetListeners) {
                slave.prepend(prependCount);
            }
        }
    }

    public synchronized FDate getFirstLoadedKey() {
        return getLoadedKey(0);
    }

    public synchronized FDate getLastLoadedKey() {
        return getLoadedKey(getData().size() - 1);
    }

    private FDate getLoadedKey(final int i) {
        return FDate.valueOf(getData().get(i).getDate());
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

        //sync slave data with master
        slaveDatasetListener.loadInitial();
    }

    public synchronized void registerRangeListener(final IRangeListener rangeListener) {
        rangeListeners.add(rangeListener);
    }

    public synchronized boolean update(final FDate lastTickTime) {
        final List<OHLCDataItem> data = getData();
        if (data.isEmpty()) {
            resetRange();
            return !data.isEmpty();
        }
        final Range rangeBefore = chartPanel.getDomainAxis().getRange();
        final boolean isTrailing = isTrailing(rangeBefore);
        //remove at least two elements
        int lastItemIndex = Math.max(0, data.size() - 3);
        OHLCDataItem lastItem = data.get(lastItemIndex);
        final ICloseableIterable<? extends OHLCDataItem> history = provider.getValues(new FDate(lastItem.getDate()),
                lastTickTime);
        int appendCount = 0;
        try (ICloseableIterator<? extends OHLCDataItem> it = history.iterator()) {
            while (true) {
                final OHLCDataItem item = it.next();
                if (lastItemIndex < data.size()) {
                    lastItem = data.get(lastItemIndex);
                    if (!item.equals(lastItem)) {
                        data.set(lastItemIndex, item);
                        lastItem = item;
                        appendCount++;
                    }
                } else if (item.getDate().after(lastItem.getDate())) {
                    data.add(item);
                    appendCount++;
                    lastItem = item;
                }
                lastItemIndex++;
            }
        } catch (final NoSuchElementException ex) {
            // end reached
        }
        if (appendCount > 0) {
            /*
             * we need to replace at least the last two elements, otherwise if the slave does not draw incomplete bars,
             * the NaN bar will always be appended without the real value appearing
             */
            appendSlaves(appendCount);
            lastUpdateTime = getLastLoadedKey();
            if (isTrailing) {
                final Range updatedRange = new Range(rangeBefore.getLowerBound() + appendCount,
                        rangeBefore.getUpperBound() + appendCount);
                chartPanel.getDomainAxis().setRange(updatedRange);
            }
            return true;
        } else {
            return false;
        }
    }

}
