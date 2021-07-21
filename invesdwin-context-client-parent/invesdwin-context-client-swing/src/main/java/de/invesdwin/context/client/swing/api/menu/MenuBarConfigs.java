package de.invesdwin.context.client.swing.api.menu;

import java.awt.Component;

import javax.annotation.concurrent.Immutable;
import javax.swing.JMenu;

import org.jdesktop.application.ResourceMap;

import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.context.client.swing.frame.content.WorkingAreaLocation;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.Strings;

@Immutable
public final class MenuBarConfigs {

    public static final String SUB_MENU_SEPARATOR = "/";

    private MenuBarConfigs() {
    }

    public static void addSubMenuView(final AView<?, ?> chartView, final JMenu menu) {
        final String title = chartView.getTitle();
        final String[] titleSplit = Strings.splitPreserveAllTokens(title, SUB_MENU_SEPARATOR);
        final int lastIndex = titleSplit.length - 1;
        JMenu curSubMenu = menu;
        for (int i = 0; i < titleSplit.length; i++) {
            final String newSubMenuName = titleSplit[i];
            if (i == lastIndex) {
                Assertions.checkNotBlank(newSubMenuName, "At: %s", title);
                menu.add(new ShowViewMenuItem(chartView, WorkingAreaLocation.Center) {
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

    public static JMenu getOrCreateSubMenu(final JMenu curSubMenu, final String newSubMenuName) {
        if (Strings.isBlank(newSubMenuName)) {
            return curSubMenu;
        }
        final Component[] children = curSubMenu.getMenuComponents();
        for (int i = 0; i < children.length; i++) {
            final Component child = children[i];
            if (child instanceof JMenu) {
                final JMenu cChild = (JMenu) child;
                if (newSubMenuName.equals(cChild.getName())) {
                    return cChild;
                }
            }
        }
        final JMenu newSubMenu = new JMenu(newSubMenuName);
        curSubMenu.add(newSubMenuName);
        return newSubMenu;
    }

}
