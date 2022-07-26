package de.invesdwin.context.client.swing.util;

import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.swing.JTable;

@Immutable
public abstract class ATableRendererRowsProvider<E> {

    public abstract List<E> getRows();

    public E getRow(final JTable table, final int viewRowIndex) {
        final List<E> rows = getRows();
        if (rows.isEmpty()) {
            return null;
        }
        final int modelRowIdx = table.convertRowIndexToModel(viewRowIndex);
        if (modelRowIdx >= rows.size()) {
            return null;
        }
        return rows.get(modelRowIdx);
    }

}
