package de.invesdwin.context.client.swing.util;

import java.awt.Component;
import java.awt.Window;

import javax.annotation.concurrent.Immutable;
import javax.swing.JComponent;

import bibliothek.gui.dock.util.BackgroundPanel;
import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.api.IDockable;

@Immutable
public final class Views {

    private Views() {}

    public static AView<?, ?> findParentView(final Component component) {
        return findParentView(component, null);
    }

    @SuppressWarnings("unchecked")
    public static <T extends AView<?, ?>> T findParentView(final Component component, final Class<T> type) {
        Component parent = component;
        while (parent != null) {
            final AView<?, ?> view = getViewAt(parent);
            if (view != null && (type == null || type.isAssignableFrom(view.getClass()))) {
                return (T) view;
            }
            parent = parent.getParent();
        }
        return null;
    }

    public static AView<?, ?> getViewAt(final Component component) {
        if (component instanceof IDockable) {
            final IDockable dockable = (IDockable) component;
            return dockable.getView();
        } else if (component instanceof JComponent) {
            final JComponent container = (JComponent) component;
            final AView<?, ?> view = (AView<?, ?>) container.getClientProperty(AView.CLIENTPROP_VIEW_INSTANCE);
            return view;
        }
        return null;
    }

    public static Component getRootComponentInDockable(final Component component) {
        Component parent = component;
        while (parent.getParent() != null && !(parent.getParent() instanceof BackgroundPanel)
                && !(parent instanceof Window)) {
            parent = parent.getParent();
        }
        return parent;
    }

}
