package de.invesdwin.context.client.swing.api.task;

import java.util.List;

import javax.annotation.concurrent.Immutable;

import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;

@Immutable
public class TaskListenerSupport<T, V> implements TaskListener<T, V> {

    @Override
    public void doInBackground(final TaskEvent<Void> event) {}

    @Override
    public void process(final TaskEvent<List<V>> event) {}

    @Override
    public void succeeded(final TaskEvent<T> event) {}

    @Override
    public void failed(final TaskEvent<Throwable> event) {}

    @Override
    public void cancelled(final TaskEvent<Void> event) {}

    @Override
    public void interrupted(final TaskEvent<InterruptedException> event) {}

    @Override
    public void finished(final TaskEvent<Void> event) {}

}
