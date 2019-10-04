package de.invesdwin.context.client.swing.api.guiservice;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
import java.util.Stack;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.swing.JDialog;

import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.TaskService;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;
import de.invesdwin.context.beans.init.MergedContext;
import de.invesdwin.context.client.swing.api.guiservice.dialog.DialogDockable;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.context.client.swing.impl.content.DockableIdGenerator;
import de.invesdwin.context.client.swing.util.SubmitAllViewsHelper;
import de.invesdwin.context.client.swing.util.UpdateAllViewsHelper;
import de.invesdwin.util.swing.Dialogs;

@ThreadSafe
public class GuiService implements IGuiService {

    private final Stack<DialogDockable> dialogs = new Stack<DialogDockable>();

    @Inject
    private StatusBar statusBar;
    @Inject
    private ContentPane contentPane;
    @GuardedBy("none for performance")
    private TaskService taskService;
    @GuardedBy("none for performance")
    private ApplicationContext applicationContext;

    public static IGuiService get() {
        return MergedContext.getInstance().getBean(IGuiService.class);
    }

    @Override
    public StatusBar getStatusBar() {
        return statusBar;
    }

    @Override
    public ContentPane getContentPane() {
        return contentPane;
    }

    @Override
    public TaskService getTaskService() {
        if (taskService == null) {
            taskService = Application.getInstance().getContext().getTaskService();
        }
        return taskService;
    }

    @Override
    public void hideModalView() {
        if (isModalViewShowing()) {
            final DialogDockable dialog = dialogs.pop();
            dialog.getView().setDockable(null);
            dialog.dispose();
        }
    }

    @Override
    public void showModalView(final AView<?, ?> view) {
        showModalView(view, null);
    }

    @Override
    public void showModalView(final AView<?, ?> view, final Dimension dimension) {
        final Window window = getWindow();
        final DialogDockable dialog = new DialogDockable(DockableIdGenerator.newId(view), window);
        dialog.setModal(true);
        dialogs.push(dialog);
        final Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(view.getComponent());
        dialog.setTitle(view.getTitle());
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        view.setDockable(dialog);
        dialog.pack();
        if (dimension != null) {
            dialog.setSize(dimension);
        } else {
            dialog.setSize(new Dimension(400, 200));
        }
        dialog.setMinimumSize(new Dimension(100, 100));
        dialog.setLocationRelativeTo(window);
        //this call blocks for modal dialogs, which is expected
        dialog.setVisible(true);
    }

    @Override
    public boolean isModalViewShowing() {
        return !dialogs.isEmpty();
    }

    @Override
    public Window getWindow() {
        if (dialogs.isEmpty()) {
            return Dialogs.getRootFrame();
        } else {
            return dialogs.peek();
        }
    }

    @Override
    public void showView(final AView<?, ?> view) {
        getContentPane().showView(view);
    }

    @Override
    public ResourceMap getResourceMap(final Class<?> clazz) {
        if (applicationContext == null) {
            applicationContext = Application.getInstance().getContext();
        }
        return applicationContext.getResourceMap(clazz);
    }

    @Override
    public void updateAllViews(final AView<?, ?> view) {
        updateAllViews(view.getComponent());
    }

    @Override
    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public void updateAllViews(final Component component) {
        UpdateAllViewsHelper.updateAllViews(component);
    }

    @Override
    public void submitAllViews(final AView<?, ?> view) {
        submitAllViews(view.getComponent());
    }

    @Override
    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public void submitAllViews(final Component component) {
        SubmitAllViewsHelper.submitAllViews(component);
    }

}
