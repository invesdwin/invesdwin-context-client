package de.invesdwin.context.client.swing.jfreechart.panel.helper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;

import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;
import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.XYNoteIconAnnotation;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IndexedDateTimeOHLCDataset;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.util.time.date.FDates;

@NotThreadSafe
public class PlotDetailsHelper {

    private static final Color PIN_COLOR = Color.BLUE;
    private static final Stroke PIN_STROKE = new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0);

    private final InteractiveChartPanel chartPanel;
    private ValueMarker pinMarker;
    private ICoordinateListener coordinateListener;

    public PlotDetailsHelper(final InteractiveChartPanel chartPanel) {
        this.chartPanel = chartPanel;
    }

    public void updateDetails(final int domainCrosshairMarkerValue) {
        if (coordinateListener != null) {
            final XYPlot xyPlot = (XYPlot) chartPanel.getCombinedPlot().getDomainAxis().getPlot();
            final IndexedDateTimeOHLCDataset dataset = (IndexedDateTimeOHLCDataset) xyPlot.getDataset();
            final FDate previousBarEndTime = dataset.getData().get(domainCrosshairMarkerValue - 1).getEndTime();
            final int xCoordinate = domainCrosshairMarkerValue;
            final boolean isCurrentBar = xCoordinate == (dataset.getData().size() - 1);
            final FDate currentBarEndTime = isCurrentBar ? FDates.MAX_DATE
                    : dataset.getData().get(xCoordinate).getEndTime();
            coordinateListener.coordinatesChanged(previousBarEndTime, currentBarEndTime);
        }
    }

    public void mouseWheelMoved(final MouseWheelEvent e) {
        if (coordinateListener != null) {
            coordinateListener.mouseWheelMoved(e);
        }
    }

    public void mousePressed(final MouseEvent e, final int domainCrosshairMarkerValue) {
        if (coordinateListener != null && MouseEvent.BUTTON1 == e.getButton() && e.isControlDown()) {
            final XYPlot xyPlot = (XYPlot) chartPanel.getCombinedPlot().getDomainAxis().getPlot();
            final boolean pinnedSomething = coordinateListener.pinCoordinates();
            xyPlot.removeDomainMarker(pinMarker);

            if (pinnedSomething) {
                pinMarker = new ValueMarker(domainCrosshairMarkerValue, PIN_COLOR, PIN_STROKE);
                xyPlot.addDomainMarker(pinMarker);
            }
        }
    }

    public void showOrderDetails(final XYNoteIconAnnotation noteShowingIconAnnotation) {
        if (coordinateListener != null) {
            final XYPlot xyPlot = (XYPlot) chartPanel.getCombinedPlot().getDomainAxis().getPlot();
            final IndexedDateTimeOHLCDataset dataset = (IndexedDateTimeOHLCDataset) xyPlot.getDataset();
            final FDate previousBarEndTime = dataset.getData()
                    .get((int) noteShowingIconAnnotation.getX() - 1)
                    .getEndTime();
            final int xCoordinate = (int) noteShowingIconAnnotation.getX();
            final boolean isCurrentBar = xCoordinate == (dataset.getData().size() - 1);
            final FDate currentBarEndTime = isCurrentBar ? FDates.MAX_DATE
                    : dataset.getData().get(xCoordinate).getEndTime();

            coordinateListener.coordinatesChanged(previousBarEndTime, currentBarEndTime);
        }
    }

    public void mouseExited() {
        if (coordinateListener != null) {
            coordinateListener.disableSelectedDetails();
        }
    }

    public void removePinMarker() {
        final XYPlot xyPlot = (XYPlot) chartPanel.getCombinedPlot().getDomainAxis().getPlot();
        xyPlot.removeDomainMarker(pinMarker);
    }

    @EventDispatchThread(InvocationType.INVOKE_LATER_IF_NOT_IN_EDT)
    public void disableSelectedDetails() {
        if (coordinateListener != null) {
            coordinateListener.disableSelectedDetails();
        }
    }

    public void registerCoordindateListener(final ICoordinateListener coordinateListener) {
        this.coordinateListener = coordinateListener;
    }

    public void unregisterCoordindateListener() {
        this.coordinateListener = null;
    }

}
