package de.invesdwin.context.client.swing.api.menu;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public interface IMenuBarConfig {

    /**
     * Null elements add a JSeparator.
     */
    List<JMenuItem> getFileMenuItems();

    /**
     * Null elements add a JSeparator.
     */
    List<JMenuItem> getEditMenuItems();

    /**
     * Null elements add a JSeparator.
     */
    List<OpenViewMenuItem<?>> getOpenViewMenuItems();

    /**
     * No null elements supported.
     */
    List<JMenu> getOtherMenus();

    /**
     * Null elements add a JSeparator.
     */
    List<JMenuItem> getHelpMenuItems();

    default boolean isAddAboutInHelpMenu() {
        return true;
    }

    default boolean isAddExitInFileMenu() {
        return true;
    }

}
