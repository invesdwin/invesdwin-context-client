package de.invesdwin.context.client.swing.test.edt;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.util.TimerTask;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.time.date.FTimeUnit;
import de.invesdwin.util.time.duration.Duration;

/**
 * Alternative events dispatching queue. The benefit over the default Event Dispatch queue is that you can add as many
 * watchdog timers as you need and they will trigger arbitrary actions when processing of single event will take longer
 * than one timer period.
 * <p/>
 * Timers can be of two types:
 * <ul>
 * <li><b>Repetitive</b> - action can be triggered multiple times for the same "lengthy" event dispatching.</li>
 * <li><b>Non-repetitive</b> - action can be triggered only once per event dispatching.</li>
 * </ul>
 * <p/>
 * The queue records time of the event dispatching start. This time is used by the timers to check if dispatching takes
 * longer than their periods. If so the timers trigger associated actions.
 * <p/>
 * In order to use this queue application should call <code>install()</code> method. This method will create, initialize
 * and register the alternative queue as appropriate. It also will return the instance of the queue for further
 * interactions. Here's an example of how it can be done:
 * <p/>
 * 
 * <pre>
 * <p/>
 *  EventQueueWithWD queue = EventQueueWithWD.install();
 *  Action edtOverloadReport = ...;
 * <p/>
 *  // install single-shot wg to report EDT overload after
 *  // 10-seconds timeout
 *  queue.addWatchdog(10000, edtOverloadReport, false);
 * <p/>
 * </pre>
 */
//https://www.javaspecialists.eu/archive/Issue104.html
@ThreadSafe
public final class TimeoutEventQueue extends EventQueue {
    @GuardedBy("this.class")
    private static EventQueue previousEventQueue;
    // Main timer
    private final java.util.Timer timer = new java.util.Timer(true);

    // Group of informational fields for describing the event
    private final Object eventChangeLock = new Object();
    private long eventDispatchingStart;
    private AWTEvent event;
    private Thread eventDispatchThread;

    /**
     * Hidden utility constructor.
     */
    private TimeoutEventQueue() {}

    /**
     * Install alternative queue.
     *
     * @return instance of queue installed.
     */
    public static synchronized TimeoutEventQueue install() {
        final EventQueue cur = Toolkit.getDefaultToolkit().getSystemEventQueue();
        if (cur instanceof TimeoutEventQueue) {
            return (TimeoutEventQueue) cur;
        } else {
            return installNow();
        }
    }

    private static TimeoutEventQueue installNow() {
        Assertions.checkNull(previousEventQueue);
        final EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        previousEventQueue = eventQueue;
        final TimeoutEventQueue newEventQueue = new TimeoutEventQueue();
        eventQueue.push(newEventQueue);
        return newEventQueue;
    }

    public static synchronized void uninstall() {
        if (previousEventQueue != null) {
            final EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
            eventQueue.push(previousEventQueue);
            previousEventQueue = null;
        }
    }

    /**
     * Record the event and continue with usual dispatching.
     *
     * @param anEvent
     *            event to dispatch.
     */
    @Override
    protected void dispatchEvent(final AWTEvent anEvent) {
        setEventDispatchingStart(anEvent, System.nanoTime(), Thread.currentThread());
        super.dispatchEvent(anEvent);
        setEventDispatchingStart(null, -1, null);
    }

    /**
     * Register event and dispatching start time.
     *
     * @param event
     *            event.
     * @param eventDispatchingStart
     *            dispatching start time.
     */
    private void setEventDispatchingStart(final AWTEvent event, final long eventDispatchingStart,
            final Thread eventDispatchThread) {
        synchronized (eventChangeLock) {
            this.event = event;
            this.eventDispatchingStart = eventDispatchingStart;
            this.eventDispatchThread = eventDispatchThread;
        }
    }

    public void addTimeoutListener(final Duration maxProcessingTime, final Duration checkInterval,
            final ITimeoutEventQueueListener listener, final boolean repetitive) {
        final TimeoutListenerTask checker = new TimeoutListenerTask(maxProcessingTime, listener, repetitive);
        final long checkIntervalMillis = checkInterval.longValue(FTimeUnit.MILLISECONDS);
        timer.schedule(checker, checkIntervalMillis, checkIntervalMillis);
    }

    /**
     * Checks if the processing of the event is longer than the specified <code>maxProcessingTime</code>. If so then
     * listener is notified.
     */
    private final class TimeoutListenerTask extends TimerTask {
        // Settings
        private final long maxProcessingTimeNanos;
        private final ITimeoutEventQueueListener listener;
        private final boolean repetitive;

        // Event reported as "lengthy" for the last time. Used to
        // prevent repetitive behaviour in non-repeatitive timers.
        private AWTEvent lastReportedEvent = null;

        /**
         * Creates timer.
         *
         * @param maxProcessingTime
         *            maximum event processing time before listener is notified.
         * @param listener
         *            listener to notify.
         * @param repetitive
         *            TRUE to allow consequent notifications for the same event
         */
        private TimeoutListenerTask(final Duration maxProcessingTime, final ITimeoutEventQueueListener listener,
                final boolean repetitive) {
            if (listener == null) {
                throw new IllegalArgumentException("Listener cannot be null.");
            }
            if (maxProcessingTime.isNegativeOrZero()) {
                throw new IllegalArgumentException("Max locking period should be greater than zero");
            }
            this.maxProcessingTimeNanos = maxProcessingTime.nanosValue();
            this.listener = listener;
            this.repetitive = repetitive;
        }

        @Override
        public void run() {
            final long time;
            final AWTEvent currentEvent;
            final Thread edt;

            // Get current event requisites
            synchronized (eventChangeLock) {
                time = eventDispatchingStart;
                currentEvent = event;
                edt = eventDispatchThread;
            }

            final long currentTime = System.nanoTime();

            // Check if event is being processed longer than allowed
            if (time != -1 && (currentTime - time > maxProcessingTimeNanos)
                    && (repetitive || currentEvent != lastReportedEvent)) {
                listener.onTimeout(currentEvent, edt);
                lastReportedEvent = currentEvent;
            }
        }
    }
}
