package de.invesdwin.context.client.swing.api.guiservice;

import java.awt.Component;

import org.jdesktop.application.TaskService;

public interface IGuiService {

    StatusBar getStatusBar();

    SplashScreen getSplashScreen();

    ContentPane getContentPane();

    TaskService getTaskService();

    /**
     * Executes gui service tasks and updates all components of this has not been skipped. If it was skipped, only the
     * relevant components will be updated.
     */
    void submitAllViews(Component component);

}
