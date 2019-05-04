package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import de.invesdwin.context.jfreechart.dataset.XYDataItemOHLC;
import de.invesdwin.util.time.fdate.FDate;

public interface ISlaveLazyDatasetProvider {

    XYDataItemOHLC getValue(FDate key);

}
