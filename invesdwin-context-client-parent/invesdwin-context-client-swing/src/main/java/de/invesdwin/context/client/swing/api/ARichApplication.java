package de.invesdwin.context.client.swing.api;

import java.awt.Dimension;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.client.swing.api.menu.IMenuBarConfig;

@Immutable
public abstract class ARichApplication implements IRichApplication {

    @Override
    public boolean isHideMainFrameOnStartup() {
        return false;
    }

    @Override
    public boolean isHideSplashOnStartup() {
        return false;
    }

    @Override
    public MainFrameCloseOperation getMainFrameCloseOperation() {
        return MainFrameCloseOperation.SystemExit;
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

}
