package de.invesdwin.context.client.swing.api.binding.component.table;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
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
    private final TableSelectionModelBinding selectionModel;
    private List<Integer> selectedIndexesInModel;
    private boolean selectionUpdating = false;

    public TableModelBinding(final ATableBeanPathElement element, final BindingGroup bindingGroup,
            final TableSelectionModelBinding selectionModel) {
        this.element = element;
        this.columns = element.getColumns();
        this.bindingGroup = bindingGroup;
        this.selectionModel = selectionModel;
    }

    public void update(final List<?> newValues) {
        if (selectionUpdating) {
            return;
        }
        this.rows = new ArrayList<>(newValues);
        if (element.getSelectionModifier() != null) {
            final List<Integer> selectedIndexesInModel = getSelectedIndexesInModel(true);
            if (!Objects.equals(selectedIndexesInModel, selectionModel.getSelectedIndexes())) {
                selectionUpdating = true;
                selectionModel.setValueIsAdjusting(true);
                try {
                    selectionModel.clearSelection();
                    for (int i = 0; i < selectedIndexesInModel.size(); i++) {
                        final int selectedIndexInModel = selectedIndexesInModel.get(i);
                        selectionModel.addSelectionInterval(selectedIndexInModel, selectedIndexInModel);
                    }
                } finally {
                    selectionModel.setValueIsAdjusting(false);
                    selectionUpdating = false;
                }
            }
        }
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
    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (selectionUpdating) {
            return;
        }
        selectionUpdating = true;
        try {
            final List<Integer> selectedIndexesInTable = selectionModel.getSelectedIndexes();
            if (!Objects.equals(selectedIndexesInTable, getSelectedIndexesInModel(false))) {
                final List<Object> selectedValuesInTable = new ArrayList<>(selectedIndexesInTable.size());
                for (int i = 0; i < selectedIndexesInTable.size(); i++) {
                    selectedValuesInTable.add(rows.get(selectedIndexesInTable.get(i)));
                }
                element.getSelectionModifier().setValueFromRoot(bindingGroup.getModel(), selectedValuesInTable);
            }
        } finally {
            selectionUpdating = false;
        }
    }

    private List<Integer> getSelectedIndexesInModel(final boolean forceUpdate) {
        if (forceUpdate || selectedIndexesInModel == null) {
            selectedIndexesInModel = newSelectedIndexesInModel();
        }
        return selectedIndexesInModel;
    }

    private List<Integer> newSelectedIndexesInModel() {
        final IBeanPathPropertyModifier<List<?>> selectionModifier = element.getSelectionModifier();
        final List<?> selectedValuesInModel = selectionModifier.getValueFromRoot(bindingGroup.getModel());
        final List<Integer> selectedIndexesInModel = new ArrayList<>(selectedValuesInModel.size());
        for (final Object selectedValueInModel : selectedValuesInModel) {
            if (selectedValueInModel != null) {
                final int indexOf = rows.indexOf(selectedValueInModel);
                if (indexOf >= 0) {
                    selectedIndexesInModel.add(indexOf);
                }
            }
        }
        return selectedIndexesInModel;
    }

}
