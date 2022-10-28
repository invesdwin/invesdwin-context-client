package de.invesdwin.context.client.swing.jfreechart.plot;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.annotation.concurrent.Immutable;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.util.Args;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;

@Immutable
public final class Axises {

    private Axises() {

    }

    /**
     * Copied from org.jfree.chart.plot.PlotRenderingInfo.getSubplotIndex(Point2D source) and modified since we need to
     * check the PlotArea (not the DataArea) when the mouse is hovered over the axis and not the actual data-area.
     */

    public static int getSubplotIndexFromPlotArea(final PlotRenderingInfo info, final Point2D point2D) {
        Args.nullNotPermitted(info, "info");
        for (int i = 0; i < info.getSubplotCount(); i++) {
            final PlotRenderingInfo subPlotInfo = info.getSubplotInfo(i);
            final Rectangle2D area = subPlotInfo.getPlotArea();
            if (area != null && area.contains(point2D)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if the given Point2D is in the axis-area.
     */
    public static boolean isAxisArea(final PlotRenderingInfo plotInfo, final Point2D point2d) {
        return !plotInfo.getDataArea().contains(point2d) && plotInfo.getPlotArea().contains(point2d);
    }

    /**
     * Checks if the given Point2D is in the axis-area.
     */
    public static AxisLocation getAxisLocation(final PlotRenderingInfo plotInfo, final Point2D point2d) {
        return isAxisArea(plotInfo, point2d)
                ? plotInfo.getPlotArea().getCenterX() >= point2d.getX() ? AxisLocation.TOP_OR_LEFT
                        : AxisLocation.TOP_OR_RIGHT
                : null;
    }

    public static ValueAxis getRangeAxis(final InteractiveChartPanel chartPanel, final Point2D point2D) {
        final PlotRenderingInfo plotInfo = chartPanel.getChartPanel().getChartRenderingInfo().getPlotInfo();
        final int subplotIndex = Axises.getSubplotIndexFromPlotArea(plotInfo, point2D);
        final XYPlot xyPlot = chartPanel.getCombinedPlot().getSubplots().get(subplotIndex);
        return Axises.getRangeAxis(plotInfo, point2D, xyPlot);
    }

    /**
     * return the RangeAxis to the corresponding mouse-coordinates. There can be several axis attached to one plot which
     * might be visible/invisible and attached to either the left - or right-side of the plot.
     */

    public static ValueAxis getRangeAxis(final PlotRenderingInfo plotInfo, final Point2D point2d, final XYPlot xyPlot) {
        final AxisLocation axisLocationForPoint2D = getAxisLocation(plotInfo, point2d);
        if (axisLocationForPoint2D != null) {
            for (int i = 0; i < xyPlot.getRangeAxisCount(); i++) {
                final ValueAxis rangeAxis = xyPlot.getRangeAxis(i);
                final AxisLocation axisLocation = xyPlot.getRangeAxisLocation(i);
                if (rangeAxis.isVisible() && axisLocationForPoint2D.equals(axisLocation)) {
                    return rangeAxis;
                }
            }
        }
        return null;
    }

    /**
     * Checks if every Axis/Indicator in the plot is in AutoRange-Mode.
     */

    public static boolean isEveryAxisAutoRange(final XYPlot xyPlot) {
        for (int i = 0; i < xyPlot.getRangeAxisCount(); i++) {
            final ValueAxis rangeAxis = xyPlot.getRangeAxis(i);
            if (!rangeAxis.isAutoRange()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets all the autoRange-Parameters on every axis of the plots/sublots back to true.
     */

    public static void resetAllAutoRanges(final InteractiveChartPanel chartPanel) {
        for (final XYPlot xyPlot : chartPanel.getCombinedPlot().getSubplots()) {
            for (int i = 0; i < xyPlot.getRangeAxisCount(); i++) {
                xyPlot.getRangeAxis(i).setAutoRange(true);
            }
        }
    }
}
