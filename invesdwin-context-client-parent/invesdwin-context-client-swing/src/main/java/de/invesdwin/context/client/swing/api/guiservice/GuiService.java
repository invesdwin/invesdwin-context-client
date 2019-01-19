package de.invesdwin.context.client.swing.api.guiservice;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.TaskService;

import de.invesdwin.context.beans.init.MergedContext;
import de.invesdwin.context.client.swing.api.AView;

@Named
@ThreadSafe
public class GuiService implements IGuiService {

    @Inject
    private StatusBar statusBar;
    @Inject
    private ContentPane contentPane;
    @Inject
    private SplashScreen splashScreen;
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
    public SplashScreen getSplashScreen() {
        return splashScreen;
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

}
