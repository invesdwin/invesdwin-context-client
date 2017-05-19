package de.invesdwin.common.client.swing;

import java.awt.Color;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.jdesktop.application.Application;
import org.jdesktop.application.TaskService;

import de.invesdwin.common.client.swing.internal.status.message.StatusBarMessageView;
import de.invesdwin.util.time.duration.Duration;

@ThreadSafe
public class StatusBar {

    @Inject
    @GuardedBy("this")
    private StatusBarMessageView view;

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

    public TaskService getTaskService() {
        return Application.getInstance().getContext().getTaskService();
    }

}
