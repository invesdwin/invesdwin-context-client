package de.invesdwin.context.client.swing.api.exit;

import java.util.EventObject;

import javax.annotation.concurrent.Immutable;

import org.jdesktop.application.ResourceMap;

import de.invesdwin.context.client.swing.impl.app.DelegateRichApplication;
import de.invesdwin.util.swing.Dialogs;

@Immutable
public class ConfirmExitListener extends ExitListenerSupport {

    @Override
    public boolean canExit(final EventObject event) {
        final DelegateRichApplication application = new DelegateRichApplication();
        final ResourceMap resourceMap = application.getContext().getResourceMap();
        final int result = Dialogs.showConfirmDialog(application.getMainFrame(),
                resourceMap.getString("exit.confirm.message"), resourceMap.getString("exit.text"),
                Dialogs.YES_NO_OPTION);
        return result == Dialogs.YES_OPTION;
    }

}
