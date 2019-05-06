package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import org.jfree.data.xy.OHLCDataItem;

import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.time.fdate.FDate;

public interface IMasterLazyDatasetProvider {

    FDate getFirstAvailableKey();

    FDate getLastAvailableKey();

    ICloseableIterable<OHLCDataItem> getPreviousValues(FDate key, int count);

    ICloseableIterable<OHLCDataItem> getNextValues(FDate key, int count);

}
