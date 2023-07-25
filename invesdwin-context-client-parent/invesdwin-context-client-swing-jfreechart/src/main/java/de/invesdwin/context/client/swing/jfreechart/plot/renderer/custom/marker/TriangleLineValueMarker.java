package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.marker;

import java.awt.Paint;
import java.awt.Stroke;

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
