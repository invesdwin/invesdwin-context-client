package de.invesdwin.context.client.swing.frame.status.task;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;

import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;
import de.invesdwin.context.client.swing.api.hook.IRichApplicationHook;
import de.invesdwin.context.client.swing.api.task.EstimatedRemainingDurationTaskListener;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.util.lang.Strings;
import de.invesdwin.util.swing.Components;
import de.invesdwin.util.swing.MouseEnteredListener;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.util.time.fdate.FTimeUnit;

@ThreadSafe
public class StatusBarTaskView extends AView<StatusBarTaskView, JPanel> implements IRichApplicationHook {

    private static final Border VISIBLE_BORDER = BorderFactory.createEmptyBorder(1, 0, 2, 0);
    private static final Border INVISIBLE_BORDER = BorderFactory.createEmptyBorder(0, 0, 0, 0);
    private static final Duration ETA_THRESHOLD = new Duration(10, FTimeUnit.SECONDS);
    private TaskMonitor taskMonitor;
    private JLabel lblForegroundTask;
    private MouseEnteredListener lblForegroundTask_mouseEnteredListener;
    private JLabel lblTasks;
    private MouseEnteredListener lblTasks_mouseEnteredListener;
    private JProgressBar pgbForegroundTask;
    private MouseEnteredListener pgbForegroundTask_mouseEnteredListener;
    private JPanel pnlProgress;

    /**
     * @wbp.parser.entryPoint
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected JPanel initComponent() {
        final JPanel component = new JPanel();
        component.setBorder(VISIBLE_BORDER);
        component.setMinimumSize(new Dimension(0, 0));
        component.setLayout(new BorderLayout(0, 0));

        lblForegroundTask = new JLabel("");
        lblForegroundTask.setVerticalAlignment(SwingConstants.BOTTOM);
        component.add(lblForegroundTask, BorderLayout.WEST);
        lblForegroundTask_mouseEnteredListener = MouseEnteredListener.get(lblForegroundTask);

        pnlProgress = new JPanel();
        pnlProgress.setMinimumSize(new Dimension(0, 0));
        component.add(pnlProgress, BorderLayout.CENTER);
        pnlProgress.setLayout(new BorderLayout(0, 0));

        pgbForegroundTask = new JProgressBar();
        pnlProgress.add(pgbForegroundTask, BorderLayout.SOUTH);
        pgbForegroundTask.setMinimumSize(new Dimension(0, 0));
        pgbForegroundTask_mouseEnteredListener = MouseEnteredListener.get(pgbForegroundTask);

        lblTasks = new JLabel("");
        lblTasks.setVerticalAlignment(SwingConstants.BOTTOM);
        component.add(lblTasks, BorderLayout.EAST);
        lblTasks_mouseEnteredListener = MouseEnteredListener.get(lblTasks);

        calculateProgressbarPreferredSize();
        final List<Task<?, ?>> tasks = (List) taskMonitor.getTasks();
        setForegroundTaskText(taskMonitor.getForegroundTask(), tasks);
        setTasksText(tasks);

        Components.showTooltipWithoutDelay(lblForegroundTask);
        Components.showTooltipWithoutDelay(pgbForegroundTask);
        Components.showTooltipWithoutDelay(lblTasks);

        updateBorder(component);

        return component;
    }

    private void updateBorder(final JPanel component) {
        //somehow we need to set BOTH border and visible, only visible still shows gaps
        if (pnlProgress.isVisible()) {
            component.setBorder(VISIBLE_BORDER);
        } else {
            component.setBorder(INVISIBLE_BORDER);
        }
        component.setVisible(pnlProgress.isVisible());
    }

    private void calculateProgressbarPreferredSize() {
        final Dimension pgbPreferredSize = new Dimension();
        pgbPreferredSize.height = lblForegroundTask.getFont().getSize() + 2;
        pgbPreferredSize.width = pgbPreferredSize.height * 3;
        pgbForegroundTask.setPreferredSize(pgbPreferredSize);
    }

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    private void setForegroundTaskText(final Task<?, ?> foregroundTask, final List<Task<?, ?>> tasks) {
        if (pgbForegroundTask == null) {
            //not initialized yet
            return;
        }
        if (foregroundTask != null) {
            final Task<?, ?> task = determineForegroundTask(foregroundTask, tasks);
            final String taskToString = taskToString(task);
            lblForegroundTask.setText(taskToString + " ");
            final StringBuilder tooltip = new StringBuilder("<html>");
            tooltip.append(taskToString);
            //Description
            if (Strings.isNotBlank(task.getDescription())) {
                tooltip.append("<hr>");
                tooltip.append(task.getDescription());
            }
            Components.setToolTipText(lblForegroundTask, tooltip.toString(),
                    lblForegroundTask_mouseEnteredListener.isMouseEntered());

            if (task.isProgressPropertyValid()) {
                pgbForegroundTask.setIndeterminate(false);
                pgbForegroundTask.setValue(task.getProgress());
            } else {
                pgbForegroundTask.setIndeterminate(true);
            }
            pnlProgress.setVisible(true);
        } else {
            lblForegroundTask.setText(null);
            Components.setToolTipText(lblForegroundTask, null, lblForegroundTask_mouseEnteredListener.isMouseEntered());
            pnlProgress.setVisible(false);
        }
        Components.setToolTipText(pgbForegroundTask, lblForegroundTask.getToolTipText(),
                pgbForegroundTask_mouseEnteredListener.isMouseEntered());
    }

    private Task<?, ?> determineForegroundTask(final Task<?, ?> foregroundTask, final List<Task<?, ?>> tasks) {
        Task<?, ?> longestDurationTask = foregroundTask;
        Duration longestDuration = Duration.ZERO;
        for (final Task<?, ?> task : tasks) {
            final Duration duration = EstimatedRemainingDurationTaskListener.get(task).getEstimatedRemainingDuration();
            if (duration != null && longestDuration.isLessThan(duration)) {
                longestDuration = duration;
                longestDurationTask = task;
            }
        }
        return longestDurationTask;
    }

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    private void setTasksText(final List<Task<?, ?>> tasks) {
        if (lblTasks == null) {
            //not initialized yet
            return;
        }
        if (tasks.size() > 1) {
            final StringBuilder text = new StringBuilder(" [");
            text.append(tasks.size());
            text.append("] ");
            lblTasks.setText(text.toString());

            final StringBuilder tooltip = new StringBuilder("<html>");
            for (int i = 0; i < tasks.size(); i++) {
                final Task<?, ?> task = tasks.get(i);
                tooltip.append("[");
                tooltip.append(i + 1);
                tooltip.append("] ");
                tooltip.append(taskToString(task));
                if (i < tasks.size()) {
                    tooltip.append("<br>");
                }
            }
            Components.setToolTipText(lblTasks, tooltip.toString(), lblTasks_mouseEnteredListener.isMouseEntered());
        } else if (tasks.size() == 1) {
            lblTasks.setText(" ");
            Components.setToolTipText(lblTasks, null, lblTasks_mouseEnteredListener.isMouseEntered());
        } else {
            lblTasks.setText(null);
            Components.setToolTipText(lblTasks, null, lblTasks_mouseEnteredListener.isMouseEntered());
        }
    }

    private String taskToString(final Task<?, ?> task) {
        final StringBuilder s = new StringBuilder();
        if (Strings.isNotBlank(task.getTitle())) {
            s.append(task.getTitle());
        } else {
            s.append("Untitled Task");
        }
        if (task.isProgressPropertyValid()) {
            s.append(" (");
            s.append(task.getProgress());
            s.append("%)");
        }
        if (Strings.isNotBlank(task.getMessage())) {
            s.append(": ");
            s.append(task.getMessage());
        }
        final Duration eta = EstimatedRemainingDurationTaskListener.get(task).getEstimatedRemainingDuration();
        if (eta != null && eta.isGreaterThanOrEqualTo(ETA_THRESHOLD)) {
            s.append(" | ETA: ");
            s.append(eta.toString(FTimeUnit.SECONDS));
        }
        return s.toString();
    }

    @Override
    public void initializeDone() {
        final ApplicationContext context = Application.getInstance().getContext();
        context.addPropertyChangeListener("taskServices", new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                updateTaskMonitor(context);
            }
        });
        updateTaskMonitor(context);
    }

    @Override
    public void startupDone() {}

    @Override
    public void shutdownDone() {}

    private void updateTaskMonitor(final ApplicationContext context) {
        this.taskMonitor = new TaskMonitor(context);

        taskMonitor.addPropertyChangeListener(TaskMonitor.PROP_FOREGROUND_TASK, new PropertyChangeListener() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                final Task<?, ?> newForegroundTask = (Task<?, ?>) evt.getNewValue();
                if (newForegroundTask != null) {
                    newForegroundTask.getPropertyChangeSupport()
                            .addPropertyChangeListener(new PropertyChangeListener() {
                                @Override
                                public void propertyChange(final PropertyChangeEvent evt) {
                                    final Task<?, ?> task = (Task<?, ?>) evt.getSource();
                                    if (task != taskMonitor.getForegroundTask()) {
                                        task.removePropertyChangeListener(this);
                                    } else {
                                        final List<Task<?, ?>> tasks = (List) taskMonitor.getTasks();
                                        setForegroundTaskText(task, tasks);
                                        setTasksText(tasks);
                                    }
                                }
                            });
                }
                final List<Task<?, ?>> tasks = (List) taskMonitor.getTasks();
                setForegroundTaskText(newForegroundTask, tasks);
                setTasksText(tasks);
                updateBorder(getComponent());
            }
        });

        taskMonitor.addPropertyChangeListener("tasks", new PropertyChangeListener() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                final List<Task<?, ?>> tasks = (List) taskMonitor.getTasks();
                if (tasks.size() == 0) {
                    setForegroundTaskText(null, tasks);
                }
                setTasksText(tasks);
                updateBorder(getComponent());
            }
        });
    }

}
