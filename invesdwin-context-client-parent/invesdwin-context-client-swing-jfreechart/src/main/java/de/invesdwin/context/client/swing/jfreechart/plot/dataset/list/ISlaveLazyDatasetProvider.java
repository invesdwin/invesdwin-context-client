package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import org.jfree.data.xy.OHLCDataItem;

import de.invesdwin.context.jfreechart.dataset.XYDataItemOHLC;
import de.invesdwin.util.time.fdate.FDate;

public interface ISlaveLazyDatasetProvider {

    default XYDataItemOHLC getValue(final FDate key, final OHLCDataItem prevValue) {
        //ignore per default, can be used to remove flickering in async providers
        return getValue(key);
    }

    XYDataItemOHLC getValue(FDate key);

}
