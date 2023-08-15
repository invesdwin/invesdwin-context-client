package de.invesdwin.context.client.swing.jfreechart.panel.helper.crosshair;

import java.awt.event.MouseWheelEvent;

import de.invesdwin.util.time.date.FDate;

public interface IPlotCoordinateListener {

    IPlotCoordinateListener[] EMPTY_LIST = new IPlotCoordinateListener[0];

    void coordinatesChanged(FDate from, FDate to);

    void disableSelectedDetails();

    void mouseWheelMoved(MouseWheelEvent e);

    boolean pinCoordinates();

    void maybeUpdateIncompleteBar(FDate currentBarStartTime, FDate currentBarEndTime);
}
