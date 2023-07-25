package de.invesdwin.context.client.swing.jfreechart.plot;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;

import javax.annotation.concurrent.Immutable;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;

import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.marker.TriangleLineValueMarker;
import de.invesdwin.util.lang.reflection.field.UnsafeField;

@Immutable
public final class Markers {
    private static final UnsafeField<Double> VALUEMARKER_VALUE_FIELD;

    private Markers() {
        super();
    }

    static {
        try {
            final Field valueMarkerValueField = ValueMarker.class.getDeclaredField("value");
            VALUEMARKER_VALUE_FIELD = new UnsafeField<>(valueMarkerValueField);
        } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setValue(final ValueMarker valueMarker, final double value) {
        VALUEMARKER_VALUE_FIELD.put(valueMarker, -1D);
    }

    public static void drawRangeTriangles(final TriangleLineValueMarker marker, final Graphics2D g2, final XYPlot plot,
            final ValueAxis rangeAxis, final Rectangle2D dataArea) {
        if (PlotOrientation.HORIZONTAL.equals(plot.getOrientation())) {
            throw new UnsupportedOperationException(
                    "PlotOrientation " + PlotOrientation.HORIZONTAL + " not supported yet for TriangleValueMarker");
        }
        final double v = rangeAxis.valueToJava2D(marker.getValue(), dataArea, plot.getRangeAxisEdge());
        //Left Triangle
        if (marker.isDrawStartTriangle()) {
            final GeneralPath leftTriangle = new GeneralPath();
            leftTriangle.moveTo(dataArea.getMinX() + (marker.getTriangleWidth() / 2.0), v);
            leftTriangle.lineTo(dataArea.getMinX(), v - (marker.getTriangleHeight() / 2.0));
            leftTriangle.lineTo(dataArea.getMinX(), v + (marker.getTriangleHeight() / 2.0));
            drawGeneralPath(marker, g2, leftTriangle);
        }

        //Right Triangle
        if (marker.isDrawEndTriangle()) {
            final GeneralPath rightTriangle = new GeneralPath();
            rightTriangle.moveTo(dataArea.getMaxX() - (marker.getTriangleWidth() / 2.0), v);
            rightTriangle.lineTo(dataArea.getMaxX(), v - (marker.getTriangleHeight() / 2.0));
            rightTriangle.lineTo(dataArea.getMaxX(), v + (marker.getTriangleHeight() / 2.0));
            drawGeneralPath(marker, g2, rightTriangle);
        }
    }

    public static void drawDomainTriangles(final TriangleLineValueMarker marker, final Graphics2D g2, final XYPlot plot,
            final ValueAxis domainAxis, final Rectangle2D dataArea) {
        if (PlotOrientation.HORIZONTAL.equals(plot.getOrientation())) {
            throw new UnsupportedOperationException(
                    "PlotOrientation " + PlotOrientation.HORIZONTAL + " not supported yet for TriangleValueMarker");
        }
        final double v = domainAxis.valueToJava2D(marker.getValue(), dataArea, plot.getDomainAxisEdge());
        //Top Triangle
        if (marker.isDrawStartTriangle()) {
            final GeneralPath topTriangle = new GeneralPath();
            topTriangle.moveTo(v, dataArea.getMinY() + (marker.getTriangleHeight() / 2.0));
            topTriangle.lineTo(v - (marker.getTriangleWidth() / 2), dataArea.getMinY());
            topTriangle.lineTo(v + (marker.getTriangleWidth() / 2), dataArea.getMinY());
            drawGeneralPath(marker, g2, topTriangle);
        }

        //Bottom Triangle
        if (marker.isDrawEndTriangle()) {
            final GeneralPath bottomTriangle = new GeneralPath();
            bottomTriangle.moveTo(v, dataArea.getMaxY() - (marker.getTriangleHeight() / 2.0));
            bottomTriangle.lineTo(v - (marker.getTriangleWidth() / 2), dataArea.getMaxY());
            bottomTriangle.lineTo(v + (marker.getTriangleWidth() / 2), dataArea.getMaxY());
            drawGeneralPath(marker, g2, bottomTriangle);
        }
    }

    private static void drawGeneralPath(final TriangleLineValueMarker marker, final Graphics2D g2,
            final GeneralPath generalPath) {
        generalPath.setWindingRule(Path2D.WIND_EVEN_ODD);
        generalPath.closePath();
        g2.setPaint(marker.getTrianglePaint());
        g2.fill(generalPath);
        g2.draw(generalPath);
    }
}
