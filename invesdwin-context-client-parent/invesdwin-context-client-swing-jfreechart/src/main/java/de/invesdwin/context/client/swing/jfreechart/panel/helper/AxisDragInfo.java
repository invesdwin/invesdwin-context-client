package de.invesdwin.context.client.swing.jfreechart.panel.helper;

import java.awt.geom.Point2D;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.axis.ValueAxis;

@NotThreadSafe
public class AxisDragInfo {

    private Point2D previousDragPoint;
    private ValueAxis rangeAxis;

    public AxisDragInfo(final Point2D previousDragPoint, final ValueAxis rangeAxis) {
        this.previousDragPoint = previousDragPoint;
        this.rangeAxis = rangeAxis;
    }

    public Point2D getPreviousDragPoint() {
        return previousDragPoint;
    }

    public void setPreviousDragPoint(final Point2D previousDragPoint) {
        this.previousDragPoint = previousDragPoint;
    }

    public ValueAxis getRangeAxis() {
        return rangeAxis;
    }

    public void setRangeAxis(final ValueAxis rangeAxis) {
        this.rangeAxis = rangeAxis;
    }

}
