package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations;

import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IIndexedDateTimeXYDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.item.AAnnotationPlottingDataItem;
import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.concurrent.WrappedExecutorService;

public interface IAnnotationPlottingDataset extends IPlotSourceDataset, IIndexedDateTimeXYDataset {

    WrappedExecutorService getExecutor();

    String[] getAnnotationIds();

    void addOrUpdate(AAnnotationPlottingDataItem item);

    AAnnotationPlottingDataItem get(String annotationId);

    void remove(String annotationId);

    ICloseableIterable<AAnnotationPlottingDataItem> getVisibleItems(int firstItem, int lastItem);

}
