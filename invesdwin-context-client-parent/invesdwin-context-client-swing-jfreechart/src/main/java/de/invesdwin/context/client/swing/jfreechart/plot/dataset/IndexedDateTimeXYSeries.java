package de.invesdwin.context.client.swing.jfreechart.plot.dataset;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.jfreechart.dataset.ListXYSeriesOHLC;
import de.invesdwin.context.jfreechart.dataset.MutableXYDataItemOHLC;

@NotThreadSafe
public class IndexedDateTimeXYSeries extends ListXYSeriesOHLC implements IDrawIncompleteBar {

    private final boolean drawIncompleteBar;

    public IndexedDateTimeXYSeries(final String seriesKey, final List<? extends MutableXYDataItemOHLC> data) {
        super(seriesKey, data);
        this.drawIncompleteBar = IDrawIncompleteBar.isDrawIncompleteBar(data);
    }

    @Override
    public boolean isDrawIncompleteBar() {
        return drawIncompleteBar;
    }

    @Override
    public Number getX(final int index) {
        return index;
    }

}
