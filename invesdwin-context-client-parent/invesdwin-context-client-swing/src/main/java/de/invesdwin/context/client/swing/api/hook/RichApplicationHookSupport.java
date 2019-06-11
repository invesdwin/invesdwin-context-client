package de.invesdwin.context.client.swing.api.hook;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Named;

@Named
@NotThreadSafe
public class RichApplicationHookSupport implements IRichApplicationHook {

    @Override
    public void initializeDone() {}

    @Override
    public void startupDone() {}

}
