package de.invesdwin.context.client.swing.jfreechart.panel.helper.crosshair;

import java.awt.event.MouseWheelEvent;

import de.invesdwin.util.time.date.FDate;

public interface IPlotCoordinateListener {

    IPlotCoordinateListener[] EMPTY_LIST = new IPlotCoordinateListener[0];

    void coordinatesChanged(FDate from, FDate to);

    void mouseExited();

    void mouseWheelMoved(MouseWheelEvent e);

    /**
     * When intentedPinState is true, only allow pin, when false only allow unpin, when null allow to toggle from
     * previous state.
     * 
     * Returns true when something got pinned, false when something got unpinned, null when no change occurred.
     */
    Boolean togglePinCoordinates(Boolean intentedPinState);

    boolean isPinned();

    void maybeUpdateIncompleteBar();
}
