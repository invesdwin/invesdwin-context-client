package de.invesdwin.context.client.swing.api;

import java.awt.Dimension;
import java.util.Locale;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.client.swing.api.exit.AMainFrameCloseOperation;
import de.invesdwin.context.client.swing.api.menu.IMenuBarConfig;

@Immutable
public abstract class ARichApplication implements IRichApplication {

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
    public AView<?, ?> getInitialView() {
        return null;
    }

    @Override
    public Dimension getInitialFrameSize() {
        return null;
    }

    @Override
    public IMenuBarConfig getMenuBarConfig() {
        return null;
    }

    @Override
    public void initializeDone() {}

    @Override
    public void startupDone() {}

}
