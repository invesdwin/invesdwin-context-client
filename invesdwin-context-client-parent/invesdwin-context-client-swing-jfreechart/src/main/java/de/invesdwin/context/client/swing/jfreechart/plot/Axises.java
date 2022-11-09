package de.invesdwin.context.client.swing.jfreechart.plot;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.annotation.concurrent.Immutable;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.AxisDragInfo;

@Immutable
public final class Axises {

    private Axises() {

    }

    /**
     * Copied from org.jfree.chart.plot.PlotRenderingInfo.getSubplotIndex(Point2D source) and modified since we need to
     * check the PlotArea (not the DataArea) when the mouse is hovered over the axis and not the actual data-area.
     */
    public static int getSubplotIndexFromPlotArea(final InteractiveChartPanel chartPanel, final Point2D point2d) {
        final PlotRenderingInfo plotInfo = chartPanel.getChartPanel().getChartRenderingInfo().getPlotInfo();
        for (int i = 0; i < plotInfo.getSubplotCount(); i++) {
            final PlotRenderingInfo subPlotInfo = plotInfo.getSubplotInfo(i);
            final Rectangle2D area = subPlotInfo.getPlotArea();
            if (area != null && area.contains(point2d)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if the given Point2D is in the axis-area.
     */
    public static boolean isAxisArea(final InteractiveChartPanel chartPanel, final Point2D point2d) {
        return isAxisArea(chartPanel, point2d, true);
    }

    private static boolean isAxisArea(final InteractiveChartPanel chartPanel, final Point2D point2d,
            final boolean checkRangeAxisExists) {
        final PlotRenderingInfo plotInfo = chartPanel.getChartPanel().getChartRenderingInfo().getPlotInfo();
        for (int i = 0; i < plotInfo.getSubplotCount(); i++) {
            final PlotRenderingInfo subPlotRenderingInfo = plotInfo.getSubplotInfo(i);
            final Rectangle2D dataArea = subPlotRenderingInfo.getDataArea();
            final Rectangle2D plotArea = subPlotRenderingInfo.getPlotArea();
            if (dataArea != null && plotArea != null && !dataArea.contains(point2d) && plotArea.contains(point2d)) {
                if (checkRangeAxisExists) {
                    return getRangeAxis(chartPanel, point2d, i) != null;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the given Point2D is in the axis-area.
     */
    public static AxisLocation getAxisLocation(final InteractiveChartPanel chartPanel, final Point2D point2d) {
        if (!isAxisArea(chartPanel, point2d, false)) {
            return null;
        } else {
            final PlotRenderingInfo plotInfo = chartPanel.getChartPanel().getChartRenderingInfo().getPlotInfo();
            return plotInfo.getPlotArea().getCenterX() >= point2d.getX() ? AxisLocation.TOP_OR_LEFT
                    : AxisLocation.TOP_OR_RIGHT;
        }
    }

    public static ValueAxis getRangeAxis(final InteractiveChartPanel chartPanel, final Point2D point2d) {
        final int subplotIndex = Axises.getSubplotIndexFromPlotArea(chartPanel, point2d);
        return getRangeAxis(chartPanel, point2d, subplotIndex);
    }

    private static ValueAxis getRangeAxis(final InteractiveChartPanel chartPanel, final Point2D point2d,
            final int subplotIndex) {
        return Axises.getRangeAxis(chartPanel, point2d, chartPanel.getCombinedPlot().getSubplots().get(subplotIndex));
    }

    /**
     * return the RangeAxis to the corresponding mouse-coordinates. There can be several axis attached to one plot which
     * might be visible/invisible and attached to either the left - or right-side of the plot.
     */
    public static ValueAxis getRangeAxis(final InteractiveChartPanel chartPanel, final Point2D point2d,
            final XYPlot xyPlot) {
        final AxisLocation axisLocationForPoint2D = getAxisLocation(chartPanel, point2d);
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
     * creates a container containing Information about the plot/axis when a mouse-drag started.
     */
    public static AxisDragInfo createAxisDragInfo(final InteractiveChartPanel chartPanel, final Point2D point2D) {
        final int subplotIndex = Axises.getSubplotIndexFromPlotArea(chartPanel, point2D);
        final ValueAxis rangeAxis = getRangeAxis(chartPanel, point2D, subplotIndex);
        final PlotRenderingInfo plotRenderingInfo = chartPanel.getChartPanel().getChartRenderingInfo().getPlotInfo();
        final double roundedPlotHeight = Math
                .round(plotRenderingInfo.getSubplotInfo(subplotIndex).getPlotArea().getHeight());
        return new AxisDragInfo(point2D, rangeAxis, subplotIndex, roundedPlotHeight);
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
