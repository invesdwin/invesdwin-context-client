package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import de.invesdwin.context.jfreechart.dataset.TimeRangedOHLCDataItem;
import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.collections.iterable.LimitingIterable;
import de.invesdwin.util.time.fdate.FDate;

public interface IMasterLazyDatasetProvider {

    FDate getFirstAvailableEndTime();

    FDate getLastAvailableEndTime();

    ICloseableIterable<? extends TimeRangedOHLCDataItem> getPreviousValues(FDate key, int count);

    default ICloseableIterable<? extends TimeRangedOHLCDataItem> getNextValues(final FDate key, final int count) {
        return new LimitingIterable<>(getValues(key, getLastAvailableEndTime()), count);
    }

    ICloseableIterable<? extends TimeRangedOHLCDataItem> getValues(FDate from, FDate to);

}
