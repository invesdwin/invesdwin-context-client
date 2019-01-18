package de.invesdwin.context.client.swing.api.guiservice;

import org.jdesktop.application.TaskService;

public interface IGuiService {

    StatusBar getStatusBar();

    SplashScreen getSplashScreen();

    ContentPane getContentPane();

    TaskService getTaskService();

}
