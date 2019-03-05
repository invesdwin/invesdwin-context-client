package de.invesdwin.context.client.swing.api.guiservice;

import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.TaskService;

import de.invesdwin.context.client.swing.api.AView;

public interface IGuiService {

    StatusBar getStatusBar();

    ContentPane getContentPane();

    TaskService getTaskService();

    void hideModalView();

    void showModalView(AView<?, ?> view);

    /**
     * If a view is already visible with an equal model, then that is being replaced with the new view. Otherwise this
     * view is just displayed in a new dockable.
     */
    void showView(AView<?, ?> view);

    boolean isModalViewShowing();

    ResourceMap getResourceMap(Class<?> clazz);

}
