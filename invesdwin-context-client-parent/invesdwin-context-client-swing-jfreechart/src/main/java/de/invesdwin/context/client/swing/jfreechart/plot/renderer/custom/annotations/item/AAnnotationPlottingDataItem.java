package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.item;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.AnnotationPlottingDataset;
import de.invesdwin.util.time.date.FDate;

@NotThreadSafe
public abstract class AAnnotationPlottingDataItem {

    private final String annotationId;

    //CHECKSTYLE:OFF
    public AAnnotationPlottingDataItem(final String annotationId) {
        //CHECKSTYLE:ON
        this.annotationId = annotationId;
    }

    public String getAnnotationId() {
        return annotationId;
    }

    public abstract boolean isItemLoaded();

    public abstract void updateItemLoaded(long firstLoadedKeyMillis, long lastLoadedKeyMillis, boolean trailingLoaded,
            AnnotationPlottingDataset dataset);

    public abstract void modifyItemLoadedIndexes(int fromIndex, int addend);

    public final int getStartTimeLoadedIndex() {
        assertItemLoaded();
        return innerGetStartTimeLoadedIndex();
    }

    protected abstract int innerGetStartTimeLoadedIndex();

    protected void assertItemLoaded() {
        if (!isItemLoaded()) {
            throw new IllegalStateException("not loaded");
        }
    }

    public final int getEndTimeLoadedIndex() {
        assertItemLoaded();
        return innerGetEndTimeLoadedIndex();
    }

    protected abstract int innerGetEndTimeLoadedIndex();

    public abstract FDate getStartTime();

    public abstract FDate getEndTime();
}
