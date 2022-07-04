package de.invesdwin.context.client.swing.api.menu;

import java.awt.Component;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.jdesktop.application.ResourceMap;

import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.context.client.swing.frame.content.IWorkingAreaLocation;
import de.invesdwin.context.client.swing.frame.content.WorkingAreaLocation;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.Strings;

@Immutable
public final class MenuBarConfigs {

    public static final String SUB_MENU_SEPARATOR = "/";

    private MenuBarConfigs() {
    }

    public static void addSubMenuView(final AView<?, ?> chartView, final List<JMenuItem> menuList) {
        addSubMenuView(chartView, menuList, WorkingAreaLocation.Center);
    }

    public static void addSubMenuView(final AView<?, ?> chartView, final List<JMenuItem> menuList,
            final IWorkingAreaLocation location) {
        final String title = chartView.getTitle();
        final String[] titleSplit = Strings.splitPreserveAllTokens(title, SUB_MENU_SEPARATOR);
        final int lastIndex = titleSplit.length - 1;
        JMenu curSubMenu = null;
        for (int i = 0; i < titleSplit.length; i++) {
            final String newSubMenuName = titleSplit[i];
            if (i == lastIndex) {
                Assertions.checkNotBlank(newSubMenuName, "At: %s", title);
                final ShowViewMenuItem menuItem = new ShowViewMenuItem(chartView, location) {
                    @Override
                    protected String newTitle(final ResourceMap resourceMap) {
                        return newSubMenuName;
                    }
                };
                if (curSubMenu == null) {
                    menuList.add(curSubMenu);
                } else {
                    curSubMenu.add(menuItem);
                }
            } else {
                if (curSubMenu == null) {
                    curSubMenu = getOrCreateSubMenu(menuList, newSubMenuName);
                } else {
                    curSubMenu = getOrCreateSubMenu(curSubMenu, newSubMenuName);
                }
            }
        }
    }

    public static void addSubMenuView(final AView<?, ?> chartView, final JMenu menu) {
        addSubMenuView(chartView, menu, WorkingAreaLocation.Center);
    }

    public static void addSubMenuView(final AView<?, ?> chartView, final JMenu menu,
            final IWorkingAreaLocation location) {
        final String title = chartView.getTitle();
        final String[] titleSplit = Strings.splitPreserveAllTokens(title, SUB_MENU_SEPARATOR);
        final int lastIndex = titleSplit.length - 1;
        JMenu curSubMenu = menu;
        for (int i = 0; i < titleSplit.length; i++) {
            final String newSubMenuName = titleSplit[i];
            if (i == lastIndex) {
                Assertions.checkNotBlank(newSubMenuName, "At: %s", title);
                curSubMenu.add(new ShowViewMenuItem(chartView, location) {
                    @Override
                    protected String newTitle(final ResourceMap resourceMap) {
                        return newSubMenuName;
                    }
                });
            } else {
                curSubMenu = getOrCreateSubMenu(curSubMenu, newSubMenuName);
            }
        }
    }

    public static JMenu getOrCreateSubMenu(final List<JMenuItem> curSubMenuList, final String newSubMenuName) {
        if (Strings.isBlank(newSubMenuName)) {
            return null;
        }
        for (int i = 0; i < curSubMenuList.size(); i++) {
            final JMenuItem child = curSubMenuList.get(i);
            if (child instanceof JMenu) {
                final JMenu cChild = (JMenu) child;
                if (newSubMenuName.equals(cChild.getText())) {
                    return cChild;
                }
            }
        }
        final JMenu newSubMenu = new JMenu(newSubMenuName);
        curSubMenuList.add(newSubMenu);
        return newSubMenu;
    }

    public static JMenu getOrCreateSubMenu(final JMenu curSubMenu, final String newSubMenuName) {
        if (Strings.isBlank(newSubMenuName)) {
            return curSubMenu;
        }
        final Component[] children = curSubMenu.getMenuComponents();
        for (int i = 0; i < children.length; i++) {
            final Component child = children[i];
            if (child instanceof JMenu) {
                final JMenu cChild = (JMenu) child;
                if (newSubMenuName.equals(cChild.getText())) {
                    return cChild;
                }
            }
        }
        final JMenu newSubMenu = new JMenu(newSubMenuName);
        curSubMenu.add(newSubMenu);
        return newSubMenu;
    }

}
