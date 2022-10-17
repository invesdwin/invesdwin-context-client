package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.shape;

import java.awt.Shape;

import javax.annotation.concurrent.Immutable;

import org.jfree.chart.util.ShapeUtils;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.LineWidthType;
import de.invesdwin.util.swing.HiDPI;

@Immutable
public final class CrossSeriesShapeFactory implements ISeriesShapeFactory {

    public static final CrossSeriesShapeFactory INSTANCE = new CrossSeriesShapeFactory();

    private CrossSeriesShapeFactory() {}

    @Override
    public Shape newShape(final LineWidthType lineWidthType) {
        final float width = HiDPI.scale(lineWidthType.getWidth());
        final float length = width * 1.5f;
        final float thickness = 0.25f + 0.25f * width;
        final Shape cross = ShapeUtils.createRegularCross(length, thickness);
        return cross;
    }

}
