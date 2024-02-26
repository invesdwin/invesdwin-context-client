package de.invesdwin.context.client.swing.component;

import javax.annotation.concurrent.Immutable;

import org.japura.util.i18n.HandlerString;

import de.invesdwin.context.client.swing.api.guiservice.GuiService;

@Immutable
public class InvesdwinJapuraGUIHandlerString implements HandlerString {

    @Override
    public String getString(final String key) {
        if (key == null) {
            return null;
        }

        return GuiService.i18n(this.getClass(), key);
    }
}
