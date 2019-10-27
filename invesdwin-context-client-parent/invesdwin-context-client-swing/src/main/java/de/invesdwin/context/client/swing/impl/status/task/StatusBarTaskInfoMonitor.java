package de.invesdwin.context.client.swing.impl.status.task;

import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.apache.commons.text.StringEscapeUtils;
import org.jdesktop.application.Application;
import org.jdesktop.application.TaskService;

import com.github.benmanes.caffeine.cache.Caffeine;

import de.invesdwin.context.client.swing.api.ATask;
import de.invesdwin.context.client.swing.api.hook.IRichApplicationHook;
import de.invesdwin.util.concurrent.Threads;
import de.invesdwin.util.concurrent.taskinfo.ITaskInfoListener;
import de.invesdwin.util.concurrent.taskinfo.TaskInfo;
import de.invesdwin.util.concurrent.taskinfo.TaskInfoManager;
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
    public void startupDone() {}

    @Override
    public void shutdownDone() {
        TaskInfoManager.unregisterListener(this);
    }

    @Override
    public void onTaskInfoAdded(final String name) {
        final ATask<Object, Object> task = new ATask<Object, Object>(Application.getInstance()) {

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
                    final Percent progress = taskInfo.getProgress();
                    if (progress != null && !progress.isZero() && progress.isLessThan(Percent.ONE_HUNDRED_PERCENT)) {
                        setProgress((float) progress.getValue(PercentScale.RATE));
                    } else {
                        setProgress(0F);
                    }
                    if (taskInfo.getTasksCount() > 1) {
                        setMessage(taskInfo.getCompletedCount() + "/" + taskInfo.getTasksCount());
                    } else {
                        setMessage(null);
                    }
                    final Set<String> descriptions = taskInfo.getDescriptions();
                    if (!descriptions.isEmpty()) {
                        setDescription(StringEscapeUtils.escapeHtml4(descriptions.iterator().next()));
                    } else {
                        setDescription(null);
                    }
                    CHECK_INTERVAL.sleep();
                }
                return null;
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
