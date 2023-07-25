package de.invesdwin.context.client.swing.jfreechart.plot.renderer;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.LineUtils;
import org.jfree.data.xy.XYDataset;

import de.invesdwin.context.client.swing.jfreechart.plot.Markers;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.IDelegatePriceLineXYItemRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.IPriceLineRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.XYPriceLineAnnotation;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.marker.TriangleLineValueMarker;
import de.invesdwin.util.math.Doubles;

@NotThreadSafe
public class FastXYStepRenderer extends XYStepRenderer implements IDelegatePriceLineXYItemRenderer {

    private final IPlotSourceDataset dataset;
    private final XYPriceLineAnnotation priceLineAnnotation;

    public FastXYStepRenderer(final IPlotSourceDataset dataset) {
        Renderers.disableAutoPopulate(this);

        this.dataset = dataset;
        this.priceLineAnnotation = new XYPriceLineAnnotation(dataset, this);
        addAnnotation(priceLineAnnotation);
    }

    @Override
    public IPlotSourceDataset getDataset() {
        return dataset;
    }

    @Override
    public IPriceLineRenderer getDelegatePriceLineRenderer() {
        return priceLineAnnotation;
    }

    @Override
    public XYItemRendererState initialise(final Graphics2D g2, final Rectangle2D dataArea, final XYPlot plot,
            final XYDataset data, final PlotRenderingInfo info) {
        //info null to skip entity collection stuff
        return super.initialise(g2, dataArea, plot, data, null);
    }

    @Override
    protected void updateCrosshairValues(final CrosshairState crosshairState, final double x, final double y,
            final int datasetIndex, final double transX, final double transY, final PlotOrientation orientation) {
        //noop
    }

    @Override
    protected void addEntity(final EntityCollection entities, final Shape hotspot, final XYDataset dataset,
            final int series, final int item, final double entityX, final double entityY) {
        //noop
    }

    @Override
    public boolean isItemLabelVisible(final int row, final int column) {
        //noop
        return false;
    }

    @Override
    protected void drawItemLabel(final Graphics2D g2, final PlotOrientation orientation, final XYDataset dataset,
            final int series, final int item, final double x, final double y, final boolean negative) {
        //noop
    }

    @Override
    public void drawItem(final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea,
            final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis,
            final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState,
            final int pass) {
        //don't draw in-progress values that are missing
        if (Doubles.isNaN(dataset.getYValue(series, item))) {
            return;
        }

        // do nothing if item is not visible
        if (!getItemVisible(series, item)) {
            return;
        }

        final PlotOrientation orientation = plot.getOrientation();

        final Paint seriesPaint = getItemPaint(series, item);
        final Stroke seriesStroke = getItemStroke(series, item);
        g2.setPaint(seriesPaint);
        g2.setStroke(seriesStroke);

        // get the data point...
        final double x1 = dataset.getXValue(series, item);
        final double y1 = dataset.getYValue(series, item);

        final RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        final RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        final double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        final double transY1 = (Double.isNaN(y1) ? Double.NaN : rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation));

        if (pass == 0 && item > 0) {
            // get the previous data point...
            final double x0 = dataset.getXValue(series, item - 1);
            final double y0 = dataset.getYValue(series, item - 1);
            final double transX0 = domainAxis.valueToJava2D(x0, dataArea, xAxisLocation);
            final double transY0 = (Double.isNaN(y0) ? Double.NaN
                    : rangeAxis.valueToJava2D(y0, dataArea, yAxisLocation));

            if (orientation == PlotOrientation.HORIZONTAL) {
                if (transY0 == transY1) {
                    // this represents the situation
                    // for drawing a horizontal bar.
                    drawLine(g2, state.workingLine, transY0, transX0, transY1, transX1, dataArea);
                } else { //this handles the need to perform a 'step'.

                    // calculate the step point
                    final double transXs = transX0 + (getStepPoint() * (transX1 - transX0));
                    drawLine(g2, state.workingLine, transY0, transX0, transY0, transXs, dataArea);
                    drawLine(g2, state.workingLine, transY0, transXs, transY1, transXs, dataArea);
                    drawLine(g2, state.workingLine, transY1, transXs, transY1, transX1, dataArea);
                }
            } else if (orientation == PlotOrientation.VERTICAL) {
                if (transY0 == transY1) { // this represents the situation
                                          // for drawing a horizontal bar.
                    drawLine(g2, state.workingLine, transX0, transY0, transX1, transY1, dataArea);
                } else { //this handles the need to perform a 'step'.
                    // calculate the step point
                    final double transXs = transX0 + (getStepPoint() * (transX1 - transX0));
                    drawLine(g2, state.workingLine, transX0, transY0, transXs, transY0, dataArea);
                    drawLine(g2, state.workingLine, transXs, transY0, transXs, transY1, dataArea);
                    drawLine(g2, state.workingLine, transXs, transY1, transX1, transY1, dataArea);
                }
            }
        }
    }

    private void drawLine(final Graphics2D g2, final Line2D line, final double x0, final double y0, final double x1,
            final double y1, final Rectangle2D dataArea) {
        if (Double.isNaN(x0) || Double.isNaN(x1) || Double.isNaN(y0) || Double.isNaN(y1)) {
            return;
        }
        line.setLine(x0, y0, x1, y1);
        final boolean visible = LineUtils.clipLine(line, dataArea);
        if (visible) {
            g2.draw(line);
        }
    }

    @Override
    public void drawRangeMarker(final Graphics2D g2, final XYPlot plot, final ValueAxis rangeAxis, final Marker marker,
            final Rectangle2D dataArea) {
        super.drawRangeMarker(g2, plot, rangeAxis, marker, dataArea);
        if (marker instanceof TriangleLineValueMarker) {
            Markers.drawRangeTriangles((TriangleLineValueMarker) marker, g2, plot, rangeAxis, dataArea);
        }
    }

    @Override
    public void drawDomainMarker(final Graphics2D g2, final XYPlot plot, final ValueAxis domainAxis,
            final Marker marker, final Rectangle2D dataArea) {
        super.drawDomainMarker(g2, plot, domainAxis, marker, dataArea);
        if (marker instanceof TriangleLineValueMarker) {
            Markers.drawDomainTriangles((TriangleLineValueMarker) marker, g2, plot, domainAxis, dataArea);
        }
    }

}
