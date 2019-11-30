package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.item;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.data.xy.OHLCDataItem;

import de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.ISlaveLazyDatasetProvider;
import de.invesdwin.context.jfreechart.dataset.MutableOHLCDataItem;
import de.invesdwin.context.jfreechart.dataset.MutableXYDataItemOHLC;
import de.invesdwin.util.time.fdate.FDate;

@NotThreadSafe
public class SlaveXYDataItemOHLC extends MutableXYDataItemOHLC {

    public static final SlaveXYDataItemOHLC DUMMY_VALUE = new SlaveXYDataItemOHLC(null,
            MasterOHLCDataItem.DUMMY_VALUE) {
        @Override
        public void setOHLC(final OHLCDataItem ohlc) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setX(final Number x) {
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

        @Override
        public void loadValue(final FDate key) {
            throw new UnsupportedOperationException();
        }
    };
    private volatile ISlaveLazyDatasetProvider provider;

    public SlaveXYDataItemOHLC(final ISlaveLazyDatasetProvider provider) {
        this(provider, MutableOHLCDataItem.DUMMY_VALUE);
    }

    public SlaveXYDataItemOHLC(final ISlaveLazyDatasetProvider provider, final OHLCDataItem prevValue) {
        super(prevValue != null ? prevValue : MutableOHLCDataItem.DUMMY_VALUE);
        this.provider = provider;
    }

    public void loadValue(final FDate key) {
        if (provider == null) {
            return;
        }
        final ISlaveLazyDatasetProvider providerCopy;
        synchronized (this) {
            providerCopy = provider;
            provider = null;
        }
        if (providerCopy == null) {
            return;
        }
        innerLoadValue(key, providerCopy);
    }

    private void innerLoadValue(final FDate key, final ISlaveLazyDatasetProvider providerCopy) {
        final OHLCDataItem value = providerCopy.getValue(key);
        if (value == null) {
            setOHLC(MutableOHLCDataItem.DUMMY_VALUE);
        } else {
            setOHLC(value);
        }
    }

    public void invalidate() {
        setOHLC(null);
        provider = null;
    }

}
