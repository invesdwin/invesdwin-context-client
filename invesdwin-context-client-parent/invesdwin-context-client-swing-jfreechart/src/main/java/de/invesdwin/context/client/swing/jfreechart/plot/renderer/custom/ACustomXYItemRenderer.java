package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;

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
            final ValueMarker vm = (ValueMarker) marker;
            final double value = vm.getValue();
            final double v = rangeAxis.valueToJava2D(value, dataArea, plot.getRangeAxisEdge());

            final GeneralPath triangle = new GeneralPath();
            triangle.setWindingRule(Path2D.WIND_EVEN_ODD);
            final double width = 10;
            final double height = 10;
            triangle.moveTo(dataArea.getMaxY(), v);
            triangle.lineTo(dataArea.getMinX() + (width / 2.0), v - (height / 2));
            triangle.lineTo(dataArea.getMinX() + (width / 2.0), v + (height / 2));
            triangle.lineTo(dataArea.getMinX() - (width / 2.0), v - (height / 2));
            triangle.closePath();
            g2.draw(triangle);
        }
    }

    @Override
    public void drawDomainMarker(final Graphics2D g2, final XYPlot plot, final ValueAxis domainAxis,
            final Marker marker, final Rectangle2D dataArea) {
        super.drawDomainMarker(g2, plot, domainAxis, marker, dataArea);
        if (marker instanceof TriangleLineValueMarker) {
            if (PlotOrientation.HORIZONTAL.equals(plot.getOrientation())) {
                throw new UnsupportedOperationException(
                        "PlotOrientation " + PlotOrientation.HORIZONTAL + " not supported yet for TriangleValueMarker");
            }
            final TriangleLineValueMarker vm = (TriangleLineValueMarker) marker;
            final double value = vm.getValue();
            final double v = domainAxis.valueToJava2D(value, dataArea, plot.getDomainAxisEdge());

            final double width = vm.getTriangleWidth();
            final double height = vm.getTriangleHeight();
            //Top Triangle
            if (vm.isDrawStartTriangle()) {
                final GeneralPath topTriangle = new GeneralPath();
                topTriangle.setWindingRule(Path2D.WIND_EVEN_ODD);
                topTriangle.moveTo(v, dataArea.getMinY() + (height / 2.0));
                topTriangle.lineTo(v - (width / 2), dataArea.getMinY());
                topTriangle.lineTo(v + (width / 2), dataArea.getMinY());
                topTriangle.lineTo(v, dataArea.getMinY() + (height / 2.0));
                topTriangle.closePath();
                g2.setPaint(vm.getTrianglePaint());
                g2.fill(topTriangle);
                g2.draw(topTriangle);
            }

            //Bottom Triangle
            if (vm.isDrawEndTriangle()) {
                final GeneralPath bottomTriangle = new GeneralPath();
                bottomTriangle.setWindingRule(Path2D.WIND_EVEN_ODD);
                bottomTriangle.moveTo(v, dataArea.getMaxY() - (height / 2.0));
                bottomTriangle.lineTo(v - (width / 2), dataArea.getMaxY());
                bottomTriangle.lineTo(v + (width / 2), dataArea.getMaxY());
                bottomTriangle.lineTo(v, dataArea.getMaxY() - (height / 2.0));
                bottomTriangle.closePath();
                g2.setPaint(vm.getTrianglePaint());
                g2.fill(bottomTriangle);
                g2.draw(bottomTriangle);
            }
        }
    }
}
