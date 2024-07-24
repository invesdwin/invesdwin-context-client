package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.util.math.Longs;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

@NotThreadSafe
public class AnnotationRenderingInfo {

    private final Long2IntOpenHashMap labelMagicNumber_count = new Long2IntOpenHashMap();

    public void beforePlotDraw() {
        if (!labelMagicNumber_count.isEmpty()) {
            labelMagicNumber_count.clear();
        }
    }

    public int getAndIncrementLabelOverlap(final LabelVerticalAlignType labelVerticalAlign, final int itemIndex) {
        final long labelMagicNumber = Longs.combine(labelVerticalAlign.ordinal(), itemIndex);
        final int prevCount = labelMagicNumber_count.addTo(labelMagicNumber, 1);
        return prevCount;
    }
}
