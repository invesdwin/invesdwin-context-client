package de.invesdwin.context.client.swing.api.binding.component.table;

import java.awt.Component;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import de.invesdwin.util.swing.Components;

@NotThreadSafe
public class GeneratedTableCellRenderer implements TableCellRenderer {

    private final GeneratedTableModel model;
    private final TableCellRenderer delegate;
    private JComponent prevComponent;

    public GeneratedTableCellRenderer(final GeneratedTableModel model, final TableCellRenderer delegate) {
        this.model = model;
        this.delegate = delegate;
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
            final boolean hasFocus, final int row, final int column) {
        if (prevComponent != null) {
            Components.setForeground(prevComponent, null);
            Components.setBackground(prevComponent, null);
            Components.setToolTipText(prevComponent, null, false);
            Components.setText(prevComponent, null);
        }
        final JComponent component = (JComponent) delegate.getTableCellRendererComponent(table, value, isSelected,
                hasFocus, row, column);
        if (component.getToolTipText() == null) {
            final String tooltip = model.getTooltipAt(row, column);
            Components.setToolTipText(component, tooltip, false);
        }
        prevComponent = component;
        return component;
    }

}
