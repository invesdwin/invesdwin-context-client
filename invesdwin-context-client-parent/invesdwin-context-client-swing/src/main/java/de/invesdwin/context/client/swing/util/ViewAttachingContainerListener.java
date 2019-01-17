package de.invesdwin.context.client.swing.util;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.listener.ContainerListenerSupport;

@Immutable
public final class ViewAttachingContainerListener extends ContainerListenerSupport {

    private final AView<?, ?> view;

    public ViewAttachingContainerListener(final AView<?, ?> view) {
        this.view = view;
    }

    public AView<?, ?> getView() {
        return view;
    }

}