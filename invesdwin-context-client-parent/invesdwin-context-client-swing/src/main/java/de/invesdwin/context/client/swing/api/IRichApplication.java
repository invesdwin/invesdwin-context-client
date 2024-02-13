package de.invesdwin.context.client.swing.api;

import java.awt.Dimension;
import java.util.Locale;

import de.invesdwin.context.client.swing.api.exit.AMainFrameCloseOperation;
import de.invesdwin.context.client.swing.api.guiservice.ContentPane;
import de.invesdwin.context.client.swing.api.menu.IMenuBarConfig;

public interface IRichApplication {

    Locale getLocaleOverride();

    String getLookAndFeelOverride();

    boolean isHideMainFrameOnStartup();

    AMainFrameCloseOperation getMainFrameCloseOperation();

    boolean isKeepSplashVisible();

    void showInitialViews(ContentPane contentPane);

    boolean isSaveRestorePersistentLayout();

    Dimension getInitialFrameSize();

    IMenuBarConfig newMenuBarConfig();

    void initializeDone();

    void startupDone();

    void showMainFrameDone();

    void hideMainFrameDone();

    void shutdownDone();

}
