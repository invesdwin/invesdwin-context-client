package de.invesdwin.context.client.swing.frame.status.task;

import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;

import org.jdesktop.application.Application;
import org.jdesktop.application.TaskService;

import com.github.benmanes.caffeine.cache.Caffeine;

import de.invesdwin.context.client.swing.api.hook.IRichApplicationHook;
import de.invesdwin.context.client.swing.api.task.ATask;
import de.invesdwin.context.client.swing.frame.app.DelegateRichApplication;
import de.invesdwin.util.concurrent.Threads;
import de.invesdwin.util.concurrent.taskinfo.ITaskInfoListener;
import de.invesdwin.util.concurrent.taskinfo.TaskInfo;
import de.invesdwin.util.concurrent.taskinfo.TaskInfoManager;
import de.invesdwin.util.lang.string.Strings;
import de.invesdwin.util.math.decimal.scaled.Percent;
import de.invesdwin.util.math.decimal.scaled.PercentScale;
import de.invesdwin.util.time.date.FTimeUnit;
import de.invesdwin.util.time.duration.Duration;
import jakarta.inject.Inject;

@ThreadSafe
public class StatusBarTaskInfoMonitor implements IRichApplicationHook, ITaskInfoListener {

    private static final Duration CHECK_INTERVAL = new Duration(100, FTimeUnit.MILLISECONDS);

    private final Map<String, ATask<?, ?>> name_task = Caffeine.newBuilder()
            .weakValues()
            .<String, ATask<?, ?>> build()
            .asMap();

    @Inject
    private TaskService taskService;

    @Override
    public void initializeDone() {
        TaskInfoManager.registerListener(this);
    }

    @Override
    public void startupDone() {}

    @Override
    public void showMainFrameDone() {}

    @Override
    public void hideMainFrameDone() {}

    @Override
    public void shutdownDone() {
        TaskInfoManager.unregisterListener(this);
    }

    @Override
    public void onTaskInfoAdded(final String name) {
        if (!name_task.containsKey(name)) {
            final ATask<Object, Object> task = new TaskInfoTask(DelegateRichApplication.getInstance(), name);
            final ATask<?, ?> existing = name_task.putIfAbsent(name, task);
            if (existing == null) {
                taskService.execute(task);
            }
        }
    }

    @Override
    public void onTaskInfoRemoved(final String name) {}

    private final class TaskInfoTask extends ATask<Object, Object> {

        private final String name;

        private TaskInfoTask(final Application application, final String name) {
            super(application);
            setTitle(name);
            this.name = name;
        }

        @Override
        protected Object doInBackground() throws Exception {
            boolean removed = false;
            try {
                while (!Threads.isInterrupted()) {
                    final TaskInfo taskInfo;
                    synchronized (TaskInfoManager.class) {
                        taskInfo = TaskInfoManager.getTaskInfo(name);
                        if (taskInfo == null) {
                            name_task.remove(name);
                            removed = true;
                            break;
                        }
                    }
                    updateProgress(taskInfo);
                    updateMessage(taskInfo);
                    updateDescription(taskInfo);
                    try {
                        CHECK_INTERVAL.sleep();
                    } catch (final InterruptedException e) {
                        break;
                    }
                }
                return null;
            } finally {
                if (!removed) {
                    name_task.remove(name);
                }
            }
        }

        private void updateProgress(final TaskInfo taskInfo) {
            final Percent progress = taskInfo.getProgress();
            if (progress != null) {
                final int percentage = (int) progress.getValue(PercentScale.PERCENT);
                if (percentage < 0 || percentage > 100) {
                    setProgress(0);
                } else {
                    setProgress(percentage);
                }
            } else {
                setProgress(0);
            }
        }

        private void updateMessage(final TaskInfo taskInfo) {
            if (taskInfo.getTasksCount() > 1) {
                setMessage(taskInfo.getCompletedCount() + "/" + taskInfo.getTasksCount());
            } else {
                setMessage(null);
            }
        }

        private void updateDescription(final TaskInfo taskInfo) {
            final Set<String> descriptions = taskInfo.getDescriptions();
            if (!descriptions.isEmpty()) {
                setDescription(Strings.escapeHtml4(descriptions.iterator().next()));
            } else {
                setDescription(null);
            }
        }
    }

}
