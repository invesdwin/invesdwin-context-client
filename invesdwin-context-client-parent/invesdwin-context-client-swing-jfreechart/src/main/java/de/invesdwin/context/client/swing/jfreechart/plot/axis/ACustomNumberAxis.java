package de.invesdwin.context.client.swing.jfreechart.plot.axis;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.crosshair.PlotCrosshairHelper;
import de.invesdwin.context.client.swing.jfreechart.plot.XYPlots;
import de.invesdwin.util.lang.color.Colors;

@NotThreadSafe
public abstract class ACustomNumberAxis extends NumberAxis {

    public static final int BACKGROUND_RECTANGLE_ADDED_HEIGHT = 2;
    public static final int BACKGROUND_RECTANGLE_OFFSET = 1;

    /**
     * Draw Range-Crosshair-Label's on the range-axis'es.The crosshair-lines get drawn in the PlotCrosshairHelper.
     *
     */

    protected void drawRangeCrosshairLabels(final Graphics2D g2, final double cursor, final Rectangle2D dataArea,
            final RectangleEdge edge) {
        final XYPlot plot = (XYPlot) getPlot();
        final InteractiveChartPanel chartPanel = XYPlots.getChartPanel(plot);
        final PlotCrosshairHelper plotCrosshairHelper = chartPanel.getPlotCrosshairHelper();

        if (RectangleEdge.LEFT.equals(edge)) {
            final double rangeCrosshairMarkerLeftValue = plotCrosshairHelper.getRangeCrosshairMarkerLeft()
                    .getAxisValue();
            final String rangeCrosshairMarkerLeftLabel = plotCrosshairHelper.getRangeCrosshairMarkerLeft()
                    .getAxisLabel();
            final XYPlot markerPlot = plotCrosshairHelper.getRangeCrosshairMarkerLeft().getCurrentPlot();

            drawLabel(g2, cursor, dataArea, edge, chartPanel, rangeCrosshairMarkerLeftValue,
                    rangeCrosshairMarkerLeftLabel, plot, markerPlot);
        } else if (RectangleEdge.RIGHT.equals(edge)) {
            final double rangeCrosshairMarkerRightValue = plotCrosshairHelper.getRangeCrosshairMarkerRight()
                    .getAxisValue();
            final String rangeCrosshairMarkerRightLabel = plotCrosshairHelper.getRangeCrosshairMarkerRight()
                    .getAxisLabel();
            final XYPlot markerPlot = plotCrosshairHelper.getRangeCrosshairMarkerRight().getCurrentPlot();

            drawLabel(g2, cursor, dataArea, edge, chartPanel, rangeCrosshairMarkerRightValue,
                    rangeCrosshairMarkerRightLabel, plot, markerPlot);
        } else {
            throw new UnsupportedOperationException("LabelRendering not supported for: " + edge);
        }
    }

    protected void drawLabel(final Graphics2D g2, final double cursor, final Rectangle2D dataArea,
            final RectangleEdge edge, final InteractiveChartPanel chartPanel, final double rangeValue,
            final String formattedText, final XYPlot plot, final XYPlot markerPlot) {

        if (rangeValue == -1D || formattedText == null || !plot.equals(markerPlot)) {
            return;
        }

        //RectangleCoords
        final float[] anchorPoint = calculateAnchorPoint(cursor, dataArea, edge, TextAnchor.CENTER_LEFT, rangeValue);

        //Draw the background
        final int height = g2.getFontMetrics().getHeight() + BACKGROUND_RECTANGLE_ADDED_HEIGHT;
        final int width;
        final Rectangle2D chartArea = chartPanel.getChartPanel().getChartRenderingInfo().getChartArea();
        //Different TextAnchor depending on the edge necessary: see NumberAxis.refreshTicksVertical()
        final TextAnchor textAnchor;
        final int y = (int) anchorPoint[1] - (height / 2) + BACKGROUND_RECTANGLE_OFFSET;
        if (RectangleEdge.LEFT.equals(edge)) {
            width = (int) (cursor - chartArea.getX());
            final int x = (int) chartArea.getX();
            //Background-Rectangle
            g2.setColor(PlotCrosshairHelper.CROSSHAIR_COLOR);
            g2.fillRect(x, y, width, height);
            textAnchor = TextAnchor.CENTER_RIGHT;
        } else if (RectangleEdge.RIGHT.equals(edge)) {
            final int x = (int) cursor;
            width = (int) (chartArea.getWidth() - cursor);
            //Background-Rectangle
            g2.setColor(PlotCrosshairHelper.CROSSHAIR_COLOR);
            g2.fillRect(x, y, width, height);
            textAnchor = TextAnchor.CENTER_LEFT;
        } else {
            throw new UnsupportedOperationException("Label rendering not supported for: " + edge);
        }

        //Draw the text
        g2.setColor(Colors.getContrastColor(PlotCrosshairHelper.CROSSHAIR_COLOR));
        TextUtils.drawAlignedString(formattedText, g2, anchorPoint[0], anchorPoint[1], textAnchor);
    }

    protected float[] calculateAnchorPoint(final double cursor, final Rectangle2D dataArea, final RectangleEdge edge,
            final TextAnchor textAnchor, final double rangeValue) {
        final NumberTick tick = new NumberTick(rangeValue, String.valueOf(rangeValue), textAnchor, textAnchor, 0.0);
        final float[] anchorPoint = calculateAnchorPoint(tick, cursor, dataArea, edge);
        return anchorPoint;
    }
}
