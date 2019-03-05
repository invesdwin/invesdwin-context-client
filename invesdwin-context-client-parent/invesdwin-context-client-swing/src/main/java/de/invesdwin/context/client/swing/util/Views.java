package de.invesdwin.context.client.swing.util;

import java.awt.Component;

import javax.annotation.concurrent.Immutable;
import javax.swing.JComponent;

import bibliothek.gui.dock.util.BackgroundPanel;
import de.invesdwin.context.client.swing.api.AView;

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
        if (component instanceof JComponent) {
            final JComponent container = (JComponent) component;
            final AView<?, ?> view = (AView<?, ?>) container.getClientProperty(AView.CLIENTPROP_VIEW_INSTANCE);
            return view;
        }
        return null;
    }

    public static Component getRootComponentInDockable(final Component component) {
        Component parent = component;
        while (parent.getParent() != null && !(parent.getParent() instanceof BackgroundPanel)) {
            parent = parent.getParent();
        }
        return parent;
    }

    public static void updateAllViews(final AView<?, ?> view) {
        updateAllViews(view.getComponent());
    }

    public static void updateAllViews(final Component component) {
        UpdateAllViewsHelper.update(component);
    }

    public static void submitAllViews(final AView<?, ?> view) {
        submitAllViews(view.getComponent());
    }

    public static void submitAllViews(final Component component) {
        SubmitAllViewsHelper.submit(component);
    }

}
