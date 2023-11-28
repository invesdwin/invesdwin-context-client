package de.invesdwin.context.client.swing.api.hook;

import javax.annotation.concurrent.NotThreadSafe;

import jakarta.inject.Named;

@Named
@NotThreadSafe
public class RichApplicationHookSupport implements IRichApplicationHook {

    @Override
    public void initializeDone() {}

    @Override
    public void startupDone() {}

    @Override
    public void showMainFrameDone() {}

    @Override
    public void hideMainFrameDone() {}

    @Override
    public void shutdownDone() {}

}
