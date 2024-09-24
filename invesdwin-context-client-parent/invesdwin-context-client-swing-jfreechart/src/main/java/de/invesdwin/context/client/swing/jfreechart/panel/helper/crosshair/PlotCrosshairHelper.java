package de.invesdwin.context.client.swing.jfreechart.panel.helper.crosshair;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.math.RoundingMode;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;

import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;
import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.plot.Markers;
import de.invesdwin.context.client.swing.jfreechart.plot.XYPlots;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.XYNoteIconAnnotation;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.XYPriceLineAnnotation;
import de.invesdwin.util.lang.color.Colors;
import de.invesdwin.util.math.Doubles;

@NotThreadSafe
public class PlotCrosshairHelper {
    public static final Cursor CROSSHAIR_CURSOR = new Cursor(Cursor.CROSSHAIR_CURSOR);

    private static final Font CROSSHAIR_FONT = XYPriceLineAnnotation.FONT;
    private static final Color CROSSHAIR_COLOR = Color.BLACK;
    private static final Stroke CROSSHAIR_STROKE = new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
            0, new float[] { 5, 6 }, 0);

    private final InteractiveChartPanel chartPanel;
    private final ValueMarker domainCrosshairMarker;
    private final ValueMarker lastDomainCrosshairMarker;
    private final ValueMarker rangeCrosshairMarkerRight;
    private final ValueMarker rangeCrosshairMarkerLeft;
    private int crosshairLastMouseX;
    private int crosshairLastMouseY;

    public PlotCrosshairHelper(final InteractiveChartPanel chartPanel) {
        this.chartPanel = chartPanel;

        domainCrosshairMarker = new ValueMarker(-1D);
        domainCrosshairMarker.setStroke(CROSSHAIR_STROKE);
        domainCrosshairMarker.setPaint(CROSSHAIR_COLOR);
        domainCrosshairMarker.setLabelFont(CROSSHAIR_FONT);
        domainCrosshairMarker.setLabelPaint(CROSSHAIR_COLOR);
        domainCrosshairMarker.setLabelBackgroundColor(Colors.INVISIBLE_COLOR);
        domainCrosshairMarker.setLabelAnchor(RectangleAnchor.BOTTOM);
        domainCrosshairMarker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
        domainCrosshairMarker.setLabelOffset(new RectangleInsets(0, 4, 2, 0));
        try {
            lastDomainCrosshairMarker = (ValueMarker) domainCrosshairMarker.clone();
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        rangeCrosshairMarkerRight = new ValueMarker(-1D);
        rangeCrosshairMarkerRight.setStroke(CROSSHAIR_STROKE);
        rangeCrosshairMarkerRight.setPaint(CROSSHAIR_COLOR);
        rangeCrosshairMarkerRight.setLabelFont(CROSSHAIR_FONT);
        rangeCrosshairMarkerRight.setLabelPaint(CROSSHAIR_COLOR);
        rangeCrosshairMarkerRight.setLabelBackgroundColor(Colors.INVISIBLE_COLOR);
        rangeCrosshairMarkerRight.setLabelAnchor(RectangleAnchor.RIGHT);
        rangeCrosshairMarkerRight.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
        rangeCrosshairMarkerRight.setLabelOffset(new RectangleInsets(0, 0, 1, 1));
        rangeCrosshairMarkerLeft = new ValueMarker(0D);
        rangeCrosshairMarkerLeft.setStroke(CROSSHAIR_STROKE);
        rangeCrosshairMarkerLeft.setPaint(Colors.INVISIBLE_COLOR);
        rangeCrosshairMarkerLeft.setLabelFont(CROSSHAIR_FONT);
        rangeCrosshairMarkerLeft.setLabelPaint(CROSSHAIR_COLOR);
        rangeCrosshairMarkerLeft.setLabelBackgroundColor(Colors.INVISIBLE_COLOR);
        rangeCrosshairMarkerLeft.setLabelAnchor(RectangleAnchor.LEFT);
        rangeCrosshairMarkerLeft.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        rangeCrosshairMarkerLeft.setLabelOffset(new RectangleInsets(0, 2, 1, 0));
    }

    public double getDomainCrosshairMarkerValue() {
        return domainCrosshairMarker.getValue();
    }

    public int getDomainCrosshairMarkerValueForPinning() {
        final XYNoteIconAnnotation note = chartPanel.getPlotNavigationHelper().getNoteShowingIconAnnotation();
        if (note != null) {
            return (int) note.getX();
        }

        final double value = domainCrosshairMarker.getValue();
        if (value >= 0D) {
            return (int) value;
        }
        if (crosshairLastMouseX < 0 || crosshairLastMouseY < 0) {
            return -1;
        }
        //fallback
        final Point mousePoint = new Point(crosshairLastMouseX, crosshairLastMouseY);

        // convert the Java2D coordinate to axis coordinates...
        final ChartRenderingInfo chartInfo = chartPanel.getChartPanel().getChartRenderingInfo();
        final Point2D java2DPoint = chartPanel.getChartPanel().translateScreenToJava2D(mousePoint);
        final PlotRenderingInfo plotInfo = chartInfo.getPlotInfo();

        // see if the point is in one of the subplots; this is the
        // intersection of the range and domain crosshairs
        final int subplotIndex = plotInfo.getSubplotIndex(java2DPoint);
        if (subplotIndex < 0) {
            return -1;
        }

        final int xx = calculateDomainCrosshairMarkerValue(java2DPoint, plotInfo);
        return xx;
    }

    private int calculateDomainCrosshairMarkerValue(final Point2D java2DPoint, final PlotRenderingInfo plotInfo) {
        // all subplots have the domain crosshair
        // the x coordinate is the same for all subplots
        final Rectangle2D dataArea = plotInfo.getDataArea();
        final double xxDouble = chartPanel.getCombinedPlot()
                .getDomainAxis()
                .java2DToValue(java2DPoint.getX(), dataArea, chartPanel.getCombinedPlot().getDomainAxisEdge());
        final int xx = (int) Doubles.round(xxDouble, 0, RoundingMode.HALF_UP);
        return xx;
    }

    @EventDispatchThread(InvocationType.INVOKE_LATER_IF_NOT_IN_EDT)
    public void updateCrosshair(final int mouseX, final int mouseY) {
        final Point mousePoint = new Point(mouseX, mouseY);

        // convert the Java2D coordinate to axis coordinates...
        final ChartRenderingInfo chartInfo = chartPanel.getChartPanel().getChartRenderingInfo();
        final Point2D java2DPoint = chartPanel.getChartPanel().translateScreenToJava2D(mousePoint);
        final PlotRenderingInfo plotInfo = chartInfo.getPlotInfo();

        // see if the point is in one of the subplots; this is the
        // intersection of the range and domain crosshairs
        final int subplotIndex = plotInfo.getSubplotIndex(java2DPoint);
        boolean domainMarkerChanged = false;

        if (subplotIndex >= 0) {
            final int xx = calculateDomainCrosshairMarkerValue(java2DPoint, plotInfo);

            final Rectangle2D panelArea = chartPanel.getChartPanel().getScreenDataArea(mouseX, mouseY);

            domainMarkerChanged = lastDomainCrosshairMarker.getValue() != xx;
            domainCrosshairMarker.setValue(xx);
            lastDomainCrosshairMarker.setValue(xx);
            lastDomainCrosshairMarker.setLabel(chartPanel.getDomainAxis().getNumberFormatOverride().format(xx));
            final List<XYPlot> plots = chartPanel.getCombinedPlot().getSubplots();
            for (int i = 0; i < plots.size(); i++) {
                final XYPlot plot = plots.get(i);
                // set domain crosshair for each plot

                if (!plot.isDomainCrosshairLockedOnData()) {
                    if (i == plots.size() - 1) {
                        plot.addDomainMarker(lastDomainCrosshairMarker);
                    } else {
                        plot.addDomainMarker(domainCrosshairMarker);
                    }
                    plot.setDomainCrosshairLockedOnData(true); //our marker for enabled crosshair
                }
                if (subplotIndex == i) {
                    final NumberAxis rangeAxisRight = (NumberAxis) plot.getRangeAxis();
                    final double yyRight = rangeAxisRight.java2DToValue(mousePoint.getY(), panelArea,
                            plot.getRangeAxisEdge());
                    rangeCrosshairMarkerRight.setValue(yyRight);
                    if (rangeAxisRight.isVisible()) {
                        rangeCrosshairMarkerRight.setLabel(rangeAxisRight.getNumberFormatOverride().format(yyRight));
                        final NumberAxis rangeAxisLeft = (NumberAxis) plot.getRangeAxis(1);
                        if (rangeAxisLeft != null && rangeAxisLeft.isVisible()) {
                            rangeCrosshairMarkerLeft.setValue(yyRight);
                            final double yyLeft = rangeAxisLeft.java2DToValue(mousePoint.getY(), panelArea,
                                    plot.getRangeAxisEdge(1));
                            rangeCrosshairMarkerLeft.setLabel(rangeAxisLeft.getNumberFormatOverride().format(yyLeft));
                        } else {
                            rangeCrosshairMarkerLeft.setValue(-1);
                            rangeCrosshairMarkerLeft.setLabel(null);
                        }
                    } else {
                        rangeCrosshairMarkerRight.setLabel(null);
                        rangeCrosshairMarkerRight.setValue(-1D);
                        rangeCrosshairMarkerLeft.setLabel(null);
                        rangeCrosshairMarkerLeft.setValue(-1D);
                    }
                    if (!plot.isRangeCrosshairLockedOnData()) {
                        plot.addRangeMarker(rangeCrosshairMarkerRight);
                        if (rangeCrosshairMarkerLeft.getLabel() != null) {
                            plot.addRangeMarker(rangeCrosshairMarkerLeft);
                        }
                        plot.setRangeCrosshairLockedOnData(true); //our marker for enabled crosshair
                    }
                    chartPanel.setCursor(CROSSHAIR_CURSOR);
                } else {
                    // this subplot does not have the range
                    // crosshair, make sure its off
                    disableRangeCrosshair(plot);
                }
                crosshairLastMouseX = mouseX;
                crosshairLastMouseY = mouseY;
            }
        } else {
            disableCrosshair(true);
        }

        if (domainMarkerChanged) {
            chartPanel.getPlotCoordinateHelper().coordinatesChanged((int) domainCrosshairMarker.getValue());
        }
    }

    @EventDispatchThread(InvocationType.INVOKE_LATER_IF_NOT_IN_EDT)
    public void disableCrosshair(final boolean notify) {
        final List<XYPlot> subplotsList = chartPanel.getCombinedPlot().getSubplots();
        for (int i = 0; i < subplotsList.size(); i++) {
            final XYPlot subplot = subplotsList.get(i);
            disableCrosshair(subplot);
        }

        //We set this via reflection since the actual setValue method also fire's a lot of listener events which causes the plot to stutter/flicker for quite a while in certain cases.
        //This way it only flickers shortly !

        Markers.setValue(rangeCrosshairMarkerRight, -1D);
        Markers.setValue(rangeCrosshairMarkerLeft, -1D);
        Markers.setValue(domainCrosshairMarker, -1D);
        Markers.setValue(lastDomainCrosshairMarker, -1D);

        if (notify) {
            chartPanel.getCombinedPlot().notifyListeners(new PlotChangeEvent(chartPanel.getCombinedPlot()));
        }

    }

    public Point2D getCrosshairLastMousePoint() {
        if (domainCrosshairMarker.getValue() < 0D || crosshairLastMouseX < 0 || crosshairLastMouseY < 0) {
            return null;
        } else {
            return new Point2D.Double(crosshairLastMouseX, crosshairLastMouseY);
        }
    }

    private void disableCrosshair(final XYPlot subplot) {
        disableRangeCrosshair(subplot);
        XYPlots.setDomainCrosshairLockedOnData(subplot, false);
        XYPlots.removeDomainMarker(subplot, domainCrosshairMarker);
        XYPlots.removeDomainMarker(subplot, lastDomainCrosshairMarker);
    }

    private void disableRangeCrosshair(final XYPlot subplot) {
        XYPlots.setRangeCrosshairLockedOnData(subplot, false);
        XYPlots.removeRangeMarker(subplot, rangeCrosshairMarkerRight);
        XYPlots.removeRangeMarker(subplot, rangeCrosshairMarkerLeft);
    }

    public void datasetChanged() {
        if (domainCrosshairMarker.getValue() >= 0D && crosshairLastMouseX >= 0 && crosshairLastMouseY >= 0) {
            updateCrosshair(crosshairLastMouseX, crosshairLastMouseY);
        }
    }

    public void mouseMoved(final MouseEvent e) {
        final int mouseX = e.getX();
        final int mouseY = e.getY();
        updateCrosshair(mouseX, mouseY);
    }
}
