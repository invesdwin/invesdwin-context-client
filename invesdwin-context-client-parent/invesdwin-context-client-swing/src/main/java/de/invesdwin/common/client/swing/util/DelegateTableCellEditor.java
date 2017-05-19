package de.invesdwin.common.client.swing.util;

import java.awt.Component;
import java.util.EventObject;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

@NotThreadSafe
public class DelegateTableCellEditor implements TableCellEditor {

    private final TableCellEditor delegate;

    public DelegateTableCellEditor(final TableCellEditor delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object getCellEditorValue() {
        return delegate.getCellEditorValue();
    }

    @Override
    public boolean isCellEditable(final EventObject anEvent) {
        return delegate.isCellEditable(anEvent);
    }

    @Override
    public boolean shouldSelectCell(final EventObject anEvent) {
        return delegate.shouldSelectCell(anEvent);
    }

    @Override
    public boolean stopCellEditing() {
        return delegate.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        delegate.cancelCellEditing();
    }

    @Override
    public void addCellEditorListener(final CellEditorListener l) {
        delegate.addCellEditorListener(l);
    }

    @Override
    public void removeCellEditorListener(final CellEditorListener l) {
        delegate.removeCellEditorListener(l);
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected,
            final int row, final int column) {
        return delegate.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

}
