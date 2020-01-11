package de.invesdwin.context.client.swing.frame.menu;

import java.awt.Component;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;

import de.invesdwin.context.client.swing.api.IRichApplication;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.api.menu.IMenuBarConfig;
import de.invesdwin.context.client.swing.api.menu.MenuBarConfigSupport;
import de.invesdwin.context.client.swing.api.menu.OpenViewMenuItem;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.util.swing.Dialogs;

@SuppressWarnings("serial")
@ThreadSafe
public class MenuBarView extends AView<MenuBarView, JMenuBar> {

    @Inject
    private IRichApplication richApplication;

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    protected JMenuBar initComponent() {
        IMenuBarConfig menuBarConfig = richApplication.getMenuBarConfig();
        if (menuBarConfig == null) {
            menuBarConfig = new MenuBarConfigSupport();
        }

        final JMenuBar menuBar = new JMenuBar();
        addFileMenu(menuBarConfig, menuBar);
        addEditMenu(menuBarConfig, menuBar);
        addViewMenu(menuBarConfig, menuBar);
        addOtherMenus(menuBarConfig, menuBar);
        addHelpMenu(menuBarConfig, menuBar);

        return menuBar;
    }

    private void addFileMenu(final IMenuBarConfig menuBarConfig, final JMenuBar menuBar) {
        final JMenu mnFile = new JMenu();
        mnFile.setName("file");

        final List<JMenuItem> fileMenuItems = menuBarConfig.getFileMenuItems();
        if (fileMenuItems != null && fileMenuItems.size() > 0) {
            for (final JMenuItem dateiMenuItem : fileMenuItems) {
                if (dateiMenuItem == null) {
                    mnFile.add(new JSeparator());
                } else {
                    mnFile.add(dateiMenuItem);
                }
            }
            mnFile.add(new JSeparator());
        }

        if (menuBarConfig.isAddExitInFileMenu()) {
            final JMenuItem mntmExit = new JMenuItem();
            mntmExit.setName("exit");
            mnFile.add(mntmExit);
        }

        if (mnFile.getMenuComponentCount() > 0) {
            menuBar.add(mnFile);
        }
    }

    private void addEditMenu(final IMenuBarConfig menuBarConfig, final JMenuBar menuBar) {
        final JMenu mnEdit = new JMenu();
        mnEdit.setName("edit");
        menuBar.add(mnEdit);

        //Start is the same as TextFieldPopupMenu
        for (final Component c : new TextFieldPopupMenuView(null).getComponent().getComponents()) {
            mnEdit.add(c);
        }

        final List<JMenuItem> editMenuItems = menuBarConfig.getEditMenuItems();
        if (editMenuItems != null && editMenuItems.size() > 0) {
            mnEdit.add(new JSeparator());
            for (final JMenuItem bearbeitenMenuItem : editMenuItems) {
                if (bearbeitenMenuItem == null) {
                    mnEdit.add(new JSeparator());
                } else {
                    mnEdit.add(bearbeitenMenuItem);
                }
            }
        }
    }

    private void addOtherMenus(final IMenuBarConfig menuBarConfig, final JMenuBar menuBar) {
        final List<JMenu> otherMenues = menuBarConfig.getOtherMenus();
        if (otherMenues != null && otherMenues.size() > 0) {
            for (final JMenu otherMenue : otherMenues) {
                menuBar.add(otherMenue);
            }
        }
    }

    private void addViewMenu(final IMenuBarConfig menuBarConfig, final JMenuBar menuBar) {
        final List<OpenViewMenuItem<?>> openViewMenuItems = menuBarConfig.getOpenViewMenuItems();
        if (openViewMenuItems != null && openViewMenuItems.size() > 0) {
            final JMenu mnView = new JMenu();
            mnView.setName("view");
            menuBar.add(mnView);
            for (final OpenViewMenuItem<?> openViewMenuItem : openViewMenuItems) {
                if (openViewMenuItem == null) {
                    mnView.add(new JSeparator());
                } else {
                    mnView.add(openViewMenuItem);
                }
            }
        }
    }

    private void addHelpMenu(final IMenuBarConfig menuBarConfig, final JMenuBar menuBar) {
        final JMenu mnHelp = new JMenu();
        mnHelp.setName("help");
        menuBar.add(mnHelp);

        final List<JMenuItem> helpMenuItems = menuBarConfig.getHelpMenuItems();
        if (helpMenuItems != null && helpMenuItems.size() > 0) {
            for (final JMenuItem helpMenuItem : helpMenuItems) {
                if (helpMenuItem == null) {
                    mnHelp.add(new JSeparator());
                } else {
                    mnHelp.add(helpMenuItem);
                }
            }
            mnHelp.add(new JSeparator());
        }

        if (menuBarConfig.isAddAboutInHelpMenu()) {
            final JMenuItem mntmAbout = new JMenuItem();
            mntmAbout.setName("about");
            mnHelp.add(mntmAbout);
        }
    }

    @Action
    public void exit() {
        richApplication.getMainFrameCloseOperation().close(null);
    }

    @Action
    public void about() {
        final SingleFrameApplication app = (SingleFrameApplication) Application.getInstance();
        final ResourceMap resourceMap = app.getContext().getResourceMap();
        final Icon icon = resourceMap.getIcon("Application.icon");
        final String title = (String) getActionMap().get("about").getValue(javax.swing.Action.NAME);

        final StringBuilder message = new StringBuilder("<html><body><center<h1>");
        message.append(resourceMap.getString("Application.name"));
        message.append("</h1>");
        message.append(resourceMap.getString("Application.version"));
        message.append("<br><br>");
        message.append(resourceMap.getString("Application.description"));
        message.append("<br><br>");
        message.append(resourceMap.getString("Application.vendor"));
        message.append(": <a href=\"");
        final String homepage = resourceMap.getString("Application.homepage");
        message.append(homepage);
        message.append("\">");
        message.append(homepage);
        message.append("</a></center></body></html>");
        Dialogs.showMessageDialog(GuiService.get().getWindow(), message, title, Dialogs.INFORMATION_MESSAGE, icon);
    }

}
