package de.invesdwin.context.client.swing.api;

import javax.annotation.concurrent.Immutable;

@Immutable
public enum MainFrameCloseOperation {
    Nothing,
    HideFrame,
    MinimizeFrame,
    SystemExit;
}
