package de.invesdwin.context.client.swing.jfreechart.panel.helper;

import java.awt.geom.Point2D;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.Range;

import de.invesdwin.context.client.swing.jfreechart.plot.Axis;

@NotThreadSafe
public class AxisDragInfo {

    private Point2D initialDragPoint;
    private Range initalAxisRange;
    private ValueAxis valueAxis;
    private Integer subplotIndex;
    private double plotWidth;
    private double plotHeight;
    private final Axis axis;
    private double domainAnchor;

    /**
     * Constructor for Domain-AxisDragInfo since we don't need a subplotindex here.
     */
    public AxisDragInfo(final Point2D initialDragPoint, final ValueAxis valueAxis, final double plotWidth,
            final Axis axis, final PlotRenderingInfo plotInfo) {
        this.initialDragPoint = initialDragPoint;
        this.initalAxisRange = valueAxis.getRange();
        this.valueAxis = valueAxis;
        this.plotWidth = plotWidth;
        this.axis = axis;
        this.domainAnchor = valueAxis.java2DToValue(initialDragPoint.getX(), plotInfo.getDataArea(),
                RectangleEdge.BOTTOM);
    }

    /**
     * Constructor for Range-AxisDragInfo.
     */
    public AxisDragInfo(final Point2D initialDragPoint, final ValueAxis valueAxis, final Integer subplotIndex,
            final double plotHeight, final Axis axis) {
        this.initialDragPoint = initialDragPoint;
        this.initalAxisRange = valueAxis.getRange();
        this.valueAxis = valueAxis;
        this.subplotIndex = subplotIndex;
        this.plotHeight = plotHeight;
        this.axis = axis;
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

    public ValueAxis getValueAxis() {
        return valueAxis;
    }

    public void setValueAxis(final ValueAxis valueAxis) {
        this.valueAxis = valueAxis;
    }

    public Integer getSubplotIndex() {
        return subplotIndex;
    }

    public void setSubplotIndex(final Integer subplotIndex) {
        this.subplotIndex = subplotIndex;
    }

    public double getPlotWidth() {
        return plotWidth;
    }

    public void setPlotWidth(final double plotWidth) {
        this.plotWidth = plotWidth;
    }

    public double getPlotHeight() {
        return plotHeight;
    }

    public void setPlotHeight(final double plotHeight) {
        this.plotHeight = plotHeight;
    }

    public Axis getAxis() {
        return axis;
    }

    public double getDomainAnchor() {
        return domainAnchor;
    }

}
