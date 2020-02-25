package de.invesdwin.context.client.swing.jfreechart.plot.dataset;

import org.jfree.data.xy.XYDataset;

import de.invesdwin.util.time.fdate.FDate;

public interface IIndexedDateTimeXYDataset extends XYDataset {

    double getXValueAsDateTime(int series, int item);

    int getDateTimeAsItemIndex(int series, FDate time);

}
