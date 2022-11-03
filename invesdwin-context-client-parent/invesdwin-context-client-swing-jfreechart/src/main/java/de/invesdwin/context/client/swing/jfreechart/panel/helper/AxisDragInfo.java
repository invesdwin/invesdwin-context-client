package de.invesdwin.context.client.swing.jfreechart.panel.helper;

import java.awt.geom.Point2D;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.Range;

@NotThreadSafe
public class AxisDragInfo {

    private Point2D initialDragPoint;
    private Range initalAxisRange;
    private ValueAxis rangeAxis;
    private Integer subplotIndex;
    private double plotHeight;

    public AxisDragInfo(final Point2D initialDragPoint, final ValueAxis rangeAxis, final Integer subplotIndex,
            final double plotHeight) {
        this.initialDragPoint = initialDragPoint;
        this.initalAxisRange = rangeAxis.getRange();
        this.rangeAxis = rangeAxis;
        this.subplotIndex = subplotIndex;
        this.plotHeight = plotHeight;
    }

    public Point2D getInitialDragPoint() {
        return initialDragPoint;
    }

    public void setInitialDragPoint(final Point2D initialDragPoint) {
        this.initialDragPoint = initialDragPoint;
    }

    public Range getInitalAxisRange() {
        return initalAxisRange;
    }

    public void setInitalAxisRange(final Range initalAxisRange) {
        this.initalAxisRange = initalAxisRange;
    }

    public ValueAxis getRangeAxis() {
        return rangeAxis;
    }

    public void setRangeAxis(final ValueAxis rangeAxis) {
        this.rangeAxis = rangeAxis;
    }

    public Integer getSubplotIndex() {
        return subplotIndex;
    }

    public void setSubplotIndex(final Integer subplotIndex) {
        this.subplotIndex = subplotIndex;
    }

    public double getPlotHeight() {
        return plotHeight;
    }

    public void setPlotHeight(final double plotHeight) {
        this.plotHeight = plotHeight;
    }
}
