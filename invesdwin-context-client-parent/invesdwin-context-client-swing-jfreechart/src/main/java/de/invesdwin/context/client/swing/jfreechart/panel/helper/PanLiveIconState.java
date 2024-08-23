package de.invesdwin.context.client.swing.jfreechart.panel.helper;

import javax.annotation.concurrent.Immutable;

@Immutable
public enum PanLiveIconState {
    Invisible(false, false, false),
    PanLiveForwardVisible(true, false, false),
    PanLiveForwardHighlighted(true, false, true),
    PanLiveBackwardVisible(false, true, false),
    PanLiveBackwardHighlighted(false, true, true);

    private final boolean forwardVisible;
    private final boolean backwardVisible;
    private final boolean highlighted;

    PanLiveIconState(final boolean forwardVisible, final boolean backwardVisible, final boolean highlighted) {
        this.forwardVisible = forwardVisible;
        this.backwardVisible = backwardVisible;
        this.highlighted = highlighted;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public boolean isForwardVisible() {
        return forwardVisible;
    }

    public boolean isBackwardVisible() {
        return backwardVisible;
    }
}