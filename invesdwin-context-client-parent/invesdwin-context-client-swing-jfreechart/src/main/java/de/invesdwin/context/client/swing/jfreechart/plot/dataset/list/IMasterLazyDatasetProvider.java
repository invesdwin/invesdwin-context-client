package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import org.jfree.data.xy.OHLCDataItem;

import de.invesdwin.util.collections.loadingcache.historical.query.IHistoricalCacheQuery;
import de.invesdwin.util.time.fdate.FDate;

public interface IMasterLazyDatasetProvider {

    FDate getFirstAvailableKey();

    FDate getLastAvailableKey();

    IHistoricalCacheQuery<OHLCDataItem> query();

}
