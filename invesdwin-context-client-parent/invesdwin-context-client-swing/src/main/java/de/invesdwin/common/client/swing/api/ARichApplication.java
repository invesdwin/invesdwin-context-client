package de.invesdwin.common.client.swing.api;

import java.awt.Dimension;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.common.client.swing.api.menu.IMenuBarConfig;

@Immutable
public abstract class ARichApplication implements IRichApplication {

    @Override
    public boolean hideMainFrameOnStartup() {
        return false;
    }

    @Override
    public boolean keepSplashVisible() {
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
