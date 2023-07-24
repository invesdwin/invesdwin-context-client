package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.marker;

import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import javax.annotation.concurrent.Immutable;

import org.jfree.chart.plot.ValueMarker;

@Immutable
public class TriangleLineValueMarker extends ValueMarker {

    private final double triangleWidth;
    private final double triangleHeight;
    private final Paint trianglePaint;
    private final boolean drawStartTriangle;
    private final boolean drawEndTriangle;

    public TriangleLineValueMarker(final double value, final Paint paint, final Stroke stroke,
            final double triangleWidth, final double triangleHeight, final Paint trianglePaint) {
        this(value, paint, stroke, triangleWidth, triangleHeight, trianglePaint, true, true);
    }

    public TriangleLineValueMarker(final double value, final Paint paint, final Stroke stroke,
            final double triangleWidth, final double triangleHeight, final Paint trianglePaint,
            final boolean drawStartTriangle, final boolean drawEndTriangle) {
        super(value, paint, stroke, paint, stroke, 1.0f);
        this.triangleWidth = triangleWidth;
        this.triangleHeight = triangleHeight;
        this.trianglePaint = trianglePaint;
        this.drawStartTriangle = drawStartTriangle;
        this.drawEndTriangle = drawEndTriangle;
    }

    public GeneralPath getTriangle(final Rectangle2D dataArea) {
        final GeneralPath triangle = new GeneralPath();
        triangle.setWindingRule(Path2D.WIND_EVEN_ODD);
        triangle.moveTo(getValue(), dataArea.getMaxY() - (triangleHeight / 2.0));
        triangle.lineTo(getValue() - (triangleWidth / 2), dataArea.getMaxY());
        triangle.lineTo(getValue() + (triangleWidth / 2), dataArea.getMaxY());
        triangle.lineTo(getValue(), dataArea.getMaxY() - (triangleHeight / 2.0));
        triangle.closePath();
        return triangle;
    }

    public double getTriangleWidth() {
        return triangleWidth;
    }

    public double getTriangleHeight() {
        return triangleHeight;
    }

    public Paint getTrianglePaint() {
        return trianglePaint;
    }

    public boolean isDrawStartTriangle() {
        return drawStartTriangle;
    }

    public boolean isDrawEndTriangle() {
        return drawEndTriangle;
    }
}
