package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.item;

import java.util.NoSuchElementException;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.jfreechart.dataset.MutableOHLCDataItem;
import de.invesdwin.context.jfreechart.dataset.TimeRangedOHLCDataItem;
import de.invesdwin.util.collections.iterable.buffer.BufferingIterator;
import de.invesdwin.util.collections.iterable.buffer.EmptyBufferingIterator;
import de.invesdwin.util.collections.iterable.buffer.IBufferingIterator;
import de.invesdwin.util.time.date.FDate;

@NotThreadSafe
public class MasterOHLCDataItem extends MutableOHLCDataItem {

    public static final MasterOHLCDataItem DUMMY_VALUE = new MasterOHLCDataItem(FDate.MIN_DATE, FDate.MIN_DATE) {

        @Override
        public void setStartTime(final FDate startTime) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setEndTime(final FDate endTime) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setClose(final double close) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setHigh(final double high) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLow(final double low) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setOpen(final double open) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setVolume(final double volume) {
            throw new UnsupportedOperationException();
        }
    };

    private IBufferingIterator<SlaveXYDataItemOHLC> slaveItems;

    public MasterOHLCDataItem(final FDate startTime, final FDate endTime) {
        super(startTime, endTime);
    }

    public MasterOHLCDataItem(final TimeRangedOHLCDataItem ohlc) {
        super(ohlc);
    }

    public MasterOHLCDataItem(final FDate startTime, final FDate endTime, final double open, final double high,
            final double low, final double close, final double volume) {
        super(startTime, endTime, open, high, low, close, volume);
    }

    public void addSlaveItem(final SlaveXYDataItemOHLC slaveItem) {
        if (slaveItems == EmptyBufferingIterator.<SlaveXYDataItemOHLC> getInstance()) {
            //load directly if we have a valid value
            final FDate endTime = getEndTime();
            if (!endTime.equalsNotNullSafe(DUMMY_VALUE.getEndTime())) {
                slaveItem.loadValue(endTime);
            }
        } else {
            if (slaveItems == null) {
                slaveItems = new BufferingIterator<>();
            }
            this.slaveItems.add(slaveItem);
        }
    }

    public void loadSlaveItems(final FDate key) {
        if (slaveItems != null) {
            try {
                while (true) {
                    final SlaveXYDataItemOHLC slaveItemLoader = slaveItems.next();
                    slaveItemLoader.loadValue(key);
                }
            } catch (final NoSuchElementException e) {
                //end reached
            }
        }
        slaveItems = EmptyBufferingIterator.getInstance();
    }

    public boolean isSlaveItemsLoaded() {
        return slaveItems instanceof EmptyBufferingIterator;
    }

}
