package de.invesdwin.context.client.swing.api.exit;

import java.util.EventObject;

import javax.annotation.concurrent.Immutable;

import org.jdesktop.application.Application.ExitListener;

@Immutable
public class ExitListenerSupport implements ExitListener {

    @Override
    public boolean canExit(final EventObject event) {
        return true;
    }

    @Override
    public void willExit(final EventObject event) {}

}
