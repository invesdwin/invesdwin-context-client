package de.invesdwin.context.client.swing.test.edt;

import java.awt.AWTEvent;

public interface ITimeoutEventQueueListener {

    void onTimeout(AWTEvent event, Thread eventDispatchThread);

}
