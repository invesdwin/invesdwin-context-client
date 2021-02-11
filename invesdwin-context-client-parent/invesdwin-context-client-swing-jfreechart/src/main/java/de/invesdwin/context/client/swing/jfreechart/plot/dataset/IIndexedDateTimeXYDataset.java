package de.invesdwin.context.client.swing.jfreechart.plot.dataset;

import org.jfree.data.xy.XYDataset;

import de.invesdwin.util.time.fdate.FDate;

public interface IIndexedDateTimeXYDataset extends XYDataset {

    double getXValueAsDateTimeStart(int series, int item);

    double getXValueAsDateTimeEnd(int series, int item);

    int getDateTimeStartAsItemIndex(int series, FDate time);

    int getDateTimeEndAsItemIndex(int series, FDate time);

}
