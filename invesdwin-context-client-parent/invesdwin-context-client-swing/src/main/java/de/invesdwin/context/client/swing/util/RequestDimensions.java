package de.invesdwin.context.client.swing.util;

import java.awt.Dimension;

import javax.annotation.concurrent.Immutable;

import bibliothek.gui.dock.common.layout.RequestDimension;

@Immutable
public final class RequestDimensions {

    private RequestDimensions() {}

    public static boolean isResizeRequestPending(final RequestDimension request, final Dimension actual) {
        return request != null && request.isWidthSet() && actual.width != request.getWidth()
                || request.isHeightSet() && actual.height != request.getHeight();
    }

    public static Dimension toDimension(final RequestDimension request, final Dimension actual) {
        if (request.isWidthSet() && request.isHeightSet()) {
            return new Dimension(request.getWidth(), request.getHeight());
        } else if (request.isWidthSet()) {
            return new Dimension(request.getWidth(), actual.height);
        } else if (request.isHeightSet()) {
            return new Dimension(actual.width, request.getHeight());
        } else {
            return actual;
        }
    }

}
