package de.invesdwin.context.client.swing.api;

import java.awt.Dimension;
import java.util.EventObject;
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
    public boolean isHideSplashOnStartup() {
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
    public void startupDone(final String[] args) {}

    @Override
    public boolean canExit(final EventObject event) {
        return true;
    }

    @Override
    public void willExit(final EventObject event) {}

}
