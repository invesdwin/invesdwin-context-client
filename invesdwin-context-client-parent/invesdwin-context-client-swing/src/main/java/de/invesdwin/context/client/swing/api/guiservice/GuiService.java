package de.invesdwin.context.client.swing.api.guiservice;

import java.awt.Component;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.TaskService;

import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;
import de.invesdwin.context.beans.init.MergedContext;
import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.util.SubmitAllViewsHelper;
import de.invesdwin.context.client.swing.util.UpdateAllViewsHelper;

@ThreadSafe
public class GuiService implements IGuiService {

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
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void showModalView(final AView<?, ?> view) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean isModalViewShowing() {
        return false;
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

    /**
     * Synchronize models to components for all views in the tree.
     */
    @Override
    public void updateAllViews(final AView<?, ?> view) {
        updateAllViews(view.getComponent());
    }

    /**
     * Synchronize models to components for all views in the tree.
     */
    @Override
    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public void updateAllViews(final Component component) {
        UpdateAllViewsHelper.updateAllViews(component);
    }

    /**
     * Synchronize components to models for all views in the tree. Run validations and update components again
     * accordingly.
     */
    @Override
    public void submitAllViews(final AView<?, ?> view) {
        submitAllViews(view.getComponent());
    }

    /**
     * Synchronize components to models for all views in the tree. Run validations and update components again
     * accordingly.
     */
    @Override
    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public void submitAllViews(final Component component) {
        SubmitAllViewsHelper.submitAllViews(component);
    }

}
