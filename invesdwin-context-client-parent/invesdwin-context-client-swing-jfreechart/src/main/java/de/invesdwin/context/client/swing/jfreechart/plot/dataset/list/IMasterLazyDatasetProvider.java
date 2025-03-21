package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IDrawIncompleteBar;
import de.invesdwin.context.jfreechart.dataset.TimeRangedOHLCDataItem;
import de.invesdwin.util.collections.iterable.EmptyCloseableIterable;
import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.collections.iterable.LimitingIterable;
import de.invesdwin.util.time.date.FDate;

public interface IMasterLazyDatasetProvider extends IDrawIncompleteBar {

    FDate getFirstAvailableBarEndTime();

    FDate getLastAvailableTickTime();

    FDate getLastAvailableBarEndTime();

    ICloseableIterable<? extends TimeRangedOHLCDataItem> getPreviousValues(FDate key, int count);

    default ICloseableIterable<? extends TimeRangedOHLCDataItem> getNextValues(final FDate key, final int count) {
        final FDate lastAvailableKeyTo = getLastAvailableTickTime();
        if (lastAvailableKeyTo == null) {
            return EmptyCloseableIterable.getInstance();
        }
        return new LimitingIterable<>(getValues(key, lastAvailableKeyTo), count);
    }

    ICloseableIterable<? extends TimeRangedOHLCDataItem> getValues(FDate from, FDate to);

}
