package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.ThreadSafe;

import org.jfree.data.xy.OHLCDataItem;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.jfreechart.dataset.XYDataItemOHLC;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.time.fdate.FDate;

@ThreadSafe
public class SlaveLazyDatasetList extends ALazyDatasetList<XYDataItemOHLC> implements ISlaveLazyDatasetListener {

    public static final XYDataItemOHLC DUMMY_VALUE = new XYDataItemOHLC(MasterLazyDatasetList.DUMMY_VALUE) {
        @Override
        public void setOHLC(final OHLCDataItem ohlc) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setY(final double y) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setY(final Number y) {
            throw new UnsupportedOperationException();
        }
    };

    private final ISlaveLazyDatasetProvider provider;
    private final MasterLazyDatasetList master;

    public SlaveLazyDatasetList(final InteractiveChartPanel chartPanel, final ISlaveLazyDatasetProvider provider) {
        this.provider = provider;
        this.master = (MasterLazyDatasetList) chartPanel.getDataset().getData();
        master.registerSlaveDatasetListener(this);
    }

    @Override
    protected XYDataItemOHLC dummyValue() {
        return DUMMY_VALUE;
    }

    @Override
    public synchronized void append(final int appendCount) {
        final int masterSizeAfter = master.size();
        final int masterSizeBefore = masterSizeAfter - appendCount;
        int countRemoved = 0;
        //remove at least two elements
        final Map<Integer, OHLCDataItem> prevValues = new HashMap<>();
        while (data.size() > masterSizeBefore || countRemoved < 2) {
            final int index = data.size() - 1;
            prevValues.put(index, data.get(index).getOHLC());
            invalidate(index);
            countRemoved++;
        }
        final int fromIndex = data.size();
        final int toIndex = masterSizeAfter - 1;
        for (int i = fromIndex; i <= toIndex; i++) {
            final FDate key = FDate.valueOf(master.get(i).getDate());
            final OHLCDataItem prevValue = prevValues.get(i);
            final XYDataItemOHLC value = provider.getValue(key, prevValue);
            if (value == null) {
                throw new IllegalStateException(toString() + ": " + i + ". value should not be null: " + key);
            }
            data.add(value);
        }
        assertSameSizeAsMaster();
    }

    private void invalidate(final int i) {
        final XYDataItemOHLC removed = data.remove(i);
        removed.setOHLC(null);
    }

    @Override
    public synchronized void prepend(final int prependCount) {
        final List<XYDataItemOHLC> prependItems = new ArrayList<>(prependCount);
        for (int i = 0; i < prependCount; i++) {
            final FDate key = FDate.valueOf(master.get(i).getDate());
            final XYDataItemOHLC value = provider.getValue(key);
            if (value == null) {
                throw new IllegalStateException(toString() + ": " + i + ". value should not be null: " + key);
            }
            prependItems.add(value);
        }
        data.addAll(0, prependItems);
        assertSameSizeAsMaster();
    }

    private void assertSameSizeAsMaster() {
        final int masterSize = master.size();
        if (data.size() != masterSize) {
            Err.process(new IllegalStateException("slave.size [" + data.size() + "] should be equal to master.size ["
                    + masterSize + "]. Reloading: " + provider));
            loadInitial();
        } else if (data.size() > 0) {
            final Date slaveFirstDate = data.get(0).getOHLC().getDate();
            final Date masterFirstDate = master.get(0).getDate();
            if (!slaveFirstDate.equals(masterFirstDate)) {
                Err.process(new IllegalStateException("slave[first].date [" + slaveFirstDate
                        + "] should be equal to master[first].date [" + masterFirstDate + "]. Reloading: " + provider));
                loadInitial();
            } else {
                final Date slaveLastDate = data.get(data.size() - 1).getOHLC().getDate();
                final Date masterLastDate = master.get(master.size() - 1).getDate();
                if (!slaveLastDate.equals(masterLastDate)) {
                    Err.process(new IllegalStateException(
                            "slave[last].date [" + slaveLastDate + "] should be equal to master[last].date ["
                                    + masterLastDate + "]. Reloading: " + provider));
                    loadInitial();
                }
            }
        }
    }

    @Override
    public synchronized void loadInitial() {
        for (int i = 0; i < data.size(); i++) {
            invalidate(i);
        }
        data = new ArrayList<>(data.size());
        for (int i = 0; i < master.size(); i++) {
            final FDate key = FDate.valueOf(master.get(i).getDate());
            final XYDataItemOHLC value = provider.getValue(key);
            if (value == null) {
                throw new IllegalStateException(toString() + ": " + i + ". value should not be null: " + key);
            }
            data.add(value);
        }
        assertSameSizeAsMaster();
    }

    @Override
    public synchronized void removeStart(final int tooManyBefore) {
        for (int i = 0; i < tooManyBefore; i++) {
            invalidate(0);
        }
        assertSameSizeAsMaster();
    }

    @Override
    public synchronized void removeEnd(final int tooManyAfter) {
        for (int i = 0; i < tooManyAfter; i++) {
            invalidate(data.size() - 1);
        }
        assertSameSizeAsMaster();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(SlaveLazyDatasetList.class).with(provider).toString();
    }

}
