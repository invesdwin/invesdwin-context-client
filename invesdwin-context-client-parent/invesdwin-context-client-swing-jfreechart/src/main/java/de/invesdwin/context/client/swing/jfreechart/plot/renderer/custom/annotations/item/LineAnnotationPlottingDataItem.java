package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.item;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.ui.TextAnchor;

import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.AnnotationPlottingDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.LabelHorizontalAlignType;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.LabelVerticalAlignType;
import de.invesdwin.util.time.date.FDate;

@NotThreadSafe
public class LineAnnotationPlottingDataItem extends AAnnotationPlottingDataItem {

    private final FDate startTime;
    private final double startPrice;
    private final FDate endTime;
    private final double endPrice;
    private final String label;
    private final LabelHorizontalAlignType labelHorizontalAlign;
    private final LabelVerticalAlignType labelVerticalAlign;
    private boolean itemLoaded;
    private int startTimeLoadedIndex = Integer.MIN_VALUE;
    private int endTimeLoadedIndex = Integer.MIN_VALUE;
    private double startPriceLoaded = Double.NaN;
    private double endPriceLoaded = Double.NaN;

    //CHECKSTYLE:OFF
    public LineAnnotationPlottingDataItem(final String annotationId, final FDate startTime, final double startPrice,
            final FDate endTime, final double endPrice, final String label,
            final LabelHorizontalAlignType labelHorizontalAlign, final LabelVerticalAlignType labelVerticalAlign) {
        //CHECKSTYLE:ON
        super(annotationId);
        this.startTime = startTime;
        this.startPrice = startPrice;
        this.endTime = endTime;
        this.endPrice = endPrice;
        this.label = label;
        this.labelHorizontalAlign = labelHorizontalAlign;
        this.labelVerticalAlign = labelVerticalAlign;
    }

    public FDate getStartTime() {
        return startTime;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public FDate getEndTime() {
        return endTime;
    }

    public double getEndPrice() {
        return endPrice;
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
        if (!trailingLoaded && getStartTime().millisValue() > lastLoadedKeyMillis
                || getEndTime() != null && getEndTime().millisValue() < firstLoadedKeyMillis) {
            if (itemLoaded) {
                itemLoaded = false;
                startTimeLoadedIndex = Integer.MIN_VALUE;
                startPriceLoaded = startPrice;
                endTimeLoadedIndex = Integer.MIN_VALUE;
                endPriceLoaded = endPrice;
            }
        } else {
            this.startTimeLoadedIndex = dataset.getDateTimeEndAsItemIndex(0, startTime);
            if (endTime != null) {
                this.endTimeLoadedIndex = dataset.getDateTimeEndAsItemIndex(0, endTime);
            } else {
                this.endTimeLoadedIndex = dataset.getItemCount(0) - 1;
            }
            this.startPriceLoaded = newStartPriceLoaded(dataset);
            this.endPriceLoaded = newEndPriceLoaded(dataset);
            itemLoaded = true;
        }
    }

    @Override
    public void modifyItemLoadedIndexes(final int fromIndex, final int addend) {
        if (itemLoaded) {
            if (startTimeLoadedIndex >= fromIndex) {
                startTimeLoadedIndex += addend;
            }
            if (endTimeLoadedIndex >= fromIndex) {
                endTimeLoadedIndex += addend;
            }
        }
    }

    @Override
    public int innerGetStartTimeLoadedIndex() {
        return startTimeLoadedIndex;
    }

    @Override
    public int innerGetEndTimeLoadedIndex() {
        return endTimeLoadedIndex;
    }

    public TextAnchor getLabelTextAnchor() {
        return labelHorizontalAlign.getTextAnchor(labelVerticalAlign);
    }

    public double getStartPriceLoaded() {
        return startPriceLoaded;
    }

    private double newStartPriceLoaded(final AnnotationPlottingDataset dataset) {
        if (FDate.MIN_DATE.equals(startTime) || FDate.MAX_DATE.equals(endTime)) {
            //no limited time range
            return startPrice;
        }
        if (startPrice == endPrice) {
            //no slope
            return startPrice;
        }
        final long startTimeLoadedKey = (long) dataset.getXValueAsDateTimeEnd(0, startTimeLoadedIndex);
        if (startTime.millisValue() == startTimeLoadedKey) {
            //no adjustment needed, since we hit the point
            return startPrice;
        }
        final double expectedTimeRange = endTime.millisValue() - startTime.millisValue();
        final double priceIncrementPerMillisecond = (endPrice - startPrice) / expectedTimeRange;
        final double millisDifference = startTimeLoadedKey - startTime.millisValue();
        final double startPriceLoaded = startPrice + millisDifference * priceIncrementPerMillisecond;
        return startPriceLoaded;
    }

    public double getEndPriceLoaded() {
        return endPriceLoaded;
    }

    private double newEndPriceLoaded(final AnnotationPlottingDataset dataset) {
        if (FDate.MIN_DATE.equals(startTime) || FDate.MAX_DATE.equals(endTime)) {
            //no limited time range
            return endPrice;
        }
        if (startPrice == endPrice) {
            //no slope
            return startPrice;
        }
        final long endTimeLoadedKey = (long) dataset.getXValueAsDateTimeEnd(0, endTimeLoadedIndex);
        if (endTime.millisValue() == endTimeLoadedKey) {
            //no adjustment needed, since we hit the point
            return endPrice;
        }
        final double expectedTimeRange = endTime.millisValue() - startTime.millisValue();
        final double priceIncrementPerMillisecond = (endPrice - startPrice) / expectedTimeRange;
        final double millisDifference = endTimeLoadedKey - endTime.millisValue();
        final double endPriceLoaded = endPrice + millisDifference * priceIncrementPerMillisecond;
        return endPriceLoaded;
    }

}
