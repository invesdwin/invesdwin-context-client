package de.invesdwin.context.client.swing.api.guiservice;

import java.awt.Component;

public interface IGuiService {

    StatusBar getStatusBar();

    SplashScreen getSplashScreen();

    ContentPane getContentPane();

    /**
     * Executes gui service tasks and updates all components of this has not been skipped. If it was skipped, only the
     * relevant components will be updated.
     */
    void processRequestFinally(Component component);

}
