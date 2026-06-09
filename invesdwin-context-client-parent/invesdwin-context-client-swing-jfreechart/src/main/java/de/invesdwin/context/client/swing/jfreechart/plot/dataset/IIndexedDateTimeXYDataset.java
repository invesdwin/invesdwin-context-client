package de.invesdwin.context.client.swing.jfreechart.plot.dataset;

import org.jfree.data.xy.XYDataset;

import de.invesdwin.util.time.date.FDate;

public interface IIndexedDateTimeXYDataset extends XYDataset {

    FDate getXValueAsDateTimeStart(int series, int item);

    FDate getXValueAsDateTimeEnd(int series, int item);

    int getDateTimeStartAsItemIndex(int series, FDate time);

    int getDateTimeEndAsItemIndex(int series, FDate time);

}
