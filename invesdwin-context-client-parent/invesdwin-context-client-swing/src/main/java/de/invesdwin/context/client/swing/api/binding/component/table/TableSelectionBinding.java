package de.invesdwin.context.client.swing.api.binding.component.table;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.component.AComponentBinding;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.norva.beanpath.spi.element.table.ATableBeanPathElement;
import de.invesdwin.util.collections.Collections;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.swing.listener.KeyListenerSupport;
import de.invesdwin.util.swing.listener.MouseListenerSupport;

@NotThreadSafe
public class TableSelectionBinding extends AComponentBinding<JTable, List<?>> {

    private final ATableBeanPathElement element;
    private final GeneratedTableModel tableModel;
    private final GeneratedTableSelectionModel selectionModel;
    private List<Integer> prevSelectedIndexesInModel = Collections.emptyList();
    private List<Integer> prevSelectedIndexesInTable = Collections.emptyList();
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
                    if (selectionUpdating) {
                        return;
                    }
                    if (eagerSubmitRunnable != null) {
                        final List<Integer> selectedIndexesInTableNew = getSelectedIndexesInTable();
                        if (!Objects.equals(prevSelectedIndexesInTable, selectedIndexesInTableNew)) {
                            eagerSubmitRunnable.run();
                            prevSelectedIndexesInTable = selectedIndexesInTableNew;
                            valueIsAdjusting = false;
                        }
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
        final List<Integer> selectedIndexesInModel = getSelectedIndexesInModel();
        if (Objects.equals(selectedIndexesInModel, prevSelectedIndexesInModel)) {
            return Optional.ofNullable(modelValue);
        }
        prevSelectedIndexesInModel = selectedIndexesInModel;
        selectionUpdating = true;
        selectionModel.setValueIsAdjusting(true);
        try {
            selectionModel.clearSelection();
            for (int i = 0; i < selectedIndexesInModel.size(); i++) {
                final int selectedIndexInModel = selectedIndexesInModel.get(i);
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
        final List<Integer> selectedIndexesInTable = getSelectedIndexesInTable();
        if (selectedIndexesInTable.isEmpty()) {
            return Collections.emptyList();
        } else {
            final List<Object> selectedValuesInTable = new ArrayList<>(selectedIndexesInTable.size());
            for (int i = 0; i < selectedIndexesInTable.size(); i++) {
                final int selectedIndexInView = selectedIndexesInTable.get(i);
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

    private List<Integer> getSelectedIndexesInModel() {
        final IBeanPathPropertyModifier<List<?>> selectionModifier = element.getSelectionModifier();
        final List<?> selectedValuesInModel = selectionModifier.getValueFromRoot(bindingGroup.getModel());
        final List<Integer> selectedIndexesInModel = new ArrayList<>(selectedValuesInModel.size());
        for (final Object selectedValueInModel : selectedValuesInModel) {
            if (selectedValueInModel != null) {
                final int indexOf = tableModel.getRows().indexOf(selectedValueInModel);
                if (indexOf >= 0) {
                    selectedIndexesInModel.add(indexOf);
                }
            }
        }
        return selectedIndexesInModel;
    }

    private List<Integer> getSelectedIndexesInTable() {
        final List<Integer> selectedIndexes = new ArrayList<>();
        if (selectionModel.getMinSelectionIndex() >= 0 && selectionModel.getMaxSelectionIndex() >= 0) {
            if (selectionModel.getSelectionMode() == ListSelectionModel.SINGLE_SELECTION) {
                selectedIndexes.add(selectionModel.getMinSelectionIndex());
            } else if (selectionModel.getSelectionMode() == ListSelectionModel.SINGLE_INTERVAL_SELECTION) {
                for (int i = selectionModel.getMinSelectionIndex(); i <= selectionModel.getMaxSelectionIndex(); i++) {
                    selectedIndexes.add(i);
                }
            } else {
                for (int i = 0; i < tableModel.getRows().size(); i++) {
                    if (selectionModel.isSelectedIndex(i)) {
                        selectedIndexes.add(i);
                    }
                }
            }
        }
        return selectedIndexes;
    }

    @Override
    protected void resetCaches() {
        prevSelectedIndexesInModel = Collections.emptyList();
        prevSelectedIndexesInTable = Collections.emptyList();
    }

}
