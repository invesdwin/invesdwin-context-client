package de.invesdwin.context.client.swing.layout;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.swing.JMenuItem;

import de.invesdwin.context.client.swing.api.RichApplicationSupport;
import de.invesdwin.context.client.swing.api.guiservice.ContentPane;
import de.invesdwin.context.client.swing.api.menu.IMenuBarConfig;
import de.invesdwin.context.client.swing.api.menu.MenuBarConfigSupport;
import de.invesdwin.util.swing.HiDPI;
import jakarta.inject.Inject;

@Immutable
public class TestSaveRestoreLayoutRichApplication extends RichApplicationSupport {

    @Inject
    private ContentPane contentPane;

    @Override
    public Dimension getInitialFrameSize() {
        return HiDPI.scale(new Dimension(1024, 768));
    }

    @Override
    public boolean isSaveRestorePersistentLayout() {
        return true;
    }

    @Override
    public IMenuBarConfig newMenuBarConfig() {
        return new MenuBarConfigSupport() {
            @Override
            public List<JMenuItem> getEditMenuItems() {
                final List<JMenuItem> items = new ArrayList<>();
                items.add(new AddViewMenuItem(contentPane));

                final List<JMenuItem> superItems = super.getHelpMenuItems();
                if (superItems != null) {
                    items.addAll(superItems);
                }
                return items;
            }
        };
    }
}
