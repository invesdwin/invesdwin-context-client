package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import de.invesdwin.context.jfreechart.dataset.TimeRangedOHLCDataItem;
import de.invesdwin.util.collections.iterable.EmptyCloseableIterable;
import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.collections.iterable.LimitingIterable;
import de.invesdwin.util.time.fdate.FDate;
import de.invesdwin.util.time.range.TimeRange;

public interface IMasterLazyDatasetProvider {

    TimeRange getFirstAvailableKey();

    FDate getLastAvailableKeyTo();

    ICloseableIterable<? extends TimeRangedOHLCDataItem> getPreviousValues(FDate key, int count);

    default ICloseableIterable<? extends TimeRangedOHLCDataItem> getNextValues(final FDate key, final int count) {
        final FDate lastAvailableKeyTo = getLastAvailableKeyTo();
        if (lastAvailableKeyTo == null) {
            return EmptyCloseableIterable.getInstance();
        }
        return new LimitingIterable<>(getValues(key, lastAvailableKeyTo), count);
    }

    ICloseableIterable<? extends TimeRangedOHLCDataItem> getValues(FDate from, FDate to);

}
