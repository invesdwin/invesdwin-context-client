package de.invesdwin.context.client.swing.jfreechart.plot.axis;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.Range;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.crosshair.PlotCrosshairHelper;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.listener.IRangeListener;
import de.invesdwin.util.lang.color.Colors;

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

        //RectangleCoords
        final float[] anchorPoint = calculateAnchorPoint(cursor, dataArea, edge, TextAnchor.BOTTOM_CENTER, markerValue);

        //Draw the background
        final Rectangle2D stringBounds = g2.getFontMetrics().getStringBounds(labelText, g2);
        final int width = (int) stringBounds.getWidth() + BACKGROUND_RECTANGLE_ADDED_WIDTH;

        final Rectangle2D chartArea = chartPanel.getChartPanel().getChartRenderingInfo().getChartArea();
        //Different TextAnchor depending on the edge necessary: see NumberAxis.refreshTicksVertical()
        final TextAnchor textAnchor;
        final int x = (int) anchorPoint[0] - width / 2 - BACKGROUND_RECTANGLE_ADDED_WIDTH / 2;

        if (RectangleEdge.BOTTOM.equals(edge)) {
            final int height = (int) (chartArea.getHeight() - cursor);
            final int y = (int) cursor;
            //Background-Rectangle
            g2.setColor(PlotCrosshairHelper.CROSSHAIR_COLOR);
            g2.fillRect(x, y, width + BACKGROUND_RECTANGLE_ADDED_WIDTH, height);
            textAnchor = TextAnchor.TOP_CENTER;
        } else {
            throw new UnsupportedOperationException("Label rendering not supported for: " + edge);
        }

        //Draw the text
        g2.setColor(Colors.getContrastColor(PlotCrosshairHelper.CROSSHAIR_COLOR));
        TextUtils.drawAlignedString(labelText, g2, anchorPoint[0], anchorPoint[1], textAnchor);
    }
}
