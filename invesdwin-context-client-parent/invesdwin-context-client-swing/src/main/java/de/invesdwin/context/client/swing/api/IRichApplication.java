package de.invesdwin.context.client.swing.api;

import java.awt.Dimension;
import java.util.Locale;

import de.invesdwin.context.client.swing.api.exit.AMainFrameCloseOperation;
import de.invesdwin.context.client.swing.api.menu.IMenuBarConfig;

public interface IRichApplication {

    Locale getLocaleOverride();

    String getLookAndFeelOverride();

    boolean isHideMainFrameOnStartup();

    boolean isHideSplashOnStartup();

    AMainFrameCloseOperation getMainFrameCloseOperation();

    boolean isKeepSplashVisible();

    AView<?, ?> getInitialView();

    Dimension getInitialFrameSize();

    IMenuBarConfig getMenuBarConfig();

    void startupDone();

}
