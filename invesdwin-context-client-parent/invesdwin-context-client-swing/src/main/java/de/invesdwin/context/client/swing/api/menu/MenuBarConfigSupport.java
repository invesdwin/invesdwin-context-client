package de.invesdwin.context.client.swing.api.menu;

import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

@Immutable
public class MenuBarConfigSupport implements IMenuBarConfig {

    @Override
    public List<JMenuItem> getFileMenuItems() {
        return null;
    }

    @Override
    public List<JMenuItem> getEditMenuItems() {
        return null;
    }

    @Override
    public List<JMenuItem> getViewMenuItems() {
        return null;
    }

    @Override
    public List<JMenu> getOtherMenus() {
        return null;
    }

    @Override
    public List<JMenuItem> getHelpMenuItems() {
        return null;
    }

}
