package de.invesdwin.context.client.swing.jfreechart.plot.axis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.Range;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.listener.IRangeListener;

@NotThreadSafe
public class CustomDomainNumberAxis extends ACustomNumberAxis {

    public static final int BACKGROUND_RECTANGLE_ADDED_WIDTH = 10;

    private final InteractiveChartPanel chartPanel;

    public CustomDomainNumberAxis(final InteractiveChartPanel chartPanel) {
        this.chartPanel = chartPanel;
    }

    @Override
    public void setRange(final Range range, final boolean turnOffAutoRange, final boolean notify) {
        final boolean changed = !range.equals(getRange());
        super.setRange(range, turnOffAutoRange, notify);
        if (changed) {
            final Set<IRangeListener> rangeListeners = chartPanel.getPlotZoomHelper().getRangeListeners();
            if (!rangeListeners.isEmpty()) {
                for (final IRangeListener l : rangeListeners) {
                    l.onRangeChanged(range);
                }
            }
            chartPanel.configureRangeAxis();
            final boolean navigationUpdated = chartPanel.getPlotPanHelper().maybeToggleVisibilityPanLiveIcon();
            if (!navigationUpdated) {
                //Update for Pan/Zoom-Icon's if not alreay called by panLiveVisibility-handling
                chartPanel.getPlotNavigationHelper().updateNavigation();
            }
        }
    }

    @Override
    public AxisState draw(final Graphics2D g2, final double cursor, final Rectangle2D plotArea,
            final Rectangle2D dataArea, final RectangleEdge edge, final PlotRenderingInfo plotState) {
        final AxisState axisState = super.draw(g2, cursor, plotArea, dataArea, edge, plotState);
        drawDomainCrosshairLabel(g2, cursor, plotArea, dataArea, edge, plotState);
        return axisState;
    }

    private void drawDomainCrosshairLabel(final Graphics2D g2, final double cursor, final Rectangle2D plotArea,
            final Rectangle2D dataArea, final RectangleEdge edge, final PlotRenderingInfo plotState) {
        final double markerValue = chartPanel.getPlotCrosshairHelper().getLastDomainCrosshairMarker().getAxisValue();
        final String labelText = chartPanel.getPlotCrosshairHelper().getLastDomainCrosshairMarker().getAxisLabel();

        if (markerValue == -1D || labelText == null) {
            return;
        }
        final Color panelBackgroundColor = (Color) chartPanel.getChart().getBackgroundPaint();

        //RectangleCoords
        final float[] anchorPoint = calculateAnchorPoint(cursor, dataArea, edge, TextAnchor.BOTTOM_CENTER, markerValue);

        //Draw the background
        final Rectangle2D stringBounds = g2.getFontMetrics().getStringBounds(labelText, g2);
        final int width = (int) stringBounds.getWidth() + BACKGROUND_RECTANGLE_ADDED_WIDTH;

        final Rectangle2D chartArea = chartPanel.getChartPanel().getChartRenderingInfo().getChartArea();
        //Different TextAnchor depending on the edge necessary: see NumberAxis.refreshTicksVertical()
        final TextAnchor textAnchor;
        final int x = (int) anchorPoint[0] - width / 2 - BACKGROUND_RECTANGLE_ADDED_WIDTH / 2;

        //make overpaint 1 pixel smaller so that tick labels are cut off smoother
        final int xOverpaint = x + BACKGROUND_RECTANGLE_OFFSET;
        final int widthOverpaint = width - BACKGROUND_RECTANGLE_OFFSET - BACKGROUND_RECTANGLE_OFFSET;
        if (RectangleEdge.BOTTOM.equals(edge)) {
            final int height = (int) (chartArea.getHeight() - cursor);
            final int y = (int) cursor;
            //Paint a Rectangle to overpaint the axis-tick-label
            g2.setColor(panelBackgroundColor);
            g2.fillRect(xOverpaint, y, widthOverpaint, height);
            //Background-Rectangle
            g2.setColor(BACKGROUND_RECTANGLE_COLOR);
            g2.fillRect(x, y, width + BACKGROUND_RECTANGLE_ADDED_WIDTH, height);
            textAnchor = TextAnchor.TOP_CENTER;
        } else {
            throw new UnsupportedOperationException("Label rendering not supported for: " + edge);
        }

        drawLabelText(g2, labelText, anchorPoint, textAnchor);
    }
}
