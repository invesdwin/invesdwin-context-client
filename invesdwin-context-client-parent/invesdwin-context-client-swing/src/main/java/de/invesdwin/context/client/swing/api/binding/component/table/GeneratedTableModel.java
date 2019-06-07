package de.invesdwin.context.client.swing.api.binding.component.table;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang3.BooleanUtils;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassType;
import de.invesdwin.norva.beanpath.impl.object.BeanObjectContainer;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.ATableBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.ITableColumnBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.TableButtonColumnBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.TableSelectionButtonColumnBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.SelectionBeanPathPropertyModifier;
import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.lang.Objects;

@NotThreadSafe
public class GeneratedTableModel extends AbstractTableModel {

    private final Runnable eagerSubmitRunnable;
    private final ATableBeanPathElement element;
    private List<ITableColumnBeanPathElement> columns;
    private List<?> rows = new ArrayList<>();
    private final BindingGroup bindingGroup;
    private final GeneratedTableSelectionModel selectionModel;

    public GeneratedTableModel(final Runnable eagerSubmitRunnable, final ATableBeanPathElement element,
            final BindingGroup bindingGroup, final GeneratedTableSelectionModel selectionModel) {
        this.eagerSubmitRunnable = eagerSubmitRunnable;
        this.element = element;
        this.columns = element.getColumns();
        this.bindingGroup = bindingGroup;
        this.selectionModel = selectionModel;
    }

    public void fromModelToComponent(final List<?> newValues) {
        this.rows = new ArrayList<>(newValues);
        selectionModel.setValueIsFrozen(true);
        try {
            final List<ITableColumnBeanPathElement> newColumns = element.getColumns();
            if (!Objects.equals(newColumns, columns)) {
                this.columns = new ArrayList<>(columns);
                fireTableStructureChanged();
            } else {
                fireTableDataChanged();
            }
        } finally {
            selectionModel.setValueIsFrozen(false);
        }
    }

    protected Object getTarget() {
        final BeanObjectContainer container = (BeanObjectContainer) element.getContainer();
        return container.getObject();
    }

    @Override
    public String getColumnName(final int column) {
        return bindingGroup.getTitle(columns.get(column), null);
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        final ITableColumnBeanPathElement column = columns.get(columnIndex);
        if (column instanceof APropertyBeanPathElement) {
            final BeanClassType type = (BeanClassType) column.getAccessor().getType();
            return type.getType();
        } else if (column instanceof TableSelectionButtonColumnBeanPathElement) {
            return Boolean.class;
        } else if (column instanceof TableButtonColumnBeanPathElement) {
            return Integer.class;
        } else {
            throw UnknownArgumentException.newInstance(Class.class, column.getClass());
        }
    }

    @Override
    public int getRowCount() {
        return rows.size();
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
            final Object row = rows.get(rowIndex);
            return property.getModifier().getValueFromTarget(row);
        } else if (column instanceof TableSelectionButtonColumnBeanPathElement) {
            final TableSelectionButtonColumnBeanPathElement selection = (TableSelectionButtonColumnBeanPathElement) column;
            final SelectionBeanPathPropertyModifier modifier = selection.getSelectionModifier();
            final Object row = rows.get(rowIndex);
            return modifier.isSelected(row);
        } else if (column instanceof TableButtonColumnBeanPathElement) {
            return rowIndex;
        } else {
            throw UnknownArgumentException.newInstance(Class.class, column.getClass());
        }
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        final ITableColumnBeanPathElement column = columns.get(columnIndex);
        if (column instanceof APropertyBeanPathElement) {
            final Object row = rows.get(rowIndex);
            return column.isEnabled(row);
        } else if (column instanceof TableSelectionButtonColumnBeanPathElement) {
            return true;
        } else if (column instanceof TableButtonColumnBeanPathElement) {
            return false;
        } else {
            throw UnknownArgumentException.newInstance(Class.class, column.getClass());
        }
    }

    @Override
    public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
        final ITableColumnBeanPathElement column = columns.get(columnIndex);
        if (column instanceof APropertyBeanPathElement) {
            final APropertyBeanPathElement property = (APropertyBeanPathElement) column;
            final Object row = rows.get(rowIndex);
            property.getModifier().setValueFromTarget(row, value);
        } else if (column instanceof TableSelectionButtonColumnBeanPathElement) {
            final TableSelectionButtonColumnBeanPathElement selection = (TableSelectionButtonColumnBeanPathElement) column;
            final SelectionBeanPathPropertyModifier modifier = selection.getSelectionModifier();
            final Object row = rows.get(rowIndex);
            final Boolean selected = (Boolean) value;
            if (BooleanUtils.isTrue(selected)) {
                modifier.select(row);
            } else {
                modifier.unselect(row);
            }
        } else {
            throw UnknownArgumentException.newInstance(Class.class, column.getClass());
        }
        if (eagerSubmitRunnable != null) {
            eagerSubmitRunnable.run();
        }
    }

    public List<?> getRows() {
        return rows;
    }

}
