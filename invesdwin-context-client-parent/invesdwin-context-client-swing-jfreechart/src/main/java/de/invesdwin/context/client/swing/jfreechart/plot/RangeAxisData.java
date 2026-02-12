package de.invesdwin.context.client.swing.jfreechart.plot;

import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.data.Range;

import de.invesdwin.util.collections.factory.ILockCollectionFactory;

@NotThreadSafe
public class RangeAxisData {
    private final String rangeAxisId;
    private final int rangeAxisIndex;
    private int precision = 0;
    private boolean visible = false;
    private boolean autoRange;
    private Range range;
    private final Set<Integer> datasetIndexes = ILockCollectionFactory.getInstance(false).newLinkedSet();

    public RangeAxisData(final String rangeAxisId, final int rangeAxisIndex) {
        this.rangeAxisId = rangeAxisId;
        this.rangeAxisIndex = rangeAxisIndex;
    }

    public RangeAxisData(final String rangeAxisId, final int rangeAxisIndex, final boolean autoRange,
            final Range range) {
        this.rangeAxisId = rangeAxisId;
        this.rangeAxisIndex = rangeAxisIndex;
        this.autoRange = autoRange;
        this.range = range;
    }

    public String getRangeAxisId() {
        return rangeAxisId;
    }

    public int getRangeAxisIndex() {
        return rangeAxisIndex;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(final int precision) {
        this.precision = precision;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    public Set<Integer> getDatasetIndexes() {
        return datasetIndexes;
    }

    public boolean isAutoRange() {
        return autoRange;
    }

    public void setAutoRange(final boolean autoRange) {
        this.autoRange = autoRange;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(final Range range) {
        this.range = range;
    }

}