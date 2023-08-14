package de.invesdwin.context.client.swing.jfreechart.panel.helper;

import java.awt.event.MouseWheelEvent;

import de.invesdwin.util.time.date.FDate;

public interface ICoordinateListener {

    ICoordinateListener[] EMPTY_LIST = new ICoordinateListener[0];

    void pointOfInterestChanged(FDate from, FDate to);

    void disableSelectedDetails();

    void mouseWheelMoved(MouseWheelEvent e);

    boolean pinCoordinates();

    void maybeUpdateIncompleteBar(FDate currentBarStartTime, FDate currentBarEndTime);
}
