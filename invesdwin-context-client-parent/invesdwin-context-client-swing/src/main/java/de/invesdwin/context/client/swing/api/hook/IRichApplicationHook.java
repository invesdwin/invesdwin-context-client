package de.invesdwin.context.client.swing.api.hook;

public interface IRichApplicationHook {

    void initializeDone();

    void startupDone();

    void showMainFrameDone();

    void hideMainFrameDone();

    void shutdownDone();

}
