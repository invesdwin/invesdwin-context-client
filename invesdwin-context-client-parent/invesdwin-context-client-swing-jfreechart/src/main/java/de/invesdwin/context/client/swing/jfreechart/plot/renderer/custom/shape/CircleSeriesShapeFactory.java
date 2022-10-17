package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.shape;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.LineWidthType;
import de.invesdwin.util.swing.HiDPI;

@Immutable
public final class CircleSeriesShapeFactory implements ISeriesShapeFactory {

    public static final CircleSeriesShapeFactory INSTANCE = new CircleSeriesShapeFactory();

    private CircleSeriesShapeFactory() {}

    @Override
    public Shape newShape(final LineWidthType lineWidthType) {
        final float width = HiDPI.scale(lineWidthType.getWidth());
        final float adjWidth = width * 1.5f + 1f;
        final float offset = -(adjWidth / 2f);
        final Shape circle = new Ellipse2D.Float(offset, offset, adjWidth, adjWidth);
        return circle;
    }

}
