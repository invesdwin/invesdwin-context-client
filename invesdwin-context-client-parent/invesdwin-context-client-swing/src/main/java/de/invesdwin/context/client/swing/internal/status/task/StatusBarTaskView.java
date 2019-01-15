package de.invesdwin.context.client.swing.internal.status.task;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import org.springframework.beans.factory.InitializingBean;

import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;
import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.internal.status.StatusBarView;
import de.invesdwin.util.lang.Strings;

@SuppressWarnings("serial")
@ThreadSafe
public class StatusBarTaskView extends AView<StatusBarTaskView, JPanel> implements InitializingBean {

    private TaskMonitor taskMonitor;
    private JLabel lblForegroundTask;
    private JLabel lblTasks;
    private JProgressBar pgbForegroundTask;
    private JPanel pnlProgress;

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    protected JPanel initComponent() {
        final JPanel component = new JPanel();
        component.setMinimumSize(new Dimension(0, 0));
        component.setLayout(new BorderLayout(0, 0));

        lblForegroundTask = new JLabel("");
        lblForegroundTask.setVerticalAlignment(SwingConstants.BOTTOM);
        component.add(lblForegroundTask, BorderLayout.WEST);

        pnlProgress = new JPanel();
        pnlProgress.setMinimumSize(new Dimension(0, 0));
        component.add(pnlProgress, BorderLayout.CENTER);
        pnlProgress.setLayout(new BorderLayout(0, 0));

        pgbForegroundTask = new JProgressBar();
        pnlProgress.add(pgbForegroundTask, BorderLayout.SOUTH);
        pgbForegroundTask.setMinimumSize(new Dimension(0, 0));

        lblTasks = new JLabel("");
        lblTasks.setVerticalAlignment(SwingConstants.BOTTOM);
        component.add(lblTasks, BorderLayout.EAST);

        berechneProgressbarPreferredSize();
        setForegroundTaskText(null);
        setTasksText(new ArrayList<Task<?, ?>>());
        return component;
    }

    private void berechneProgressbarPreferredSize() {
        final Dimension pgbPreferredSize = new Dimension();
        pgbPreferredSize.height = lblForegroundTask.getFont().getSize() + 2;
        pgbPreferredSize.width = pgbPreferredSize.height * 3;
        pgbForegroundTask.setPreferredSize(pgbPreferredSize);
    }

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    private void setForegroundTaskText(final Task<?, ?> foregroundTask) {
        if (foregroundTask != null) {
            lblForegroundTask.setText(taskToString(foregroundTask) + " ");
            final StringBuilder tooltip = new StringBuilder("<html>");
            tooltip.append(lblForegroundTask.getText());
            //Description
            if (Strings.isNotBlank(foregroundTask.getDescription())) {
                tooltip.append("<hr>");
                tooltip.append(foregroundTask.getDescription());
            }
            lblForegroundTask.setToolTipText(tooltip.toString());

            if (foregroundTask.isProgressPropertyValid()) {
                pgbForegroundTask.setIndeterminate(false);
                pgbForegroundTask.setValue(foregroundTask.getProgress());
            } else {
                pgbForegroundTask.setIndeterminate(true);
            }
            pnlProgress.setVisible(true);
        } else {
            lblForegroundTask.setText(null);
            lblForegroundTask.setToolTipText(null);
            pnlProgress.setVisible(false);
        }
        pgbForegroundTask.setToolTipText(lblForegroundTask.getToolTipText());
    }

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    private void setTasksText(final List<Task<?, ?>> tasks) {
        if (tasks.size() > 0) {
            final StringBuilder text = new StringBuilder(" [");
            text.append(tasks.size());
            text.append("]");
            text.append(StatusBarView.DISTANCE_TO_BORDER);
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
            lblTasks.setToolTipText(tooltip.toString());
        } else {
            lblTasks.setText(null);
            lblTasks.setToolTipText(null);
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
        return s.toString();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.taskMonitor = new TaskMonitor(Application.getInstance().getContext());

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
                                        setForegroundTaskText(task);
                                        final List<Task<?, ?>> tasks = (List) taskMonitor.getTasks();
                                        setTasksText(tasks);
                                    }
                                }
                            });
                }
                setForegroundTaskText(newForegroundTask);
                final List<Task<?, ?>> tasks = (List) taskMonitor.getTasks();
                setTasksText(tasks);
            }
        });

        taskMonitor.addPropertyChangeListener("tasks", new PropertyChangeListener() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                final List<Task<?, ?>> tasks = (List) taskMonitor.getTasks();
                if (tasks.size() == 0) {
                    setForegroundTaskText(null);
                }
                setTasksText(tasks);
            }
        });
    }

}
