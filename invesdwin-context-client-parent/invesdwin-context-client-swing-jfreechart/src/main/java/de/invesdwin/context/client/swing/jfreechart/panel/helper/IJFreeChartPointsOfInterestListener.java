package de.invesdwin.context.client.swing.jfreechart.panel.helper;

import java.awt.event.MouseWheelEvent;

import de.invesdwin.util.time.date.FDate;

public interface IJFreeChartPointsOfInterestListener {

    IJFreeChartPointsOfInterestListener[] EMPTY_LIST = new IJFreeChartPointsOfInterestListener[0];

    void pointOfInterestChanged(FDate from, FDate to);

    void disableSelectedDetails();

    void mouseWheelMoved(MouseWheelEvent e);

    boolean pinCoordinates();

    void maybeUpdateIncompleteBar(FDate currentBarStartTime, FDate currentBarEndTime);
}
