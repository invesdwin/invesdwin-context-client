package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import de.invesdwin.context.jfreechart.dataset.TimeRangedOHLCDataItem;
import de.invesdwin.util.time.date.FDate;

@FunctionalInterface
public interface ISlaveLazyDatasetProvider {

    TimeRangedOHLCDataItem getValue(FDate key);

}
