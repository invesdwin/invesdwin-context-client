package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IDrawIncompleteBar;
import de.invesdwin.context.jfreechart.dataset.TimeRangedOHLCDataItem;
import de.invesdwin.util.time.date.FDate;

public interface ISlaveLazyDatasetProvider extends IDrawIncompleteBar {

    TimeRangedOHLCDataItem getValue(FDate key);

}
