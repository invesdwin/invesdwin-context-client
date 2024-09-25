package de.invesdwin.context.client.swing.jfreechart.plot.dataset;

import org.jfree.data.xy.XYDataset;

import de.invesdwin.util.math.Doubles;

public interface IDrawIncompleteBar {

    boolean isDrawIncompleteBar();

    static boolean isDrawIncompleteBar(final Object obj) {
        if (obj instanceof IDrawIncompleteBar) {
            final IDrawIncompleteBar cObj = (IDrawIncompleteBar) obj;
            return cObj.isDrawIncompleteBar();
        } else {
            return false;
        }
    }

    /**
     * Get's the last Non-NaN Y-Value in the Dataset. Only looking up the last 2 entrie's of the dataset depending on if
     * the dataset draws an incomplete bar or not.
     */
    static double getLastYValue(final XYDataset dataset) {
        final int lastItem = dataset.getItemCount(0) - 1;
        if (lastItem < 0) {
            return Double.NaN;
        }
        final double lastYValue = dataset.getYValue(0, lastItem);
        if (Doubles.isNaN(lastYValue) && !isDrawIncompleteBar(dataset) && lastItem > 0) {
            return dataset.getYValue(0, lastItem - 1);
        } else {
            return lastYValue;
        }
    }

}
