package de.invesdwin.context.client.swing.api;

import java.awt.Dimension;

import de.invesdwin.context.client.swing.api.menu.IMenuBarConfig;

public interface IRichApplication {

    boolean hideMainFrameOnStartup();

    boolean keepSplashVisible();

    AView<?, ?> getInitialView();

    Dimension getInitialFrameSize();

    IMenuBarConfig getMenuBarConfig();

    void startupDone(String[] args);

}
