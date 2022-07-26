package de.invesdwin.context.client.swing.util;

import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.swing.JTable;

@Immutable
public abstract class ATableRendererRowsProvider<E> {

    public abstract List<E> getRows();

    public E getViewRow(final JTable table, final int viewRowIndex) {
        final List<E> rows = getRows();
        if (rows.isEmpty()) {
            return null;
        }
        final int modelRowIndex = table.convertRowIndexToModel(viewRowIndex);
        if (modelRowIndex >= rows.size()) {
            return null;
        }
        return rows.get(modelRowIndex);
    }

    public E getModelRow(final int modelRowIndex) {
        final List<E> rows = getRows();
        if (rows.isEmpty()) {
            return null;
        }
        if (modelRowIndex >= rows.size()) {
            return null;
        }
        return rows.get(modelRowIndex);
    }

}
