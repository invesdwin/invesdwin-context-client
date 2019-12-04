package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.item;

import java.util.Date;
import java.util.NoSuchElementException;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.data.xy.OHLCDataItem;

import de.invesdwin.context.jfreechart.dataset.MutableOHLCDataItem;
import de.invesdwin.util.collections.iterable.buffer.BufferingIterator;
import de.invesdwin.util.collections.iterable.buffer.EmptyBufferingIterator;
import de.invesdwin.util.collections.iterable.buffer.IBufferingIterator;
import de.invesdwin.util.time.fdate.FDate;

@NotThreadSafe
public class MasterOHLCDataItem extends MutableOHLCDataItem {

    public static final MasterOHLCDataItem DUMMY_VALUE = new MasterOHLCDataItem(FDate.MIN_DATE.dateValue()) {

        @Override
        public void setDate(final Date date) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setClose(final Number close) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setHigh(final Number high) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLow(final Number low) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setOpen(final Number open) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setVolume(final Number volume) {
            throw new UnsupportedOperationException();
        }
    };

    private IBufferingIterator<SlaveXYDataItemOHLC> slaveItems;

    public MasterOHLCDataItem(final Date date) {
        super(date);
    }

    public MasterOHLCDataItem(final OHLCDataItem ohlc) {
        super(ohlc);
    }

    public MasterOHLCDataItem(final Date date, final double open, final double high, final double low,
            final double close, final double volume) {
        super(date, open, high, low, close, volume);
    }

    public void addSlaveItem(final SlaveXYDataItemOHLC slaveItem) {
        if (slaveItems == EmptyBufferingIterator.<SlaveXYDataItemOHLC> getInstance()) {
            //load directly if we have a valid value
            final Date date = getDate();
            if (date.getTime() != DUMMY_VALUE.getDate().getTime()) {
                slaveItem.loadValue(FDate.valueOf(date));
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
