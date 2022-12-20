package de.invesdwin.context.client.swing.jfreechart.plot;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.annotation.concurrent.Immutable;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.RangeType;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.AxisDragInfo;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;

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
     * Checks if the given Point2D is in the domain- or range-axis-area.
     */
    public static boolean isAxisArea(final InteractiveChartPanel chartPanel, final Point2D point2d) {
        return getAxisForMousePosition(chartPanel, point2d) != null;
    }

    /**
     * Checks if the given Point2D is in the range-axis-area.
     */
    public static boolean isRangeAxisArea(final InteractiveChartPanel chartPanel, final Point2D point2d) {
        return isRangeAxisArea(chartPanel, point2d, true);
    }

    private static boolean isRangeAxisArea(final InteractiveChartPanel chartPanel, final Point2D point2d,
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
     * Checks if the given Point2D is in the range-axis-area and returns the AxisLocation.
     */
    public static AxisLocation getAxisLocation(final InteractiveChartPanel chartPanel, final Point2D point2d) {
        if (!isRangeAxisArea(chartPanel, point2d, false)) {
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

    public static ValueAxis getRangeAxis(final InteractiveChartPanel chartPanel, final Point2D point2d,
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
                if (rangeAxis != null && rangeAxis.isVisible() && axisLocationForPoint2D.equals(axisLocation)) {
                    return rangeAxis;
                }
            }
        }
        return null;
    }

    /**
     * creates a container containing Information about the plot/axis when a mouse-drag started.
     */
    public static AxisDragInfo createAxisDragInfo(final InteractiveChartPanel chartPanel, final Point2D point2D,
            final Axis axis) {
        final PlotRenderingInfo plotRenderingInfo = chartPanel.getChartPanel().getChartRenderingInfo().getPlotInfo();
        if (Axis.DOMAIN_AXIS.equals(axis)) {
            final ValueAxis domainAxis = chartPanel.getCombinedPlot().getDomainAxis();
            final double roundedPlotWidth = Math.round(plotRenderingInfo.getPlotArea().getWidth());
            return new AxisDragInfo(point2D, domainAxis, roundedPlotWidth, axis);
        } else if (Axis.RANGE_AXIS.equals(axis)) {
            final int subplotIndex = Axises.getSubplotIndexFromPlotArea(chartPanel, point2D);
            final ValueAxis rangeAxis = getRangeAxis(chartPanel, point2D, subplotIndex);
            if (rangeAxis != null) {
                final double roundedPlotHeight = Math
                        .round(plotRenderingInfo.getSubplotInfo(subplotIndex).getPlotArea().getHeight());
                return new AxisDragInfo(point2D, rangeAxis, subplotIndex, roundedPlotHeight, axis);
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
            if (rangeAxis != null && !rangeAxis.isAutoRange()) {
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
                final ValueAxis rangeAxis = xyPlot.getRangeAxis(i);
                if (rangeAxis != null) {
                    rangeAxis.setAutoRange(true);
                }
            }
        }
    }

    public static String getRangeAxisId(final ValueAxis rangeAxis) {
        final XYPlot xyPlot = (XYPlot) rangeAxis.getPlot();
        for (int i = 0; i < xyPlot.getDatasetCount(); i++) {
            final IPlotSourceDataset xyDataset = (IPlotSourceDataset) xyPlot.getDataset(i);
            if (rangeAxis.equals(xyPlot.getRangeAxisForDataset(i))) {
                return xyDataset.getRangeAxisId();
            }
        }
        return null;
    }

    /**
     * Return die Axis (domain or range) for the given information
     */
    public static Axis getAxisForMousePosition(final InteractiveChartPanel chartPanel, final Point2D point) {
        final PlotRenderingInfo plotInfo = chartPanel.getChartPanel().getChartRenderingInfo().getPlotInfo();
        if (plotInfo.getDataArea() != null && !plotInfo.getDataArea().contains(point) && plotInfo.getPlotArea() != null
                && plotInfo.getPlotArea().contains(point)) {
            if (Axises.getSubplotIndexFromPlotArea(chartPanel, point) == -1) {
                return Axis.DOMAIN_AXIS;
            } else {
                return Axis.RANGE_AXIS;
            }
        }
        return null;
    }

    /**
     * Caluclates the autoRange-Range and returns its center. Method mostly copied from NumberAxis.autoAdjustRange().
     */
    //CHECKSTYLE:OFF
    public static Range calculateAutoRange(final ValueAxis rangeAxis) {
        if (!(rangeAxis instanceof NumberAxis)) {
            throw new UnsupportedOperationException("No support for: " + rangeAxis.getClass().getSimpleName());
        }
        final NumberAxis numberAxis = (NumberAxis) rangeAxis;

        final Plot plot = numberAxis.getPlot();
        if (plot == null) {
            return null; // no plot, no data
        }

        if (plot instanceof ValueAxisPlot) {
            final ValueAxisPlot vap = (ValueAxisPlot) plot;

            Range r = vap.getDataRange(numberAxis);
            if (r == null) {
                r = numberAxis.getDefaultAutoRange();
            }

            double upper = r.getUpperBound();
            double lower = r.getLowerBound();
            if (numberAxis.getRangeType() == RangeType.POSITIVE) {
                lower = Math.max(0.0, lower);
                upper = Math.max(0.0, upper);
            } else if (numberAxis.getRangeType() == RangeType.NEGATIVE) {
                lower = Math.min(0.0, lower);
                upper = Math.min(0.0, upper);
            }

            if (numberAxis.getAutoRangeIncludesZero()) {
                lower = Math.min(lower, 0.0);
                upper = Math.max(upper, 0.0);
            }
            final double range = upper - lower;

            // if fixed auto range, then derive lower bound...
            final double fixedAutoRange = numberAxis.getFixedAutoRange();
            if (fixedAutoRange > 0.0) {
                lower = upper - fixedAutoRange;
            } else {
                // ensure the autorange is at least <minRange> in size...
                final double minRange = numberAxis.getAutoRangeMinimumSize();
                if (range < minRange) {
                    final double expand = (minRange - range) / 2;
                    upper = upper + expand;
                    lower = lower - expand;
                    if (lower == upper) { // see bug report 1549218
                        final double adjust = Math.abs(lower) / 10.0;
                        lower = lower - adjust;
                        upper = upper + adjust;
                    }
                    if (numberAxis.getRangeType() == RangeType.POSITIVE) {
                        if (lower < 0.0) {
                            upper = upper - lower;
                            lower = 0.0;
                        }
                    } else if (numberAxis.getRangeType() == RangeType.NEGATIVE) {
                        if (upper > 0.0) {
                            lower = lower - upper;
                            upper = 0.0;
                        }
                    }
                }

                if (numberAxis.getAutoRangeStickyZero()) {
                    if (upper <= 0.0) {
                        upper = Math.min(0.0, upper + numberAxis.getUpperMargin() * range);
                    } else {
                        upper = upper + numberAxis.getUpperMargin() * range;
                    }
                    if (lower >= 0.0) {
                        lower = Math.max(0.0, lower - numberAxis.getLowerMargin() * range);
                    } else {
                        lower = lower - numberAxis.getLowerMargin() * range;
                    }
                } else {
                    upper = upper + numberAxis.getUpperMargin() * range;
                    lower = lower - numberAxis.getLowerMargin() * range;
                }
            }

            return new Range(lower, upper);
            //            setRange(new Range(lower, upper), false, false);
        }
        return null;
    }
    //CHECKSTYLE:ON
}
