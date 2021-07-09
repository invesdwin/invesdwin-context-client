package de.invesdwin.context.client.swing.api.task;

import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;

import org.jdesktop.application.Task;
import org.jdesktop.application.TaskListener;

import de.invesdwin.util.math.decimal.scaled.Percent;
import de.invesdwin.util.math.decimal.scaled.PercentScale;
import de.invesdwin.util.time.date.FTimeUnit;
import de.invesdwin.util.time.duration.AEstimatedRemainingDuration;
import de.invesdwin.util.time.duration.Duration;

@ThreadSafe
@SuppressWarnings("rawtypes")
public final class EstimatedRemainingDurationTaskListener extends TaskListenerSupport {

    private final Task task;
    private final AEstimatedRemainingDuration estimatedRemainingDuration = new AEstimatedRemainingDuration() {
        @Override
        protected Duration getElapsedDuration() {
            return new Duration(task.getExecutionDuration(TimeUnit.MILLISECONDS), FTimeUnit.MILLISECONDS);
        }

        @Override
        protected Percent getProgressPercent() {
            return new Percent(task.getProgress(), PercentScale.PERCENT);
        }
    };

    private EstimatedRemainingDurationTaskListener(final Task task) {
        this.task = task;
    }

    public Duration getEstimatedRemainingDuration() {
        final Duration eta = estimatedRemainingDuration.getEstimatedRemainingDuration();
        if (eta.isPositiveNonZero()) {
            return eta;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static EstimatedRemainingDurationTaskListener get(final Task task) {
        for (final TaskListener l : task.getTaskListeners()) {
            if (l instanceof EstimatedRemainingDurationTaskListener) {
                return (EstimatedRemainingDurationTaskListener) l;
            }
        }
        final EstimatedRemainingDurationTaskListener listener = new EstimatedRemainingDurationTaskListener(task);
        task.addTaskListener(listener);
        return listener;
    }

}
