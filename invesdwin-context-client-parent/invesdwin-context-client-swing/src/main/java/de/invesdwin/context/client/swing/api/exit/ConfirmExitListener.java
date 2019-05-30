package de.invesdwin.context.client.swing.api.exit;

import java.util.EventObject;

import javax.annotation.concurrent.Immutable;
import javax.swing.JFrame;

import org.jdesktop.application.ResourceMap;

import de.invesdwin.context.client.swing.impl.app.DelegateRichApplication;
import de.invesdwin.util.swing.Dialogs;

@Immutable
public class ConfirmExitListener extends ExitListenerSupport {

    private final DelegateRichApplication application;
    private final JFrame frame;

    public ConfirmExitListener(final DelegateRichApplication application, final JFrame frame) {
        this.application = application;
        this.frame = frame;
    }

    @Override
    public boolean canExit(final EventObject event) {
        final ResourceMap resourceMap = application.getContext().getResourceMap();
        final int result = Dialogs.showConfirmDialog(frame, resourceMap.getString("exit.confirm.message"),
                resourceMap.getString("exit.text"), Dialogs.YES_NO_OPTION);
        return result == Dialogs.YES_OPTION;
    }

}
