package de.invesdwin.context.client.swing.jfreechart.panel.helper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;

import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;
import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.XYNoteIconAnnotation;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IndexedDateTimeOHLCDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.marker.TriangleLineValueMarker;
import de.invesdwin.util.lang.color.Colors;
import de.invesdwin.util.math.decimal.scaled.Percent;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.util.time.date.FDates;

@NotThreadSafe
public class PlotDetailsHelper {

    private static final Color PIN_LINE_COLOR = Colors.setTransparency(Color.BLUE, Percent.SEVENTY_PERCENT);
    private static final Color PIN_TRIANGLE_COLOR = Color.BLUE;
    private static final Stroke PIN_STROKE = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0);

    private final InteractiveChartPanel chartPanel;
    private final List<ValueMarker> markers = new ArrayList<>();
    private IJFreeChartPointsOfInterestListener coordinateListener;
    private int domainMarkerValue;

    public PlotDetailsHelper(final InteractiveChartPanel chartPanel) {
        this.chartPanel = chartPanel;
    }

    public void pointOfInterestChanged(final int domainCrosshairMarkerValue) {
        final XYPlot xyPlot = (XYPlot) chartPanel.getCombinedPlot().getDomainAxis().getPlot();
        final IndexedDateTimeOHLCDataset dataset = (IndexedDateTimeOHLCDataset) xyPlot.getDataset();
        final FDate previousBarEndTime = dataset.getData().get(domainCrosshairMarkerValue - 1).getEndTime();
        final int xCoordinate = domainCrosshairMarkerValue;
        final boolean isCurrentBar = xCoordinate == (dataset.getData().size() - 1);
        final FDate currentBarEndTime = isCurrentBar ? FDates.MAX_DATE
                : dataset.getData().get(xCoordinate).getEndTime();
        coordinateListener.pointOfInterestChanged(previousBarEndTime, currentBarEndTime);
    }

    public void mouseWheelMoved(final MouseWheelEvent e) {
        coordinateListener.mouseWheelMoved(e);
    }

    public void mousePressed(final MouseEvent e, final int domainCrosshairMarkerValue) {
        if (MouseEvent.BUTTON1 == e.getButton() && e.isControlDown()) {
            final boolean pinnedSomething = coordinateListener.pinCoordinates();
            removePinMarker();

            if (pinnedSomething) {
                domainMarkerValue = domainCrosshairMarkerValue;
                updatePinMarker();
            }
        }
    }

    public void updatePinMarker() {
        removePinMarker();
        final List<XYPlot> plots = chartPanel.getCombinedPlot().getSubplots();
        //The first SubPlot is always the trashplot. We don't paint on this one.
        if (plots.size() == 2) {
            final ValueMarker pinMarker = new TriangleLineValueMarker(domainMarkerValue, PIN_LINE_COLOR, PIN_STROKE, 15,
                    15, PIN_TRIANGLE_COLOR, true, true);
            final XYPlot plot = plots.get(1);
            plot.addDomainMarker(pinMarker);
            markers.add(pinMarker);
        } else if (plots.size() > 2) {
            for (int i = 1; i < plots.size(); i++) {
                final XYPlot plot = plots.get(i);
                final ValueMarker pinMarker;
                if (i == 1) {
                    pinMarker = new TriangleLineValueMarker(domainMarkerValue, PIN_LINE_COLOR, PIN_STROKE, 15, 15,
                            PIN_TRIANGLE_COLOR, true, false);
                } else if (i == plots.size() - 1) {
                    pinMarker = new TriangleLineValueMarker(domainMarkerValue, PIN_LINE_COLOR, PIN_STROKE, 15, 15,
                            PIN_TRIANGLE_COLOR, false, true);
                } else {
                    pinMarker = new ValueMarker(domainMarkerValue, PIN_LINE_COLOR, PIN_STROKE);
                }
                plot.addDomainMarker(pinMarker);
                markers.add(pinMarker);
            }
        } else {
            throw new IllegalStateException("Plots-Size of: " + plots.size() + " is not allowed.");
        }
    }

    public void showOrderDetails(final XYNoteIconAnnotation noteShowingIconAnnotation) {
        final XYPlot xyPlot = (XYPlot) chartPanel.getCombinedPlot().getDomainAxis().getPlot();
        final IndexedDateTimeOHLCDataset dataset = (IndexedDateTimeOHLCDataset) xyPlot.getDataset();
        final FDate previousBarEndTime = dataset.getData().get((int) noteShowingIconAnnotation.getX() - 1).getEndTime();
        final int xCoordinate = (int) noteShowingIconAnnotation.getX();
        final boolean isCurrentBar = xCoordinate == (dataset.getData().size() - 1);
        final FDate currentBarEndTime = isCurrentBar ? FDates.MAX_DATE
                : dataset.getData().get(xCoordinate).getEndTime();
        coordinateListener.pointOfInterestChanged(previousBarEndTime, currentBarEndTime);
    }

    public void mouseExited() {
        coordinateListener.disableSelectedDetails();
    }

    public void removePinMarker() {
        final List<XYPlot> plots = chartPanel.getCombinedPlot().getSubplots();
        for (int i = 1; i < plots.size(); i++) {
            final XYPlot plot = plots.get(i);
            final Iterator<ValueMarker> it = markers.iterator();
            while (it.hasNext()) {
                final ValueMarker marker = it.next();
                final boolean removedSomething = plot.removeDomainMarker(marker);
                if (removedSomething) {
                    it.remove();
                }
            }
        }

        /*
         * If there were SubPlots removed after a Marker was set, there might still be markers left here --> we clear
         * the whole list just in case.
         */

        markers.clear();
    }

    public void removePinMarker(final XYPlot subplot) {
        final Iterator<ValueMarker> it = markers.iterator();
        while (it.hasNext()) {
            final ValueMarker marker = it.next();
            final boolean removedSomething = subplot.removeDomainMarker(marker);
            if (removedSomething) {
                it.remove();
            }
        }
    }

    @EventDispatchThread(InvocationType.INVOKE_LATER_IF_NOT_IN_EDT)
    public void disableSelectedDetails() {
        coordinateListener.disableSelectedDetails();
    }

    public void registerCoordindateListener(final IJFreeChartPointsOfInterestListener coordinateListener) {
        this.coordinateListener = coordinateListener;
    }

    public void unregisterCoordindateListener() {
        this.coordinateListener = null;
    }

}
