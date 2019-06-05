package de.invesdwin.context.client.swing.api.binding.component.table;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.SelectionBeanPathPropertyModifier;
import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.lang.Objects;

@NotThreadSafe
public class TableModelBinding extends AbstractTableModel implements ListSelectionListener {

    private final ATableBeanPathElement element;
    private List<ITableColumnBeanPathElement> columns;
    private List<?> rows = new ArrayList<>();
    private final BindingGroup bindingGroup;
    private ListSelectionModel selectionModel;

    public TableModelBinding(final ATableBeanPathElement element, final BindingGroup bindingGroup) {
        this.element = element;
        this.columns = element.getColumns();
        this.bindingGroup = bindingGroup;
    }

    public synchronized void update(final List<?> newValues) {
        this.rows = new ArrayList<>(newValues);
        fireTableDataChanged();
        if (selectionModel != null) {
            final IBeanPathPropertyModifier<List<?>> selectionModifier = element.getSelectionModifier();
            final List<?> selection = selectionModifier.getValueFromRoot(bindingGroup.getModel());
            selectionModel.clearSelection();
            for (final Object sel : selection) {
                final int indexOf = rows.indexOf(sel);
                if (indexOf > 0) {
                    selectionModel.addSelectionInterval(indexOf, indexOf);
                }
            }
        }
        final List<ITableColumnBeanPathElement> newColumns = element.getColumns();
        if (!Objects.equals(newColumns, columns)) {
            this.columns = new ArrayList<>(columns);
            fireTableStructureChanged();
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
    }

    @Override
    public synchronized void valueChanged(final ListSelectionEvent e) {
        final List<Object> selection = new ArrayList<>();
        if (selectionModel.getMinSelectionIndex() >= 0 && selectionModel.getMaxSelectionIndex() >= 0) {
            if (selectionModel.getSelectionMode() == ListSelectionModel.SINGLE_SELECTION) {
                selection.add(rows.get(selectionModel.getMinSelectionIndex()));
            } else if (selectionModel.getSelectionMode() == ListSelectionModel.SINGLE_INTERVAL_SELECTION) {
                selection.addAll(
                        rows.subList(selectionModel.getMinSelectionIndex(), selectionModel.getMaxSelectionIndex()));
            } else {
                for (int i = 0; i < rows.size(); i++) {
                    if (selectionModel.isSelectedIndex(i)) {
                        selection.add(rows.get(i));
                    }
                }
            }
        }
        element.getSelectionModifier().setValueFromRoot(bindingGroup.getModel(), selection);
    }

    public void enableSelectionListener(final ListSelectionModel selectionModel) {
        this.selectionModel = selectionModel;
        selectionModel.addListSelectionListener(this);
    }

}
