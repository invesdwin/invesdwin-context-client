package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import org.jfree.data.xy.OHLCDataItem;

import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.collections.iterable.LimitingIterable;
import de.invesdwin.util.time.fdate.FDate;

public interface IMasterLazyDatasetProvider {

    FDate getFirstAvailableKey();

    FDate getLastAvailableKey();

    ICloseableIterable<? extends OHLCDataItem> getPreviousValues(FDate key, int count);

    default ICloseableIterable<? extends OHLCDataItem> getNextValues(final FDate key, final int count) {
        return new LimitingIterable<>(getValues(key, getLastAvailableKey()), count);
    }

    ICloseableIterable<? extends OHLCDataItem> getValues(FDate from, FDate to);

}
