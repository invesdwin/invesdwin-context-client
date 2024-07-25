package de.invesdwin.context.client.swing.jfreechart.plot;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import javax.annotation.concurrent.Immutable;

import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;

@Immutable
public final class Annotations {

    private Annotations() {}

    public static Shape calculateShape(final Graphics2D g2, final ValueAxis domainAxis, final Rectangle2D dataArea,
            final RectangleEdge domainEdge, final RectangleEdge rangeEdge, final ValueAxis rangeAxis,
            final XYTextAnnotation annotation) {
        final float anchorX = (float) domainAxis.valueToJava2D(annotation.getX(), dataArea, domainEdge);
        final float anchorY = (float) rangeAxis.valueToJava2D(annotation.getY(), dataArea, rangeEdge);
        return TextUtils.calculateRotatedStringBounds(annotation.getText(), g2, anchorX, anchorY,
                annotation.getTextAnchor(), annotation.getRotationAngle(), annotation.getRotationAnchor());
    }

}
