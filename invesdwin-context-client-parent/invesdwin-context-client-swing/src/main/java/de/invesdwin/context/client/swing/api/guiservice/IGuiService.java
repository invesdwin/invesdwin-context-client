package de.invesdwin.context.client.swing.api.guiservice;

import org.jdesktop.application.TaskService;

import de.invesdwin.context.client.swing.api.AView;

public interface IGuiService {

    StatusBar getStatusBar();

    SplashScreen getSplashScreen();

    ContentPane getContentPane();

    TaskService getTaskService();

    void hideModalView();

    void showModalView(AView<?, ?> view);

    void showView(AView<?, ?> view);

    boolean isModalViewShowing();

}
