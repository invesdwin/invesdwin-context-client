package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations;

import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IIndexedDateTimeXYDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.item.AAnnotationPlottingDataItem;
import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.concurrent.WrappedExecutorService;
import de.invesdwin.util.concurrent.lock.ILock;

public interface IAnnotationPlottingDataset extends IPlotSourceDataset, IIndexedDateTimeXYDataset {

    WrappedExecutorService getExecutor();

    /**
     * Returns true if the item was removed due to size limitations
     */
    boolean addOrUpdateOrRemove(AAnnotationPlottingDataItem item);

    AAnnotationPlottingDataItem get(String annotationId);

    void remove(String annotationId);

    ILock getItemsLock();

    ICloseableIterable<AAnnotationPlottingDataItem> getVisibleItems(int firstItem, int lastItem);

}
