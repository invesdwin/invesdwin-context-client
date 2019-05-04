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
import de.invesdwin.context.client.swing.jfreechart.panel.helper.ILimitRangeListener;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.PlotZoomHelper;
import de.invesdwin.context.jfreechart.dataset.XYDataItemOHLC;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.collections.iterable.ICloseableIterator;
import de.invesdwin.util.collections.loadingcache.historical.query.IHistoricalCacheQuery;
import de.invesdwin.util.math.Integers;
import de.invesdwin.util.time.fdate.FDate;

@ThreadSafe
public class MasterLazyDatasetList extends ALazyDatasetList<OHLCDataItem> implements IChartPanelAwareDatasetList {

    private static final int PRELOAD_RANGE_MULTIPLIER = 2;
    private static final int MAX_ITEM_COUNT = PlotZoomHelper.MAX_ZOOM_ITEM_COUNT * 5;
    private final IMasterLazyDatasetProvider provider;
    private final IHistoricalCacheQuery<OHLCDataItem> providerQuery;
    private final Set<SlaveLazyDatasetList> slaves;
    private final FDate firstAvailableKey;

    public MasterLazyDatasetList(final IMasterLazyDatasetProvider provider) {
        this.provider = provider;
        this.providerQuery = provider.query();

        final ConcurrentMap<SlaveLazyDatasetList, Boolean> map = Caffeine.newBuilder()
                .weakKeys()
                .<SlaveLazyDatasetList, Boolean> build()
                .asMap();
        slaves = Collections.newSetFromMap(map);

        this.firstAvailableKey = provider.getFirstAvailableKey();
    }

    @Override
    public synchronized void setChartPanel(final InteractiveChartPanel chartPanel) {
        chartPanel.getPlotZoomHelper().getLimitRangeListeners().add(new LimitRangeListenerImpl());
        loadInitialData(chartPanel);
    }

    private void loadInitialData(final InteractiveChartPanel chartPanel) {
        final int initialVisibleItemCount = chartPanel.getInitialVisibleItemCount() * PRELOAD_RANGE_MULTIPLIER;
        final ICloseableIterable<OHLCDataItem> initialValues = providerQuery
                .getPreviousValues(provider.getLastAvailableKey(), initialVisibleItemCount);
        try (ICloseableIterator<OHLCDataItem> it = initialValues.iterator()) {
            while (true) {
                final OHLCDataItem next = it.next();
                list.add(next);
            }
        } catch (final NoSuchElementException e) {
            //end reached
        }
    }

    public Range maybeTrimDataRange(final Range range, final MutableBoolean rangeChanged) {
        Range updatedRange = range;
        if (list.size() > MAX_ITEM_COUNT) {
            //trim both ends based on center
            final int centralValueAdj = (int) range.getCentralValue();
            int tooManyBefore = centralValueAdj - MAX_ITEM_COUNT / 2;
            int tooManyAfter = list.size() - centralValueAdj - MAX_ITEM_COUNT / 2;
            if (tooManyBefore < 0) {
                //remove leading
                tooManyAfter -= Integers.abs(tooManyBefore);
            }
            if (tooManyAfter < 0) {
                tooManyBefore -= Integers.abs(tooManyAfter);
            }
            if (tooManyBefore > 0) {
                for (int i = 0; i < tooManyBefore; i++) {
                    list.remove(0);
                }
                updatedRange = new Range(range.getLowerBound() - tooManyBefore, range.getUpperBound() - tooManyBefore);
                rangeChanged.setTrue();
                for (final SlaveLazyDatasetList slave : slaves) {
                    final List<XYDataItemOHLC> slaveList = slave.getList();
                    for (int i = 0; i < tooManyBefore; i++) {
                        slaveList.remove(0);
                    }
                    Assertions.checkEquals(list.size(), slaveList.size());
                }
            }
            if (tooManyAfter > 0) {
                for (int i = 0; i < tooManyAfter; i++) {
                    list.remove(list.size() - 1);
                }
                for (final SlaveLazyDatasetList slave : slaves) {
                    final List<XYDataItemOHLC> slaveList = slave.getList();
                    for (int i = 0; i < tooManyAfter; i++) {
                        slaveList.remove(slaveList.size() - 1);
                    }
                    Assertions.checkEquals(list.size(), slaveList.size());
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
        if (preloadUpperBound > list.size()) {
            final FDate lastLoadedKey = getLastLoadedKey();
            if (provider.getLastAvailableKey().isAfter(lastLoadedKey)) {
                //append a whole screen additional to the requested items
                int appendCount = preloadUpperBound - list.size();
                if (appendCount > 0) {
                    appendCount = appendMaster(appendCount);
                    appendSlaves(appendCount);
                }
            }
        }
        return updatedRange;
    }

    private int appendMaster(final int appendCount) {
        list.remove(list.size() - 1);
        final OHLCDataItem lastItemRemoved = list.remove(list.size() - 1);
        final ICloseableIterable<OHLCDataItem> masterPrependValues = providerQuery.withFuture()
                .getNextValues(FDate.valueOf(lastItemRemoved.getDate()), appendCount + 2);
        //remove last two values to replace them
        int countAdded = 0;
        try (ICloseableIterator<OHLCDataItem> it = masterPrependValues.iterator()) {
            while (true) {
                final OHLCDataItem next = it.next();
                if (countAdded == 0) {
                    Assertions.checkEquals(lastItemRemoved.getDate(), next.getDate());
                }
                list.add(next);
                countAdded++;
            }
        } catch (final NoSuchElementException e) {
            //end reached
        }
        return countAdded;
    }

    private void appendSlaves(final int appendCount) {
        for (final SlaveLazyDatasetList slave : slaves) {
            final ISlaveLazyDatasetProvider slaveProvider = slave.getProvider();
            final int masterSizeAfter = list.size();
            final int masterSizeBefore = masterSizeAfter - appendCount;
            final int fromIndex = masterSizeBefore;
            final int toIndex = masterSizeAfter - 1;
            final List<XYDataItemOHLC> slaveList = slave.getList();
            XYDataItemOHLC lastItemRemoved = null;
            while (slaveList.size() > masterSizeBefore) {
                lastItemRemoved = slaveList.remove(slaveList.size() - 1);
            }
            int countAdded = 0;
            for (int i = fromIndex; i <= toIndex; i++) {
                final FDate key = FDate.valueOf(list.get(i).getDate());
                final XYDataItemOHLC next = slaveProvider.getValue(key);
                if (countAdded == 0) {
                    Assertions.checkEquals(lastItemRemoved.asOHLC().getDate(), next.asOHLC().getDate());
                }
                slaveList.add(next);
                countAdded++;
            }
            Assertions.checkEquals(countAdded, appendCount);
            Assertions.checkEquals(masterSizeAfter, slaveList.size());
        }
    }

    private int prependMaster(final int prependCount, final FDate firstLoadedKey) {
        final List<OHLCDataItem> prependItems = new ArrayList<>(prependCount);
        final ICloseableIterable<OHLCDataItem> masterPrependValues = providerQuery
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
        list.addAll(0, prependItems);
        return prependItemsSize;
    }

    private void prependSlaves(final int prependCount) {
        for (final SlaveLazyDatasetList slave : slaves) {
            final List<XYDataItemOHLC> prependItems = new ArrayList<>(prependCount);
            final ISlaveLazyDatasetProvider slaveProvider = slave.getProvider();
            for (int i = 0; i < prependCount; i++) {
                final FDate key = FDate.valueOf(list.get(i).getDate());
                final XYDataItemOHLC value = slaveProvider.getValue(key);
                prependItems.add(value);
            }
            final List<XYDataItemOHLC> slaveList = slave.getList();
            slaveList.addAll(0, prependItems);
            Assertions.checkEquals(list.size(), slaveList.size());
        }
    }

    private FDate getFirstLoadedKey() {
        return FDate.valueOf(list.get(0).getDate());
    }

    private FDate getLastLoadedKey() {
        return FDate.valueOf(list.get(list.size() - 1).getDate());
    }

    private final class LimitRangeListenerImpl implements ILimitRangeListener {
        @Override
        public Range beforeLimitRange(final Range range, final MutableBoolean rangeChanged) {
            return maybeLoadDataRange(range, rangeChanged);
        }

        @Override
        public Range afterLimitRange(final Range range, final MutableBoolean rangeChanged) {
            return maybeTrimDataRange(range, rangeChanged);
        }
    }

    public synchronized void registerSlave(final SlaveLazyDatasetList slave) {
        slaves.add(slave);

        //sync slave data with master
        final ISlaveLazyDatasetProvider slaveProvider = slave.getProvider();
        final List<XYDataItemOHLC> slaveList = slave.getList();
        for (int i = 0; i < list.size(); i++) {
            final FDate key = FDate.valueOf(list.get(i).getDate());
            final XYDataItemOHLC value = slaveProvider.getValue(key);
            slaveList.add(value);
        }
    }

}