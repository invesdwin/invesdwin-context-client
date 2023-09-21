package de.invesdwin.context.client.swing.jfreechart.panel.helper;

import java.awt.geom.Point2D;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.Range;

import de.invesdwin.context.client.swing.jfreechart.plot.Axis;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IndexedDateTimeOHLCDataset;
import de.invesdwin.util.time.date.FDate;

@NotThreadSafe
public class AxisDragInfo {

    private Point2D initialDragPoint;
    private final Range initialAxisRange;
    private FDate initialAxisLowerBoundFDate;
    private FDate initialAxisUpperBoundFDate;
    private ValueAxis valueAxis;
    private Integer subplotIndex;
    private double plotWidth;
    private double plotHeight;
    private final Axis axis;
    private FDate domainAnchorFDate;

    /**
     * Constructor for Domain-AxisDragInfo since we don't need a subplotindex here.
     */
    public AxisDragInfo(final Point2D initialDragPoint, final ValueAxis valueAxis, final double plotWidth,
            final Axis axis, final PlotRenderingInfo plotInfo) {
        final Double domainAnchor = valueAxis.java2DToValue(initialDragPoint.getX(), plotInfo.getDataArea(),
                RectangleEdge.BOTTOM);
        final XYPlot xyPlot = (XYPlot) valueAxis.getPlot();
        final IPlotSourceDataset dataset = (IPlotSourceDataset) xyPlot.getDataset();
        final IndexedDateTimeOHLCDataset masterDataset = dataset.getMasterDataset();

        this.initialDragPoint = initialDragPoint;
        this.initialAxisRange = valueAxis.getRange();
        this.initialAxisLowerBoundFDate = masterDataset.getXDate(0,
                Double.valueOf(Math.round(valueAxis.getRange().getLowerBound())).intValue());
        this.initialAxisUpperBoundFDate = masterDataset.getXDate(0,
                Double.valueOf(Math.round(valueAxis.getRange().getUpperBound())).intValue());

        this.valueAxis = valueAxis;
        this.plotWidth = plotWidth;
        this.axis = axis;

        //We get the StartTime here.. the EndTime would be more intuitive but the difference is very un-noticable (especially when zoomed out).
        this.domainAnchorFDate = masterDataset.getXDate(0, Double.valueOf(Math.round(domainAnchor)).intValue());
    }

    /**
     * Constructor for Range-AxisDragInfo.
     */
    public AxisDragInfo(final Point2D initialDragPoint, final ValueAxis valueAxis, final Integer subplotIndex,
            final double plotHeight, final Axis axis) {
        this.initialDragPoint = initialDragPoint;
        this.initialAxisRange = valueAxis.getRange();
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

    public Range getInitialAxisRange() {
        return initialAxisRange;
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

    public FDate getDomainAnchorFDate() {
        return domainAnchorFDate;
    }

    public FDate getInitialAxisLowerBoundFDate() {
        return initialAxisLowerBoundFDate;
    }

    public void setInitialAxisLowerBoundFDate(final FDate initialAxisLowerBoundFDate) {
        this.initialAxisLowerBoundFDate = initialAxisLowerBoundFDate;
    }

    public FDate getInitialAxisUpperBoundFDate() {
        return initialAxisUpperBoundFDate;
    }

    public void setInitialAxisUpperBoundFDate(final FDate initialAxisUpperBoundFDate) {
        this.initialAxisUpperBoundFDate = initialAxisUpperBoundFDate;
    }
}
