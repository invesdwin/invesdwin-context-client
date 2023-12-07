package de.invesdwin.context.client.swing.api.binding.component.table;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.component.AComponentBinding;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.norva.beanpath.spi.element.table.ATableBeanPathElement;
import de.invesdwin.util.collections.Arrays;
import de.invesdwin.util.collections.Collections;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.math.Integers;
import de.invesdwin.util.swing.listener.KeyListenerSupport;
import de.invesdwin.util.swing.listener.MouseListenerSupport;

@NotThreadSafe
public class TableSelectionBinding extends AComponentBinding<JTable, List<?>> {

    private final ATableBeanPathElement element;
    private final GeneratedTableModel tableModel;
    private final GeneratedTableSelectionModel selectionModel;
    private int[] prevSelectedIndexesInModel = Integers.EMPTY_ARRAY;
    private int[] prevSelectedIndexesInTable = Integers.EMPTY_ARRAY;
    private boolean selectionUpdating = false;
    private boolean valueIsAdjusting = false;
    private boolean mouse1Down = false;
    private boolean shiftDown = false;

    public TableSelectionBinding(final JTable component, final ATableBeanPathElement element,
            final BindingGroup bindingGroup, final GeneratedTableModel tableModel,
            final GeneratedTableSelectionModel selectionModel) {
        super(component, element, bindingGroup);
        this.tableModel = tableModel;
        this.selectionModel = selectionModel;
        this.element = element;
        component.addMouseListener(new MouseListenerSupport() {
            @Override
            public void mousePressed(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    mouse1Down = true;
                }
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                if (mouse1Down && e.getButton() == MouseEvent.BUTTON1) {
                    mouse1Down = false;
                    //update selection
                    fromModelToComponent(null);
                }
            }

            @Override
            public void mouseClicked(final MouseEvent e) {
                final Point p = e.getPoint();
                final int rowIndex = component.rowAtPoint(p);
                if (rowIndex == -1) {
                    //delesect the current row-selection (if there were rows selected)
                    component.clearSelection();
                }
            }
        });
        component.addKeyListener(new KeyListenerSupport() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftDown = true;
                }
            }

            @Override
            public void keyReleased(final KeyEvent e) {
                if (shiftDown && e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftDown = false;
                    //update selection
                    fromModelToComponent(null);
                }
            }
        });
        if (eagerSubmitRunnable != null) {
            selectionModel.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(final ListSelectionEvent e) {
                    if (e.getValueIsAdjusting()) {
                        valueIsAdjusting = true;
                        return;
                    }
                    try {
                        if (selectionUpdating) {
                            return;
                        }
                        if (eagerSubmitRunnable != null) {
                            final int[] selectedIndexesInTableNew = getSelectedIndexesInTable();
                            if (!Objects.equals(prevSelectedIndexesInTable, selectedIndexesInTableNew)) {
                                eagerSubmitRunnable.run();
                                prevSelectedIndexesInTable = selectedIndexesInTableNew;
                                valueIsAdjusting = false;
                            }
                        }
                    } finally {
                        valueIsAdjusting = false;
                    }
                }

            });
        }
    }

    public Runnable getEagerSubmitRunnable() {
        return eagerSubmitRunnable;
    }

    @Override
    protected Optional<List<?>> fromModelToComponent(final List<?> modelValue) {
        if (valueIsAdjusting || mouse1Down || shiftDown) {
            //don't interfere with user actions, thus don't reset his modifications
            return Optional.ofNullable(modelValue);
        }
        final int[] selectedIndexesInModel = getSelectedIndexesInModel();
        if (Objects.equals(selectedIndexesInModel, prevSelectedIndexesInModel)) {
            return Optional.ofNullable(modelValue);
        }
        prevSelectedIndexesInModel = selectedIndexesInModel;
        selectionUpdating = true;
        selectionModel.setValueIsAdjusting(true);
        try {
            selectionModel.clearSelection();
            for (int i = 0; i < selectedIndexesInModel.length; i++) {
                final int selectedIndexInModel = selectedIndexesInModel[i];
                final int selectedIndexInView = component.convertRowIndexToView(selectedIndexInModel);
                selectionModel.addSelectionInterval(selectedIndexInView, selectedIndexInView);
            }
        } finally {
            selectionModel.setValueIsAdjusting(false);
            selectionUpdating = false;
        }
        return Optional.ofNullable(modelValue);
    }

    @Override
    protected List<?> fromComponentToModel() {
        final int[] selectedIndexesInTable = getSelectedIndexesInTable();
        if (selectedIndexesInTable.length == 0) {
            return Collections.emptyList();
        } else {
            final List<Object> selectedValuesInTable = new ArrayList<>(selectedIndexesInTable.length);
            for (int i = 0; i < selectedIndexesInTable.length; i++) {
                final int selectedIndexInView = selectedIndexesInTable[i];
                final int selectedIndexInModel = component.convertRowIndexToModel(selectedIndexInView);
                selectedValuesInTable.add(tableModel.getRows().get(selectedIndexInModel));
            }
            return selectedValuesInTable;
        }
    }

    @Override
    protected IBeanPathPropertyModifier<List<?>> getModifier() {
        return element.getSelectionModifier();
    }

    private int[] getSelectedIndexesInModel() {
        final IBeanPathPropertyModifier<List<?>> selectionModifier = element.getSelectionModifier();
        final List<?> selectedValuesInModel = selectionModifier.getValueFromRoot(bindingGroup.getModel());
        final int[] selectedIndexesInModel = new int[selectedValuesInModel.size()];
        List<?> rows = null;
        int n = 0;
        for (int i = 0; i < selectedValuesInModel.size(); i++) {
            final Object selectedValueInModel = selectedValuesInModel.get(i);
            if (selectedValueInModel != null) {
                if (rows == null) {
                    rows = tableModel.getRows();
                }
                final int indexOf = rows.indexOf(selectedValueInModel);
                if (indexOf >= 0) {
                    selectedIndexesInModel[n] = indexOf;
                    n++;
                }
            }
        }
        if (n < selectedIndexesInModel.length) {
            return Arrays.subarray(selectedIndexesInModel, 0, n);
        } else {
            return selectedIndexesInModel;
        }
    }

    private int[] getSelectedIndexesInTable() {
        return selectionModel.getSelectedIndices();
    }

    @Override
    protected void resetCaches() {
        prevSelectedIndexesInModel = Integers.EMPTY_ARRAY;
        prevSelectedIndexesInTable = Integers.EMPTY_ARRAY;
    }

}
