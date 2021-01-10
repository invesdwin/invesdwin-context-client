package de.invesdwin.context.client.swing.api.binding.component.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.component.AComponentBinding;
import de.invesdwin.norva.beanpath.spi.element.ATableBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;

@NotThreadSafe
public class TableSelectionBinding extends AComponentBinding<JTable, List<?>> {

    private final ATableBeanPathElement element;
    private final GeneratedTableModel tableModel;
    private final GeneratedTableSelectionModel selectionModel;
    private List<Integer> prevSelectedIndexesInModel = Collections.emptyList();
    private boolean selectionUpdating = false;

    public TableSelectionBinding(final JTable component, final ATableBeanPathElement element,
            final BindingGroup bindingGroup, final GeneratedTableModel tableModel,
            final GeneratedTableSelectionModel selectionModel) {
        super(component, element, bindingGroup);
        this.tableModel = tableModel;
        this.selectionModel = selectionModel;
        this.element = element;
        if (eagerSubmitRunnable != null) {
            selectionModel.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(final ListSelectionEvent e) {
                    if (e.getValueIsAdjusting()) {
                        return;
                    }
                    if (selectionUpdating) {
                        return;
                    }
                    if (eagerSubmitRunnable != null) {
                        eagerSubmitRunnable.run();
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
        final List<Integer> selectedIndexesInModel = getSelectedIndexesInModel();
        if (selectedIndexesInModel.equals(prevSelectedIndexesInModel)) {
            Optional.ofNullable(modelValue);
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

}
