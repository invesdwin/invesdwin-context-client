package de.invesdwin.common.client.swing.util;

import java.awt.Component;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

@NotThreadSafe
public class DelegateTableCellRenderer implements TableCellRenderer {

    private final TableCellRenderer delegate;

    public DelegateTableCellRenderer(final TableCellRenderer delegate) {
        this.delegate = delegate;
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
            final boolean hasFocus, final int row, final int column) {
        return delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

}
