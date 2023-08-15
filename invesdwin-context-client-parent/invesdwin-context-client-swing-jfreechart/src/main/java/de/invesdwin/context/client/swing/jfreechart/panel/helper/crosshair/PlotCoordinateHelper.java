package de.invesdwin.context.client.swing.jfreechart.panel.helper.crosshair;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.plot.Markers;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.XYNoteIconAnnotation;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IndexedDateTimeOHLCDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.marker.TriangleLineValueMarker;
import de.invesdwin.util.collections.factory.ILockCollectionFactory;
import de.invesdwin.util.lang.color.Colors;
import de.invesdwin.util.math.decimal.scaled.Percent;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.util.time.date.FDates;

@NotThreadSafe
public class PlotCoordinateHelper {

    private static final Color PIN_LINE_COLOR = Colors.setTransparency(Color.BLUE, Percent.SEVENTY_PERCENT);
    private static final Color PIN_TRIANGLE_COLOR = Color.BLUE;
    private static final Stroke PIN_STROKE = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0);

    private final InteractiveChartPanel chartPanel;
    private final List<ValueMarker> markers = new ArrayList<>();
    private final ValueMarker pinMarkerTopAndBottomTriangle = new TriangleLineValueMarker(-1, PIN_LINE_COLOR,
            PIN_STROKE, 15, 15, PIN_TRIANGLE_COLOR, true, true);
    private final ValueMarker pinMarkerTopTriangle = new TriangleLineValueMarker(-1, PIN_LINE_COLOR, PIN_STROKE, 15, 15,
            PIN_TRIANGLE_COLOR, true, false);
    private final ValueMarker pinMarkerBottomTriangle = new TriangleLineValueMarker(-1, PIN_LINE_COLOR, PIN_STROKE, 15,
            15, PIN_TRIANGLE_COLOR, false, true);
    private final ValueMarker pinMarkerNoTriangle = new ValueMarker(-1, PIN_LINE_COLOR, PIN_STROKE);

    private IPlotCoordinateListener coordinateListener;
    private volatile FDate domainMarkerFDate;
    private final Set<XYPlot> prevMarkerPlots = ILockCollectionFactory.getInstance(true).newIdentitySet();
    private boolean domainMarkerSetOnce = false;
    private FDate previousStartTime = FDates.MIN_DATE;

    public PlotCoordinateHelper(final InteractiveChartPanel chartPanel) {
        this.chartPanel = chartPanel;
    }

    public void pointOfInterestChanged(final int domainCrosshairMarkerValue) {
        if (coordinateListener == null) {
            return;
        }
        final XYPlot xyPlot = (XYPlot) chartPanel.getCombinedPlot().getDomainAxis().getPlot();
        final IndexedDateTimeOHLCDataset dataset = (IndexedDateTimeOHLCDataset) xyPlot.getDataset();
        final FDate previousBarEndTime = dataset.getData().get(domainCrosshairMarkerValue - 1).getEndTime();
        final int xCoordinate = domainCrosshairMarkerValue;
        final boolean isCurrentBar = xCoordinate == (dataset.getData().size() - 1);
        final FDate currentBarEndTime = isCurrentBar ? FDates.MAX_DATE
                : dataset.getData().get(xCoordinate).getEndTime();
        coordinateListener.coordinatesChanged(previousBarEndTime, currentBarEndTime);
    }

    public void mouseWheelMoved(final MouseWheelEvent e) {
        if (coordinateListener == null) {
            return;
        }
        coordinateListener.mouseWheelMoved(e);
    }

    public void mousePressed(final MouseEvent e, final int domainCrosshairMarkerValue) {
        if (coordinateListener == null) {
            return;
        }
        final Point2D point = this.chartPanel.getChartPanel().translateScreenToJava2D(e.getPoint());
        final PlotRenderingInfo plotInfo = this.chartPanel.getChartPanel().getChartRenderingInfo().getPlotInfo();
        if (MouseEvent.BUTTON1 == e.getButton() && e.isControlDown() && plotInfo.getDataArea().contains(point)) {
            final boolean pinnedSomething = coordinateListener.pinCoordinates();
            if (pinnedSomething) {
                final XYPlot xyPlot = (XYPlot) chartPanel.getCombinedPlot().getDomainAxis().getPlot();
                final IndexedDateTimeOHLCDataset dataset = (IndexedDateTimeOHLCDataset) xyPlot.getDataset();
                domainMarkerFDate = dataset.getData().get(domainCrosshairMarkerValue).getStartTime();
                domainMarkerSetOnce = true;
                updatePinMarker();
            }
        }
    }

    public void updatePinMarker() {
        final FDate domainMarkerFDateCopy = domainMarkerFDate;
        if (!domainMarkerSetOnce) {
            //avoid checking the other conditions on init. only after a marker was set the first time.
            return;
        }

        final XYPlot xyPlot = (XYPlot) chartPanel.getCombinedPlot().getDomainAxis().getPlot();
        final IndexedDateTimeOHLCDataset dataset = (IndexedDateTimeOHLCDataset) xyPlot.getDataset();
        final Integer domainMarkerValueCopy = dataset.getDateTimeStartAsItemIndex(0, domainMarkerFDateCopy);

        final List<XYPlot> plots = chartPanel.getCombinedPlot().getSubplots();
        if (domainMarkerFDateCopy == null && pinMarkerTopAndBottomTriangle.getValue() >= 0) {
            //remove domain marker
            removePinMarker();
            updateDomainMarkerValuesViaReflection(-1);
        } else if (domainMarkerFDateCopy != null && pinMarkerTopAndBottomTriangle.getValue() == -1) {
            //add domain marker
            updateDomainMarkerValues(domainMarkerValueCopy);
            addDomainMarkers(plots);
        } else if (isPlotsChanged(plots)) {
            //remove and add domain marker
            removePinMarker();
            addDomainMarkers(plots);
        } else if (domainMarkerValueCopy != pinMarkerTopAndBottomTriangle.getValue()) {
            //update domain marker value
            updateDomainMarkerValues(domainMarkerValueCopy);
        }

        //update prevState so we can lazy-check in next updatePinMarkerCall
        prevMarkerPlots.clear();
        for (int i = 0; i < plots.size(); i++) {
            prevMarkerPlots.add(plots.get(i));
        }
    }

    private void updateDomainMarkerValues(final int domainMarkerValueCopy) {
        pinMarkerTopAndBottomTriangle.setValue(domainMarkerValueCopy);
        pinMarkerTopTriangle.setValue(domainMarkerValueCopy);
        pinMarkerBottomTriangle.setValue(domainMarkerValueCopy);
        pinMarkerNoTriangle.setValue(domainMarkerValueCopy);
    }

    private void updateDomainMarkerValuesViaReflection(final int domainMarkerValueCopy) {
        Markers.setValue(pinMarkerTopAndBottomTriangle, domainMarkerValueCopy);
        Markers.setValue(pinMarkerTopTriangle, domainMarkerValueCopy);
        Markers.setValue(pinMarkerBottomTriangle, domainMarkerValueCopy);
        Markers.setValue(pinMarkerNoTriangle, domainMarkerValueCopy);
    }

    private void addDomainMarkers(final List<XYPlot> plots) {
        //The first SubPlot is always the trashplot. We don't paint on this one.
        if (plots.size() == 2) {
            final XYPlot plot = plots.get(1);
            plot.addDomainMarker(pinMarkerTopAndBottomTriangle);
            markers.add(pinMarkerTopAndBottomTriangle);
        } else if (plots.size() > 2) {
            for (int i = 1; i < plots.size(); i++) {
                final XYPlot plot = plots.get(i);
                if (i == 1) {
                    plot.addDomainMarker(pinMarkerTopTriangle);
                    markers.add(pinMarkerTopTriangle);
                } else if (i == plots.size() - 1) {
                    plot.addDomainMarker(pinMarkerBottomTriangle);
                    markers.add(pinMarkerBottomTriangle);
                } else {
                    plot.addDomainMarker(pinMarkerNoTriangle);
                    markers.add(pinMarkerNoTriangle);
                }
            }
        } else {
            throw new IllegalStateException("Plots-Size of: " + plots.size() + " is not allowed.");
        }
    }

    private boolean isPlotsChanged(final List<XYPlot> plots) {
        if (plots.size() == 1) {
            return false;
        }
        if (plots.size() != prevMarkerPlots.size()) {
            return true;
        }
        for (int i = 1; i < plots.size(); i++) {
            if (!prevMarkerPlots.contains(plots.get(i))) {
                return true;
            }
        }
        return false;
    }

    public void showOrderDetails(final XYNoteIconAnnotation noteShowingIconAnnotation) {
        if (coordinateListener == null) {
            return;
        }
        final XYPlot xyPlot = (XYPlot) chartPanel.getCombinedPlot().getDomainAxis().getPlot();
        final IndexedDateTimeOHLCDataset dataset = (IndexedDateTimeOHLCDataset) xyPlot.getDataset();
        final FDate previousBarEndTime = dataset.getData().get((int) noteShowingIconAnnotation.getX() - 1).getEndTime();
        final int xCoordinate = (int) noteShowingIconAnnotation.getX();
        final boolean isCurrentBar = xCoordinate == (dataset.getData().size() - 1);
        final FDate currentBarEndTime = isCurrentBar ? FDates.MAX_DATE
                : dataset.getData().get(xCoordinate).getEndTime();
        coordinateListener.coordinatesChanged(previousBarEndTime, currentBarEndTime);
    }

    public void mouseExited() {
        if (coordinateListener == null) {
            return;
        }
        coordinateListener.disableSelectedDetails();
    }

    public void datasetChanged() {
        if (coordinateListener == null) {
            return;
        }
        final XYPlot xyPlot = (XYPlot) chartPanel.getCombinedPlot().getDomainAxis().getPlot();
        final IndexedDateTimeOHLCDataset dataset = (IndexedDateTimeOHLCDataset) xyPlot.getDataset();
        final FDate currentBarStartTime = dataset.getData().get(dataset.getData().size() - 1).getStartTime();
        if (previousStartTime.isBefore(currentBarStartTime)) {
            final FDate previousBarEndTime = dataset.getData().get(dataset.getData().size() - 2).getEndTime();
            coordinateListener.maybeUpdateIncompleteBar(previousStartTime, previousBarEndTime);
            previousStartTime = currentBarStartTime;
        }
    }

    public void triggerRemovePinMarker() {
        domainMarkerFDate = null;
        //ChartPanel.update() so the update is in the UI-Thread
        chartPanel.update();
    }

    private void removePinMarker() {
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

    public void disableSelectedDetails() {
        if (coordinateListener == null) {
            return;
        }
        coordinateListener.disableSelectedDetails();
    }

    public void registerCoordindateListener(final IPlotCoordinateListener coordinateListener) {
        this.coordinateListener = coordinateListener;
    }

    public void unregisterCoordindateListener() {
        this.coordinateListener = null;
    }

    public IPlotCoordinateListener getCoordinateListener() {
        return coordinateListener;
    }
}
