package de.invesdwin.context.client.swing.api.guiservice;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

@ThreadSafe
public class SplashScreen {

    @Inject
    private de.invesdwin.context.client.swing.impl.splash.ConfiguredSplashScreen view;

    public void splash(final boolean force) {
        view.splash(force);
    }

    public void dispose() {
        view.dispose();
    }
}
