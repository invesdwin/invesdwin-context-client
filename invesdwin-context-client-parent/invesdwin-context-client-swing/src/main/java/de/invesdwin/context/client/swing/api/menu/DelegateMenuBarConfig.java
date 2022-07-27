package de.invesdwin.context.client.swing.api.menu;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

@NotThreadSafe
public class DelegateMenuBarConfig implements IMenuBarConfig {

    private final IMenuBarConfig delegate;

    public DelegateMenuBarConfig(final IMenuBarConfig delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<JMenuItem> getFileMenuItems() {
        return delegate.getFileMenuItems();
    }

    @Override
    public List<JMenuItem> getEditMenuItems() {
        return delegate.getEditMenuItems();
    }

    @Override
    public List<JMenuItem> getViewMenuItems() {
        return delegate.getViewMenuItems();
    }

    @Override
    public List<JMenu> getOtherMenus() {
        return delegate.getOtherMenus();
    }

    @Override
    public List<JMenuItem> getHelpMenuItems() {
        return delegate.getHelpMenuItems();
    }

}
