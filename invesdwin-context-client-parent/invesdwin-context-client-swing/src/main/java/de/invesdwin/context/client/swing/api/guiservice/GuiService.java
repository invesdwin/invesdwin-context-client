package de.invesdwin.context.client.swing.api.guiservice;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import org.jdesktop.application.Application;
import org.jdesktop.application.TaskService;

import de.invesdwin.context.beans.init.MergedContext;
import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.api.DockableContent;

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

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void showView(final AView<?, ?> view) {
        if (getContentPane().containsView(view)) {
            final DockableContent dockable = view.getDockable();
            dockable.toFront(dockable.getFocusComponent());
        } else {
            final AView existingView = getContentPane().findViewWithEqualModel(view.getModel());
            if (existingView != null) {
                view.replaceView(existingView);
                final DockableContent dockable = view.getDockable();
                dockable.toFront(dockable.getFocusComponent());
            } else {
                getContentPane().addView(view);
            }
        }
    }

}
