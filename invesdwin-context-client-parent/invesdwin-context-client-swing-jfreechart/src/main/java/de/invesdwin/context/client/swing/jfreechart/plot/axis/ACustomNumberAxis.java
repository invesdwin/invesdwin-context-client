package de.invesdwin.context.client.swing.jfreechart.plot.axis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.crosshair.PlotCrosshairHelper;
import de.invesdwin.util.lang.color.Colors;

@NotThreadSafe
public abstract class ACustomNumberAxis extends NumberAxis {

    public static final int BACKGROUND_RECTANGLE_OFFSET = 1;
    public static final Color BACKGROUND_RECTANGLE_COLOR = Color.DARK_GRAY;

    protected float[] calculateAnchorPoint(final double cursor, final Rectangle2D dataArea, final RectangleEdge edge,
            final TextAnchor textAnchor, final double rangeValue) {
        final NumberTick tick = new NumberTick(rangeValue, String.valueOf(rangeValue), textAnchor, textAnchor, 0.0);
        final float[] anchorPoint = calculateAnchorPoint(tick, cursor, dataArea, edge);
        return anchorPoint;
    }

    protected void drawLabelText(final Graphics2D g2, final String labelText, final float[] anchorPoint,
            final TextAnchor textAnchor) {
        //Draw the text
        g2.setColor(Colors.getContrastColor(PlotCrosshairHelper.CROSSHAIR_COLOR));
        TextUtils.drawAlignedString(labelText, g2, anchorPoint[0], anchorPoint[1], textAnchor);
    }
}
