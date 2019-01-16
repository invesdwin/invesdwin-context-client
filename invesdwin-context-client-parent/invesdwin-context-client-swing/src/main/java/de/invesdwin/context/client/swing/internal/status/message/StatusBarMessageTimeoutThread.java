package de.invesdwin.context.client.swing.internal.status.message;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Configurable;

import de.invesdwin.context.client.swing.api.guiservice.StatusBar;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.util.time.fdate.FTimeUnit;

@ThreadSafe
@Configurable
public final class StatusBarMessageTimeoutThread extends Thread {

    public static final Duration DEFAULT_TIMEOUT = new Duration(10, FTimeUnit.SECONDS);
    public static final Duration MIN_TIMEOUT_AFTER_PAUSE = new Duration(2, FTimeUnit.SECONDS);

    @GuardedBy("StatusBarMessageTimeoutThread.class")
    private static StatusBarMessageTimeoutThread instance;

    @GuardedBy("this")
    private Duration timeout = DEFAULT_TIMEOUT;
    @GuardedBy("this")
    private Instant timeoutStarted = Instant.DUMMY;
    @GuardedBy("this")
    private boolean paused;

    @Inject
    private StatusBar statusBar;

    private StatusBarMessageTimeoutThread() {}

    public static synchronized void startInstance(final Duration timeout) {
        if (instance == null) {
            instance = new StatusBarMessageTimeoutThread();
            instance.setTimeout(timeout);
            instance.start();
        } else {
            instance.setTimeout(timeout);
        }
    }

    public static synchronized StatusBarMessageTimeoutThread getInstance() {
        return instance;
    }

    public synchronized void setPaused(final boolean paused) {
        if (this.paused != paused) {
            if (!paused) {
                //The message stays for min 2 seconds is not over the Panel anymore
                final Duration unusedTimeout = new Duration(new Instant(), getTimeoutEnd());
                if (unusedTimeout.isGreaterThan(MIN_TIMEOUT_AFTER_PAUSE)) {
                    setTimeout(unusedTimeout);
                } else {
                    setTimeout(MIN_TIMEOUT_AFTER_PAUSE);
                }
            }
            this.paused = paused;
        }
    }

    public synchronized boolean getPaused() {
        return paused;
    }

    public static synchronized void stopInstance() {
        if (instance != null) {
            instance = null;
        }
    }

    private synchronized void setTimeout(final Duration timeout) {
        if (timeout == null) {
            this.timeout = DEFAULT_TIMEOUT;
        } else {
            this.timeout = timeout;
        }
        timeoutStarted = new Instant();
    }

    private synchronized Duration getTimeout() {
        return timeout;
    }

    private synchronized Instant getTimeoutStarted() {
        return timeoutStarted;
    }

    private boolean isTimeoutExpired() {
        return new Duration(getTimeoutStarted()).isGreaterThan(getTimeout());
    }

    private Instant getTimeoutEnd() {
        return new Instant(
                getTimeoutStarted().longValue(FTimeUnit.NANOSECONDS) + timeout.longValue(FTimeUnit.NANOSECONDS),
                FTimeUnit.NANOSECONDS);
    }

    @Override
    public void run() {
        try {
            while ((!isTimeoutExpired() || getPaused()) && getInstance() == this) {
                if (getPaused()) {
                    MIN_TIMEOUT_AFTER_PAUSE.sleep();
                } else {
                    getTimeoutStarted().sleepRelative(getTimeout());
                }
            }

            if (getInstance() == this) {
                statusBar.reset();
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            stopInstance();
        }
    }

}
