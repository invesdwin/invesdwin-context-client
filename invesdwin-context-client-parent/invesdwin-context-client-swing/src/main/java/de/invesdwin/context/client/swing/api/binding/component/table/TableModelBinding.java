package de.invesdwin.context.client.swing.api.binding.component.table;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.table.AbstractTableModel;

import de.invesdwin.norva.beanpath.impl.clazz.BeanClassType;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.ITableColumnBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.TableButtonColumnBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.TableSelectionButtonColumnBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.SelectionBeanPathPropertyModifier;
import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.lang.Objects;

@NotThreadSafe
public class TableModelBinding extends AbstractTableModel {

    private final List<ITableColumnBeanPathElement> columns;
    private List<?> values = new ArrayList<>();

    public TableModelBinding(final List<ITableColumnBeanPathElement> columns) {
        this.columns = columns;
    }

    public void update(final List<?> newValues) {
        if (!Objects.equals(newValues, values)) {
            this.values = new ArrayList<>(values);
            fireTableDataChanged();
        }
    }

    @Override
    public String getColumnName(final int column) {
        return columns.get(column).getTitle();
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        final BeanClassType type = (BeanClassType) columns.get(columnIndex).getAccessor().getType();
        return type.getType();
    }

    @Override
    public int getRowCount() {
        return values.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final ITableColumnBeanPathElement column = columns.get(columnIndex);
        if (column instanceof APropertyBeanPathElement) {
            final APropertyBeanPathElement property = (APropertyBeanPathElement) column;
            final Object row = values.get(rowIndex);
            return property.getModifier().getValueFromTarget(row);
        } else if (column instanceof TableSelectionButtonColumnBeanPathElement) {
            final TableSelectionButtonColumnBeanPathElement selection = (TableSelectionButtonColumnBeanPathElement) column;
            final SelectionBeanPathPropertyModifier modifier = selection.getSelectionModifier();
            final Object row = values.get(rowIndex);
            return modifier.isSelected(row);
        } else if (column instanceof TableButtonColumnBeanPathElement) {
            return rowIndex;
        } else {
            throw UnknownArgumentException.newInstance(Class.class, column.getClass());
        }
    }

}
