package de.invesdwin.common.client.swing;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import de.invesdwin.common.client.swing.internal.splash.ConfiguredSplashScreen;

@ThreadSafe
public class SplashScreen {

    @Inject
    private ConfiguredSplashScreen view;

    public void splash(final boolean force) {
        view.splash(force);
    }

    public void dispose() {
        view.dispose();
    }
}
