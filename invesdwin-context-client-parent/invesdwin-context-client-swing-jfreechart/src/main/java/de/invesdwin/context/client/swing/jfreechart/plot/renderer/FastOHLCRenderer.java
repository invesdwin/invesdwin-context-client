package de.invesdwin.context.client.swing.jfreechart.plot.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.HighLowRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.PriceInitialSettings;
import de.invesdwin.context.client.swing.jfreechart.plot.Markers;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.IDelegatePriceLineXYItemRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.IPriceLineRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.XYPriceLineAnnotation;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.marker.TriangleLineValueMarker;

@NotThreadSafe
public class FastOHLCRenderer extends HighLowRenderer
        implements IUpDownColorRenderer, IDelegatePriceLineXYItemRenderer {

    private final FastCandlestickRenderer candlestickRenderer;
    private double tickLength;
    private final XYPriceLineAnnotation priceLineAnnotation;

    public FastOHLCRenderer(final FastCandlestickRenderer candlestickRenderer) {
        Renderers.disableAutoPopulate(this);

        this.candlestickRenderer = candlestickRenderer;
        setDefaultStroke(PriceInitialSettings.DEFAULT_SERIES_STROKE);
        this.priceLineAnnotation = new XYPriceLineAnnotation(candlestickRenderer.getDataset(), this);
        addAnnotation(priceLineAnnotation);
    }

    @Override
    public IPlotSourceDataset getDataset() {
        return candlestickRenderer.getDataset();
    }

    @Override
    public IPriceLineRenderer getDelegatePriceLineRenderer() {
        return priceLineAnnotation;
    }

    @Override
    public void setTickLength(final double length) {
        this.tickLength = length;
    }

    @Override
    public double getTickLength() {
        return tickLength;
    }

    @Override
    public Paint getItemPaint(final int row, final int column) {
        return candlestickRenderer.getItemPaint(row, column);
    }

    @Override
    public Range findRangeBounds(final XYDataset dataset) {
        return findRangeBounds(dataset, false);
    }

    @Override
    public XYItemRendererState initialise(final Graphics2D g2, final Rectangle2D dataArea, final XYPlot plot,
            final XYDataset dataset, final PlotRenderingInfo info) {
        return candlestickRenderer.initialise(g2, dataArea, plot, dataset, info);
    }

    @Override
    public void drawItem(final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea,
            final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis,
            final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState,
            final int pass) {
        final boolean horiz = candlestickRenderer.isHorizontal(plot);
        final double stickWidth = candlestickRenderer.calculateStickWidth(dataArea, horiz, domainAxis, rangeAxis);
        candlestickRenderer.calculateItemStroke(state, getDefaultStroke());
        setTickLength(stickWidth / 2);
        //info null to skip entitycollection stuff
        super.drawItem(g2, state, dataArea, null, plot, domainAxis, rangeAxis, dataset, series, item, crosshairState,
                pass);
    }

    @Override
    public Stroke getItemStroke(final int row, final int column) {
        return candlestickRenderer.getItemStroke(row, column);
    }

    @Override
    public void setUpColor(final Color upColor) {
        candlestickRenderer.setUpColor(upColor);
        fireChangeEvent();
    }

    @Override
    public Color getUpColor() {
        return candlestickRenderer.getUpColor();
    }

    @Override
    public void setDownColor(final Color downColor) {
        candlestickRenderer.setDownColor(downColor);
        fireChangeEvent();
    }

    @Override
    public Color getDownColor() {
        return candlestickRenderer.getDownColor();
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