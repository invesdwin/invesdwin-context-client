package de.invesdwin.context.client.swing.layout;

import java.awt.event.ActionEvent;

import javax.annotation.concurrent.ThreadSafe;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import de.invesdwin.context.client.swing.api.guiservice.ContentPane;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.api.guiservice.IGuiService;

@ThreadSafe
public class AddViewMenuItem extends JMenuItem {

    public AddViewMenuItem(final ContentPane contentPane) {
        setAction(new AbstractAction("AddView") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final AddViewView addViewView = new AddViewView(new AddView(contentPane));
                GuiService.get().showModalView(addViewView, IGuiService.PACK_DIMENSION);
            }
        });
    }
}
