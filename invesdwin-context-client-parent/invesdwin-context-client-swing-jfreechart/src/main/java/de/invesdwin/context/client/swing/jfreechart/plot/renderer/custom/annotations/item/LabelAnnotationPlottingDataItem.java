package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.item;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.ui.TextAnchor;

import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.AnnotationPlottingDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.LabelHorizontalAlignType;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.LabelVerticalAlignType;
import de.invesdwin.util.time.date.FDate;

@NotThreadSafe
public class LabelAnnotationPlottingDataItem extends AAnnotationPlottingDataItem {

    private final FDate time;
    private final double price;
    private final String label;
    private final LabelHorizontalAlignType labelHorizontalAlign;
    private final LabelVerticalAlignType labelVerticalAlign;
    private boolean itemLoaded;
    private int timeLoadedIndex = Integer.MIN_VALUE;

    public LabelAnnotationPlottingDataItem(final String annotationId, final FDate time, final double price,
            final String label, final LabelHorizontalAlignType labelHorizontalAlign,
            final LabelVerticalAlignType labelVerticalAlign) {
        super(annotationId);
        this.time = time;
        this.price = price;
        this.label = label;
        this.labelHorizontalAlign = labelHorizontalAlign;
        this.labelVerticalAlign = labelVerticalAlign;
    }

    public FDate getTime() {
        return time;
    }

    public double getPrice() {
        return price;
    }

    public String getLabel() {
        return label;
    }

    public LabelHorizontalAlignType getLabelHorizontalAlign() {
        return labelHorizontalAlign;
    }

    public LabelVerticalAlignType getLabelVerticalAlign() {
        return labelVerticalAlign;
    }

    @Override
    public boolean isItemLoaded() {
        return itemLoaded;
    }

    @Override
    public void updateItemLoaded(final long firstLoadedKeyMillis, final long lastLoadedKeyMillis,
            final boolean trailingLoaded, final AnnotationPlottingDataset dataset) {
        if (!trailingLoaded && getTime().millisValue() > lastLoadedKeyMillis
                || getTime() != null && getTime().millisValue() < firstLoadedKeyMillis) {
            if (itemLoaded) {
                itemLoaded = false;
                timeLoadedIndex = Integer.MIN_VALUE;
            }
        } else {
            this.timeLoadedIndex = dataset.getDateTimeEndAsItemIndex(0, time);
            itemLoaded = true;
        }
    }

    @Override
    public void modifyItemLoadedIndexes(final int fromIndex, final int addend) {
        if (itemLoaded) {
            if (timeLoadedIndex >= fromIndex) {
                timeLoadedIndex += addend;
            }
        }
    }

    @Override
    public int innerGetStartTimeLoadedIndex() {
        return timeLoadedIndex;
    }

    @Override
    public int innerGetEndTimeLoadedIndex() {
        return timeLoadedIndex;
    }

    public TextAnchor getLabelTextAnchor() {
        return labelHorizontalAlign.getTextAnchor(labelVerticalAlign);
    }

    @Override
    public FDate getStartTime() {
        return time;
    }

    @Override
    public FDate getEndTime() {
        return time;
    }

}
