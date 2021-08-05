package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.client.swing.jfreechart.plot.dataset.DummyXYDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.item.AAnnotationPlottingDataItem;
import de.invesdwin.util.collections.iterable.EmptyCloseableIterable;
import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.concurrent.Executors;
import de.invesdwin.util.concurrent.WrappedExecutorService;
import de.invesdwin.util.lang.Strings;
import de.invesdwin.util.time.date.FDate;

@Immutable
public final class DummyAnnotationPlottingDataset extends DummyXYDataset implements IAnnotationPlottingDataset {

    public static final DummyAnnotationPlottingDataset INSTANCE = new DummyAnnotationPlottingDataset();
    private static final WrappedExecutorService DISABLED_EXECUTOR = Executors
            .newDisabledExecutor(DummyAnnotationPlottingDataset.class.getSimpleName());

    private DummyAnnotationPlottingDataset() {
    }

    @Override
    public double getXValueAsDateTimeStart(final int series, final int item) {
        return Double.NaN;
    }

    @Override
    public double getXValueAsDateTimeEnd(final int series, final int item) {
        return Double.NaN;
    }

    @Override
    public int getDateTimeStartAsItemIndex(final int series, final FDate time) {
        return -1;
    }

    @Override
    public int getDateTimeEndAsItemIndex(final int series, final FDate time) {
        return -1;
    }

    @Override
    public WrappedExecutorService getExecutor() {
        return DISABLED_EXECUTOR;
    }

    @Override
    public String[] getAnnotationIds() {
        return Strings.EMPTY_ARRAY;
    }

    @Override
    public void addOrUpdate(final AAnnotationPlottingDataItem item) {
    }

    @Override
    public AAnnotationPlottingDataItem get(final String annotationId) {
        return null;
    }

    @Override
    public void remove(final String annotationId) {
    }

    @Override
    public ICloseableIterable<AAnnotationPlottingDataItem> getVisibleItems(final int firstItem, final int lastItem) {
        return EmptyCloseableIterable.getInstance();
    }

}
