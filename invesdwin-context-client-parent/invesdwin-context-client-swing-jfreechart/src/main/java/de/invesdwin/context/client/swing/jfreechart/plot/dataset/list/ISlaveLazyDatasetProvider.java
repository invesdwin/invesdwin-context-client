package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import org.jfree.data.xy.OHLCDataItem;

import de.invesdwin.util.time.fdate.FDate;

public interface ISlaveLazyDatasetProvider {

    OHLCDataItem getValue(FDate key);

}
