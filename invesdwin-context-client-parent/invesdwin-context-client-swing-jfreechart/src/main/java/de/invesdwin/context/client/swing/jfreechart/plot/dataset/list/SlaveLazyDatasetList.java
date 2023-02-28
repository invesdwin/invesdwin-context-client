package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.item.MasterOHLCDataItem;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.item.SlaveXYDataItemOHLC;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.util.collections.loadingcache.historical.query.error.ResetCacheException;
import de.invesdwin.util.concurrent.priority.IPriorityRunnable;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.time.date.FDate;

@ThreadSafe
public class SlaveLazyDatasetList extends ALazyDatasetList<SlaveXYDataItemOHLC> implements ISlaveLazyDatasetListener {

    private final ISlaveLazyDatasetProvider provider;
    private final MasterLazyDatasetList master;

    public SlaveLazyDatasetList(final InteractiveChartPanel chartPanel, final ISlaveLazyDatasetProvider provider) {
        this.provider = provider;
        this.master = (MasterLazyDatasetList) chartPanel.getMasterDataset().getData();
        master.registerSlaveDatasetListener(this);
    }

    @Override
    protected SlaveXYDataItemOHLC dummyValue() {
        return SlaveXYDataItemOHLC.DUMMY_VALUE;
    }

    @Override
    public void maybeUpdateOnIdleAppendItems() {
        //noop
    }

    @Override
    public synchronized void appendItems(final int appendCount) {
        //invalidate two elements to reload them
        final List<SlaveXYDataItemOHLC> data = getData();
        for (int i = Math.max(0, data.size() - 2); i <= data.size() - 1; i++) {
            final SlaveXYDataItemOHLC prevItem = data.get(i);
            final SlaveXYDataItemOHLC replaced = new SlaveXYDataItemOHLC(provider, prevItem.getOHLC());
            data.set(i, replaced);
            prevItem.invalidate();
            final MasterOHLCDataItem masterItem = master.get(i);
            masterItem.addSlaveItem(replaced);
        }
        final int fromIndex = data.size();
        final int toIndex = master.size() - 1;
        for (int i = fromIndex; i <= toIndex; i++) {
            final SlaveXYDataItemOHLC slaveItem = new SlaveXYDataItemOHLC(provider);
            data.add(slaveItem);
            final MasterOHLCDataItem masterItem = master.get(i);
            masterItem.addSlaveItem(slaveItem);
        }
        assertSameSizeAsMaster();
    }

    private void removeAndInvalidateItem(final List<SlaveXYDataItemOHLC> data, final int i) {
        final SlaveXYDataItemOHLC removed = data.remove(i);
        removed.invalidate();
    }

    @Override
    public synchronized void prependItems(final int prependCount) {
        final List<SlaveXYDataItemOHLC> slavePrependItems = new ArrayList<>(prependCount);
        for (int i = 0; i < prependCount; i++) {
            final SlaveXYDataItemOHLC slaveItem = new SlaveXYDataItemOHLC(provider);
            slavePrependItems.add(slaveItem);
            final MasterOHLCDataItem masterItem = master.get(i);
            masterItem.addSlaveItem(slaveItem);
        }
        getData().addAll(0, slavePrependItems);
        assertSameSizeAsMaster();
    }

    private void assertSameSizeAsMaster() {
        final int masterSize = master.size();
        final List<SlaveXYDataItemOHLC> data = getData();
        if (data.size() != masterSize) {
            Err.process(new ResetCacheException("slave.size [" + data.size() + "] should be equal to master.size ["
                    + masterSize + "]. Reloading: " + provider));
            loadIinitialItems(true);
        }
    }

    @Override
    public synchronized void loadIinitialItems(final boolean eager) {
        List<SlaveXYDataItemOHLC> data = getData();
        for (int i = 0; i < data.size(); i++) {
            removeAndInvalidateItem(data, i);
        }
        data = newData();
        for (int i = 0; i < master.size(); i++) {
            final SlaveXYDataItemOHLC slaveItem = new SlaveXYDataItemOHLC(provider);
            data.add(slaveItem);
            if (!eager) {
                final MasterOHLCDataItem masterItem = master.get(i);
                masterItem.addSlaveItem(slaveItem);
            }
        }
        assertSameSizeAsMaster();
        if (eager) {
            master.getExecutor().execute(new IPriorityRunnable() {
                @Override
                public void run() {
                    try {
                        final List<SlaveXYDataItemOHLC> data = getData();
                        for (int i = 0; i < master.size() && i < data.size(); i++) {
                            final MasterOHLCDataItem masterItem = master.get(i);
                            final FDate key = masterItem.getEndTime();
                            final SlaveXYDataItemOHLC slaveItem = data.get(i);
                            slaveItem.loadValue(key);
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

    @Override
    public synchronized void removeStartItems(final int tooManyBefore) {
        final List<SlaveXYDataItemOHLC> data = getData();
        for (int i = 0; i < tooManyBefore; i++) {
            removeAndInvalidateItem(data, 0);
        }
        assertSameSizeAsMaster();
    }

    @Override
    public synchronized void removeEndItems(final int tooManyAfter) {
        final List<SlaveXYDataItemOHLC> data = getData();
        for (int i = 0; i < tooManyAfter; i++) {
            removeAndInvalidateItem(data, data.size() - 1);
        }
        assertSameSizeAsMaster();
    }

    @Override
    public synchronized void removeMiddleItems(final int index, final int count) {
        final List<SlaveXYDataItemOHLC> data = getData();
        for (int i = 0; i < count && data.size() > index; i++) {
            data.remove(index);
        }
        assertSameSizeAsMaster();
    }

    @Override
    public void afterLoadItems(final boolean async) {
        //noop
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(SlaveLazyDatasetList.class).with(provider).toString();
    }

}
