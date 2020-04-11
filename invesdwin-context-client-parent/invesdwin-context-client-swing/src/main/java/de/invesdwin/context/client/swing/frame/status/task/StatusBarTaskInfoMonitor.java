package de.invesdwin.context.client.swing.frame.status.task;

import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.jdesktop.application.TaskService;

import com.github.benmanes.caffeine.cache.Caffeine;

import de.invesdwin.context.client.swing.api.hook.IRichApplicationHook;
import de.invesdwin.context.client.swing.api.task.ATask;
import de.invesdwin.context.client.swing.frame.app.DelegateRichApplication;
import de.invesdwin.util.concurrent.Threads;
import de.invesdwin.util.concurrent.taskinfo.ITaskInfoListener;
import de.invesdwin.util.concurrent.taskinfo.TaskInfo;
import de.invesdwin.util.concurrent.taskinfo.TaskInfoManager;
import de.invesdwin.util.lang.Strings;
import de.invesdwin.util.math.decimal.scaled.Percent;
import de.invesdwin.util.math.decimal.scaled.PercentScale;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.util.time.fdate.FTimeUnit;

@ThreadSafe
public class StatusBarTaskInfoMonitor implements IRichApplicationHook, ITaskInfoListener {

    private static final Duration CHECK_INTERVAL = new Duration(1, FTimeUnit.SECONDS);

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
    public void startupDone() {
    }

    @Override
    public void shutdownDone() {
        TaskInfoManager.unregisterListener(this);
    }

    @Override
    public void onTaskInfoAdded(final String name) {
        final ATask<Object, Object> task = new ATask<Object, Object>(DelegateRichApplication.getInstance()) {

            {
                setTitle(name);
            }

            @Override
            protected Object doInBackground() throws Exception {
                while (!Threads.isInterrupted()) {
                    final TaskInfo taskInfo = TaskInfoManager.getTaskInfo(name);
                    if (taskInfo == null) {
                        break;
                    }
                    updateProgress(taskInfo);
                    updateMessage(taskInfo);
                    updateDescription(taskInfo);
                    CHECK_INTERVAL.sleep();
                }
                return null;
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

        };
        name_task.put(name, task);
        taskService.execute(task);
    }

    @Override
    public void onTaskInfoRemoved(final String name) {
        final ATask<?, ?> task = name_task.remove(name);
        if (task != null) {
            task.cancel(true);
        }
    }

}
