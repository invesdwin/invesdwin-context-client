package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jfree.data.Range;
import org.jfree.data.xy.OHLCDataItem;

import com.github.benmanes.caffeine.cache.Caffeine;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.IRangeListener;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.PlotZoomHelper;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.collections.iterable.ICloseableIterator;
import de.invesdwin.util.math.Integers;
import de.invesdwin.util.time.fdate.FDate;

@ThreadSafe
public class MasterLazyDatasetList extends ALazyDatasetList<OHLCDataItem> implements IChartPanelAwareDatasetList {

    private static final int PRELOAD_RANGE_MULTIPLIER = 2;
    /*
     * keep a few more items because we need to keep some buffer to the left and right and we don't want to load data on
     * all the smaller panning actions
     */
    private static final int MAX_ITEM_COUNT = PlotZoomHelper.MAX_ZOOM_ITEM_COUNT * 5;
    private final IMasterLazyDatasetProvider provider;
    private final Set<ISlaveLazyDatasetListener> slaveDatasetListeners;
    private final Set<IRangeListener> rangeListeners;
    private final FDate firstAvailableKey;
    private InteractiveChartPanel chartPanel;

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

        this.firstAvailableKey = provider.getFirstAvailableKey();
    }

    @Override
    public synchronized void setChartPanel(final InteractiveChartPanel chartPanel) {
        this.chartPanel = chartPanel;
        chartPanel.getPlotZoomHelper().getRangeListeners().add(new LimitRangeListenerImpl());
    }

    @Override
    public synchronized void resetRange() {
        if (data.isEmpty() || getLastLoadedKey().isBefore(provider.getLastAvailableKey())) {
            data = new ArrayList<>(data.size());
            loadInitialDataMaster();
            if (!slaveDatasetListeners.isEmpty()) {
                for (final ISlaveLazyDatasetListener slave : slaveDatasetListeners) {
                    slave.loadInitial();
                }
            }
        }
    }

    private void loadInitialDataMaster() {
        final int initialVisibleItemCount = chartPanel.getInitialVisibleItemCount() * PRELOAD_RANGE_MULTIPLIER;
        final ICloseableIterable<OHLCDataItem> initialValues = provider
                .getPreviousValues(provider.getLastAvailableKey(), initialVisibleItemCount);
        try (ICloseableIterator<OHLCDataItem> it = initialValues.iterator()) {
            while (true) {
                final OHLCDataItem next = it.next();
                data.add(next);
            }
        } catch (final NoSuchElementException e) {
            //end reached
        }
    }

    public Range maybeTrimDataRange(final Range range, final MutableBoolean rangeChanged) {
        Range updatedRange = range;
        if (data.size() > MAX_ITEM_COUNT) {
            //trim both ends based on center
            final int centralValueAdj = (int) range.getCentralValue();
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
        Range updatedRange = range;
        final int preloadLowerBound = (int) (range.getLowerBound() - range.getLength());
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
                }
            }
        }
        return updatedRange;
    }

    private int appendMaster(final int appendCount) {
        data.remove(data.size() - 1);
        final OHLCDataItem lastItemRemoved = data.remove(data.size() - 1);
        final ICloseableIterable<OHLCDataItem> masterPrependValues = provider
                .getNextValues(FDate.valueOf(lastItemRemoved.getDate()), appendCount + 2);
        //remove last two values to replace them
        int countAdded = 0;
        try (ICloseableIterator<OHLCDataItem> it = masterPrependValues.iterator()) {
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
        final ICloseableIterable<OHLCDataItem> masterPrependValues = provider
                .getPreviousValues(firstLoadedKey.addMilliseconds(-1), prependCount);
        try (ICloseableIterator<OHLCDataItem> it = masterPrependValues.iterator()) {
            while (true) {
                final OHLCDataItem next = it.next();
                prependItems.add(next);
            }
        } catch (final NoSuchElementException e) {
            //end reached
        }
        final int prependItemsSize = prependItems.size();
        data.addAll(0, prependItems);
        return prependItemsSize;
    }

    private void prependSlaves(final int prependCount) {
        if (!slaveDatasetListeners.isEmpty()) {
            for (final ISlaveLazyDatasetListener slave : slaveDatasetListeners) {
                slave.prepend(prependCount);
            }
        }
    }

    public FDate getFirstLoadedKey() {
        return FDate.valueOf(data.get(0).getDate());
    }

    public FDate getLastLoadedKey() {
        return FDate.valueOf(data.get(data.size() - 1).getDate());
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

    public boolean update(final FDate lastTickTime) {
        int lastItemIndex = data.size() - 2;
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
             * the NaN bar will always be appended without the real value appearning
             */
            appendSlaves(Integers.max(2, appendCount));
            return true;
        } else {
            return false;
        }
    }

}
