package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

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
import de.invesdwin.context.jfreechart.dataset.XYDataItemOHLC;
import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.collections.iterable.ICloseableIterator;
import de.invesdwin.util.time.fdate.FDate;

@ThreadSafe
public class MasterLazyDatasetList extends ALazyDatasetList<OHLCDataItem> implements IChartPanelAwareDatasetList {

    private static final int PRELOAD_RANGE_MULTIPLIER = 2;
    private final IMasterLazyDatasetProvider provider;
    private final Set<SlaveLazyDatasetList> slaves;

    public MasterLazyDatasetList(final IMasterLazyDatasetProvider provider) {
        this.provider = provider;

        final ConcurrentMap<SlaveLazyDatasetList, Boolean> map = Caffeine.newBuilder()
                .weakKeys()
                .<SlaveLazyDatasetList, Boolean> build()
                .asMap();
        slaves = Collections.newSetFromMap(map);

    }

    @Override
    public synchronized void setChartPanel(final InteractiveChartPanel chartPanel) {
        chartPanel.getPlotZoomHelper().getLimitRangeListeners().add(new LimitRangeListenerImpl());
        loadInitialData(chartPanel);
    }

    private void loadInitialData(final InteractiveChartPanel chartPanel) {
        final int initialVisibleItemCount = chartPanel.getInitialVisibleItemCount() * PRELOAD_RANGE_MULTIPLIER;
        final ICloseableIterable<OHLCDataItem> initialValues = provider.query()
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

    public synchronized void maybeLoadDataRange(final Range range) {

    }

    private final class LimitRangeListenerImpl implements ILimitRangeListener {
        @Override
        public Range beforeLimitRange(final Range range, final MutableBoolean rangeChanged) {
            maybeLoadDataRange(range);
            return range;
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
