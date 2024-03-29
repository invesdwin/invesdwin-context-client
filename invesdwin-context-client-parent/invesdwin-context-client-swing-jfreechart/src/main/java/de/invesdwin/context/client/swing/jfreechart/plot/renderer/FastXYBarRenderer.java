package de.invesdwin.context.client.swing.jfreechart.plot.renderer;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;

import de.invesdwin.context.client.swing.jfreechart.plot.Markers;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.IDelegatePriceLineXYItemRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.IPriceLineRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.XYPriceLineAnnotation;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.marker.TriangleLineValueMarker;

@NotThreadSafe
public class FastXYBarRenderer extends XYBarRenderer implements IDelegatePriceLineXYItemRenderer {

    private final XYPriceLineAnnotation priceLineAnnotation;
    private final IPlotSourceDataset dataset;

    public FastXYBarRenderer(final IPlotSourceDataset dataset) {
        this(dataset, 0D);
    }

    public FastXYBarRenderer(final IPlotSourceDataset dataset, final double margin) {
        super(margin);
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
            final XYDataset dataset, final PlotRenderingInfo info) {
        //info null to skip EntityCollection stuff
        return super.initialise(g2, dataArea, plot, dataset, null);
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
