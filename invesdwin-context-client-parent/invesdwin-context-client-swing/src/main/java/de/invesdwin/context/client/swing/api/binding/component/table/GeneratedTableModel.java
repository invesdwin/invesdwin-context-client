package de.invesdwin.context.client.swing.api.binding.component.table;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang3.BooleanUtils;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContainer;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassType;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.SelectionBeanPathPropertyModifier;
import de.invesdwin.norva.beanpath.spi.element.table.ATableBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.table.column.ITableColumnBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.table.column.TableButtonColumnBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.table.column.TableSelectionButtonColumnBeanPathElement;
import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.date.FTimeUnit;
import de.invesdwin.util.time.duration.Duration;

@NotThreadSafe
public class GeneratedTableModel extends AbstractTableModel {

    private static final Duration FULL_REDRAW_INTERVAL = new Duration(250, FTimeUnit.MILLISECONDS);
    private final Runnable eagerSubmitRunnable;
    private final ATableBeanPathElement element;
    private List<ITableColumnBeanPathElement> columns;
    private List<?> rows = new ArrayList<>();
    private final BindingGroup bindingGroup;
    private final GeneratedTableSelectionModel selectionModel;

    private Object[][] prevTableModel = new Object[0][];
    private Instant lastFullRedraw = Instant.DUMMY;

    public GeneratedTableModel(final Runnable eagerSubmitRunnable, final ATableBeanPathElement element,
            final BindingGroup bindingGroup, final GeneratedTableSelectionModel selectionModel) {
        this.eagerSubmitRunnable = eagerSubmitRunnable;
        this.element = element;
        this.columns = element.getColumnsFromRoot(bindingGroup.getModel());
        this.bindingGroup = bindingGroup;
        this.selectionModel = selectionModel;
    }

    public void fromModelToComponent(final List<?> newValues) {
        this.rows = new ArrayList<>(newValues);
        selectionModel.setValueIsFrozen(true);
        try {
            final List<ITableColumnBeanPathElement> newColumns = element.getColumnsFromRoot(bindingGroup.getModel());
            final int newRowCount = rows.size();
            final int newColumnCount = newColumns.size();
            if (!Objects.equals(newColumns, columns)) {
                this.columns = new ArrayList<>(newColumns);
                resetPrevTableModel(newRowCount, newColumnCount);
                fireTableStructureChanged();
            } else if (lastFullRedraw.isGreaterThan(FULL_REDRAW_INTERVAL)) {
                //renderers might want to update something
                resetPrevTableModel(newRowCount, newColumnCount);
                fireTableDataChanged();
            } else {
                try {
                    updatePrevTableModel(newRowCount, newColumnCount);
                } catch (final IndexOutOfBoundsException e) {
                    //    Caused by - java.lang.IndexOutOfBoundsException: Invalid index
                    //    at java.desktop/javax.swing.DefaultRowSorter.convertUnsortedUnfiltered(DefaultRowSorter.java:523)
                    //    at java.desktop/javax.swing.DefaultRowSorter.convertRowIndexToView(DefaultRowSorter.java:494)
                    //    at java.desktop/javax.swing.JTable.convertRowIndexToView(JTable.java:2607)
                    //    at java.desktop/javax.swing.JTable.repaintSortedRows(JTable.java:4180)
                    //    at java.desktop/javax.swing.JTable.sortedTableChanged(JTable.java:4121)
                    //    at java.desktop/javax.swing.JTable.tableChanged(JTable.java:4400)
                    //    at java.desktop/javax.swing.table.AbstractTableModel.fireTableChanged(AbstractTableModel.java:297)
                    //    at java.desktop/javax.swing.table.AbstractTableModel.fireTableCellUpdated(AbstractTableModel.java:276)
                    //  * at de.invesdwin.context.client.swing.api.binding.component.table.GeneratedTableModel.updatePrevTableModel(GeneratedTableModel.java:90) *
                    resetPrevTableModel(newRowCount, newColumnCount);
                    try {
                        fireTableDataChanged();
                    } catch (final NullPointerException t) {
                        //                        java.lang.NullPointerException: Cannot read field "modelIndex" because "viewToModel[i]" is null
                        //                              * at de.invesdwin.util.swing.table.ComparableDefaultRowSorter.getViewToModelAsInts(ComparableDefaultRowSorter.java:678) *
                        //                              * at de.invesdwin.util.swing.table.ComparableDefaultRowSorter.sort(ComparableDefaultRowSorter.java:517) *
                        //                              * at de.invesdwin.util.swing.table.ComparableDefaultRowSorter.allRowsChanged(ComparableDefaultRowSorter.java:792) *
                        //ignore
                    }
                }
            }
        } finally {
            selectionModel.setValueIsFrozen(false);
        }
    }

    private void updatePrevTableModel(final int newRowCount, final int newColumnCount) {
        final Object[][] oldTableModel = prevTableModel;
        final int prevRowCount = oldTableModel.length;
        if (prevRowCount < newRowCount) {
            addRowsToPrevTableModel(newRowCount, newColumnCount, prevRowCount);
            fireTableRowsInserted(prevRowCount, newRowCount - 1);
        } else if (prevRowCount > newRowCount) {
            removeRowsFromPrevTableModel(newRowCount);
            fireTableRowsDeleted(newRowCount, prevRowCount - 1);
        }
        //update existing columns a bit later to reduce flickering
        for (int r = 0; r < prevRowCount; r++) {
            final Object[] row = oldTableModel[r];
            for (int c = 0; c < newColumnCount; c++) {
                final Object newValue = getValueAt(r, c);
                final Object prevValue = row[c];
                if (!Objects.equals(newValue, prevValue)) {
                    row[c] = newValue;
                    fireTableCellUpdated(r, c);
                }
            }
        }
    }

    private void removeRowsFromPrevTableModel(final int newRowCount) {
        final Object[][] oldTableModel = prevTableModel;
        final Object[][] newTableModel = new Object[newRowCount][];
        for (int r = 0; r < newTableModel.length; r++) {
            newTableModel[r] = oldTableModel[r];
        }
        prevTableModel = newTableModel;
    }

    private void addRowsToPrevTableModel(final int newRowCount, final int newColumnCount, final int prevRowCount) {
        final Object[][] oldTableModel = prevTableModel;
        final Object[][] newTableModel = new Object[newRowCount][];
        for (int r = 0; r < oldTableModel.length; r++) {
            newTableModel[r] = oldTableModel[r];
        }
        for (int r = prevRowCount; r < newTableModel.length; r++) {
            final Object[] row = new Object[newColumnCount];
            for (int c = 0; c < newColumnCount; c++) {
                row[c] = getValueAt(r, c);
            }
            newTableModel[r] = row;
        }
        prevTableModel = newTableModel;
    }

    private void resetPrevTableModel(final int newRowCount, final int newColumnCount) {
        Object[][] newTableModel = prevTableModel;
        if (newTableModel.length != newRowCount) {
            newTableModel = new Object[newRowCount][];
        }
        for (int r = 0; r < newRowCount; r++) {
            final Object[] prevRow = newTableModel[r];
            final Object[] row;
            if (prevRow == null || prevRow.length != newColumnCount) {
                row = new Object[newColumnCount];
            } else {
                row = prevRow;
            }
            for (int c = 0; c < newColumnCount; c++) {
                row[c] = getValueAt(r, c);
            }
            if (prevRow != row) {
                newTableModel[r] = row;
            }
        }
        prevTableModel = newTableModel;
        lastFullRedraw = new Instant();
    }

    protected Object getTarget() {
        final BeanClassContainer container = element.getContainer().unwrap(BeanClassContainer.class);
        return container.getTargetFromRoot(bindingGroup.getModel());
    }

    @Override
    public String getColumnName(final int column) {
        return bindingGroup.getTitleFromTarget(columns.get(column), null);
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
        try {
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
        } catch (final IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        final ITableColumnBeanPathElement column = columns.get(columnIndex);
        if (column instanceof APropertyBeanPathElement) {
            final Object row = rows.get(rowIndex);
            return column.isEnabledFromTarget(bindingGroup.getModel(), row);
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

    public String getTooltipAt(final int rowIndex, final int columnIndex) {
        final ITableColumnBeanPathElement column = columns.get(columnIndex);
        final Object row = rows.get(rowIndex);
        return bindingGroup.i18n(column.getTooltipFromTarget(bindingGroup.getModel(), row));
    }

}
