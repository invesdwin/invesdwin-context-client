package de.invesdwin.context.client.swing.impl.status.task;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.jdesktop.application.Application;
import org.jdesktop.application.TaskService;

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

    @Inject
    private TaskService taskService;

    @Override
    public void initializeDone() {
        TaskInfoManager.getListeners().add(this);
    }

    @Override
    public void startupDone() {}

    @Override
    public void shutdownDone() {
        TaskInfoManager.getListeners().remove(this);
    }

    @Override
    public void onTaskInfoAdded(final String name) {
        taskService.execute(new ATask<Object, Object>(Application.getInstance()) {

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
                        setMessage(taskInfo.getCompletedCount() + "/" + taskInfo.getTasksCount());
                    } else {
                        setMessage(null);
                    }
                    CHECK_INTERVAL.sleep();
                }
                return null;
            }

        });
    }

    @Override
    public void onTaskInfoRemoved(final String name) {}

}
