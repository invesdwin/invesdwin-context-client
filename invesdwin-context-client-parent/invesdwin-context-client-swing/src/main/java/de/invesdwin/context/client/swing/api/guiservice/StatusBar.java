package de.invesdwin.context.client.swing.api.guiservice;

import java.awt.Color;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import de.invesdwin.util.time.duration.Duration;

@ThreadSafe
public class StatusBar {

    @Inject
    @GuardedBy("this")
    private de.invesdwin.context.client.swing.internal.status.message.StatusBarMessageView view;

    public synchronized void message(final String message) {
        message(message, null);
    }

    public synchronized void message(final String message, final Duration timeout) {
        view.setMessageText(message, null, timeout);
    }

    public synchronized void error(final String message) {
        error(message, null);
    }

    public synchronized void error(final String message, final Duration timeout) {
        view.setMessageText(message, Color.RED, timeout);
    }

    public synchronized void reset() {
        view.setMessageText(null, null, null);
    }

}
