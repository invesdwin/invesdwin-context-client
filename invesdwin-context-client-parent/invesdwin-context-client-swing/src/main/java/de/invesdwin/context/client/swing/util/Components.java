package de.invesdwin.context.client.swing.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ContainerListener;

import javax.annotation.concurrent.Immutable;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import bibliothek.gui.dock.util.BackgroundPanel;
import de.invesdwin.context.client.swing.api.AView;

@Immutable
public final class Components {

    private Components() {}

    public static void updateForeground(final Component component, final Color foregroundColor) {
        if (!foregroundColor.equals(component.getForeground())) {
            component.setForeground(foregroundColor);
        }
    }

    public static void updateBackground(final Component component, final Color backgroundColor) {
        if (!backgroundColor.equals(component.getBackground())) {
            component.setBackground(backgroundColor);
        }
    }

    public static void updateRowHeight(final JTable table, final int row, final int height) {
        if (height != table.getRowHeight(row)) {
            table.setRowHeight(row, height);
        }
    }

    public static void updateMinWidth(final TableColumn column, final int minWidth) {
        if (minWidth != column.getMinWidth()) {
            column.setMinWidth(minWidth);
        }
    }

    public static void updateMaxWidth(final TableColumn column, final int maxWidth) {
        if (maxWidth != column.getMaxWidth()) {
            column.setMaxWidth(maxWidth);
        }
    }

    public static void updatePreferredWidth(final TableColumn column, final int preferredWidth) {
        if (preferredWidth != column.getPreferredWidth()) {
            column.setPreferredWidth(preferredWidth);
        }
    }

    public static Component getRootComponentInDockable(final Component component) {
        Component parent = component;
        while (parent.getParent() != null && !(parent.getParent() instanceof BackgroundPanel)) {
            parent = parent.getParent();
        }
        return parent;
    }

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
        if (component instanceof Container) {
            final Container container = (Container) component;
            final ContainerListener[] containerListeners = container.getContainerListeners();
            for (int i = 0; i < containerListeners.length; i++) {
                final ContainerListener l = containerListeners[i];
                if (l instanceof ViewAttachingContainerListener) {
                    final ViewAttachingContainerListener viewL = (ViewAttachingContainerListener) l;
                    final AView<?, ?> view = viewL.getView();
                    return view;
                }
            }
        }
        return null;
    }

}
