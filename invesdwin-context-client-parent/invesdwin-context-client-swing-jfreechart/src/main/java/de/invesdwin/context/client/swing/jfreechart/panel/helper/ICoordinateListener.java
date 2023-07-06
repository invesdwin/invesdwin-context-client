package de.invesdwin.context.client.swing.jfreechart.panel.helper;

import java.awt.event.MouseWheelEvent;

import de.invesdwin.util.time.date.FDate;

public interface ICoordinateListener {
    void coordinatesChanged(FDate previousBarEndTime, FDate currentBarEndTime);

    void disableSelectedDetails();

    void mouseWheelMoved(MouseWheelEvent e);

    boolean pinCoordinates();
}
