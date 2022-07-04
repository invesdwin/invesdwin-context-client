package de.invesdwin.context.client.swing.api;

import java.awt.Dimension;
import java.util.Locale;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.client.swing.api.exit.AMainFrameCloseOperation;
import de.invesdwin.context.client.swing.api.guiservice.ContentPane;
import de.invesdwin.context.client.swing.api.menu.IMenuBarConfig;

@Immutable
public class RichApplicationSupport implements IRichApplication {

    @Override
    public Locale getLocaleOverride() {
        return null;
    }

    /**
     * If this is null, the native look and feel will be used
     */
    @Override
    public String getLookAndFeelOverride() {
        return null;
    }

    @Override
    public boolean isHideMainFrameOnStartup() {
        return false;
    }

    @Override
    public AMainFrameCloseOperation getMainFrameCloseOperation() {
        return AMainFrameCloseOperation.EXIT;
    }

    @Override
    public boolean isKeepSplashVisible() {
        return false;
    }

    @Override
    public void showInitialViews(final ContentPane contentPane) {
    }

    @Override
    public Dimension getInitialFrameSize() {
        return null;
    }

    @Override
    public IMenuBarConfig newMenuBarConfig() {
        return null;
    }

    @Override
    public void initializeDone() {
    }

    @Override
    public void startupDone() {
    }

    @Override
    public void shutdownDone() {
    }

}
