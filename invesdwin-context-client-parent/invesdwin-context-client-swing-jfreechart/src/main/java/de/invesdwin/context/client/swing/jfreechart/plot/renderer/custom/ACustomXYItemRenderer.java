package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;

import de.invesdwin.context.client.swing.jfreechart.plot.Markers;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.marker.TriangleLineValueMarker;

/**
 * CustomXYItemRenderer with support for TriangleLineValueMarkers
 *
 * @author matze
 *
 */

@NotThreadSafe
public abstract class ACustomXYItemRenderer extends AbstractXYItemRenderer {
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
