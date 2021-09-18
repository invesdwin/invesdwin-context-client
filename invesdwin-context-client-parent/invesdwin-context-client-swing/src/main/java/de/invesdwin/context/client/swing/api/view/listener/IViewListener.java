package de.invesdwin.context.client.swing.api.view.listener;

public interface IViewListener {

    IViewListener[] EMPTY_ARRAY = new IViewListener[0];

    void onOpen();

    void onClose();

    void onShowing();

}
