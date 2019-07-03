package de.invesdwin.context.client.swing.api.exit;

import java.util.EventObject;

import javax.annotation.concurrent.Immutable;

import org.jdesktop.application.ResourceMap;

import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.impl.app.DelegateRichApplication;
import de.invesdwin.util.swing.Dialogs;

@Immutable
public class ConfirmExitListener extends ExitListenerSupport {

    @Override
    public boolean canExit(final EventObject event) {
        final DelegateRichApplication application = DelegateRichApplication.getInstance();
        final ResourceMap resourceMap = application.getContext().getResourceMap();
        final int result = Dialogs.showConfirmDialog(GuiService.get().getWindow(),
                resourceMap.getString("exit.confirm.message"), resourceMap.getString("exit.text"),
                Dialogs.YES_NO_OPTION);
        return result == Dialogs.YES_OPTION;
    }

}
