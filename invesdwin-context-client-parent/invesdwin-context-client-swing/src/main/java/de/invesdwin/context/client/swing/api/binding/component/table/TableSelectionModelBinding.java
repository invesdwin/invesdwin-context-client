package de.invesdwin.context.client.swing.api.binding.component.table;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.invesdwin.util.collections.sorted.SortedList;
import de.invesdwin.util.math.Integers;

@NotThreadSafe
public class TableSelectionModelBinding implements ListSelectionModel {

    private final Set<ListSelectionListener> listeners = new HashSet<>();
    private int selectionMode = SINGLE_SELECTION;
    private boolean valueIsAdjusting = false;
    private boolean valueIsFrozen = false;;
    private final List<Integer> selectedIndexes = new SortedList<>(Integers.COMPARATOR);
    private int anchorSelectionIndex;
    private int leadSelectionIndex;

    public TableSelectionModelBinding() {}

    @Override
    public void setSelectionInterval(final int index0, final int index1) {
        if (valueIsFrozen) {
            return;
        }
        if (selectionMode != SINGLE_SELECTION) {
            selectedIndexes.clear();
        }
        internalAdd(index0, index1);
        fireValueChanged();
    }

    protected void fireValueChanged() {
        if (!listeners.isEmpty()) {
            final ListSelectionEvent e = new ListSelectionEvent(this, 0, Integer.MAX_VALUE, getValueIsAdjusting());
            for (final ListSelectionListener listener : listeners) {
                listener.valueChanged(e);
            }
        }
    }

    @Override
    public void addSelectionInterval(final int index0, final int index1) {
        if (valueIsFrozen) {
            return;
        }
        internalAdd(index0, index1);
        fireValueChanged();
    }

    private void internalAdd(final int index0, final int index1) {
        if (selectionMode == SINGLE_SELECTION) {
            selectedIndexes.clear();
            selectedIndexes.add(index1);
        } else {
            for (int i = index0; i <= index1; i++) {
                selectedIndexes.add(i);
            }
        }
    }

    @Override
    public void removeSelectionInterval(final int index0, final int index1) {
        if (valueIsFrozen) {
            return;
        }
        for (int i = index0; i <= index1; i++) {
            selectedIndexes.remove(selectedIndexes.indexOf(i));
        }
        fireValueChanged();
    }

    @Override
    public int getMinSelectionIndex() {
        if (selectedIndexes.isEmpty()) {
            return -1;
        }
        return selectedIndexes.get(0);
    }

    @Override
    public int getMaxSelectionIndex() {
        if (selectedIndexes.isEmpty()) {
            return -1;
        }
        return selectedIndexes.get(selectedIndexes.size() - 1);
    }

    @Override
    public boolean isSelectedIndex(final int index) {
        return selectedIndexes.contains(index);
    }

    @Override
    public int getAnchorSelectionIndex() {
        return anchorSelectionIndex;
    }

    @Override
    public void setAnchorSelectionIndex(final int index) {
        System.out.println("TODO: fix keyboard selection!");
        this.anchorSelectionIndex = index;
    }

    @Override
    public int getLeadSelectionIndex() {
        return leadSelectionIndex;
    }

    @Override
    public void setLeadSelectionIndex(final int index) {
        this.leadSelectionIndex = index;
    }

    @Override
    public void clearSelection() {
        if (valueIsFrozen) {
            return;
        }
        selectedIndexes.clear();
        fireValueChanged();
    }

    @Override
    public boolean isSelectionEmpty() {
        return selectedIndexes.isEmpty();
    }

    @Override
    public void insertIndexInterval(final int index, final int length, final boolean before) {
        //noop
    }

    @Override
    public void removeIndexInterval(final int index0, final int index1) {
        //noop
    }

    @Override
    public void setValueIsAdjusting(final boolean valueIsAdjusting) {
        final boolean prevValueIsAdjusting = this.valueIsAdjusting;
        this.valueIsAdjusting = true;
        if (prevValueIsAdjusting != valueIsAdjusting) {
            fireValueChanged();
        }
    }

    @Override
    public boolean getValueIsAdjusting() {
        return valueIsAdjusting;
    }

    @Override
    public void setSelectionMode(final int selectionMode) {
        this.selectionMode = selectionMode;
    }

    @Override
    public int getSelectionMode() {
        return selectionMode;
    }

    @Override
    public void addListSelectionListener(final ListSelectionListener x) {
        listeners.add(x);
    }

    @Override
    public void removeListSelectionListener(final ListSelectionListener x) {
        listeners.remove(x);
    }

    public List<Integer> getSelectedIndexes() {
        return selectedIndexes;
    }

    public void setValueIsFrozen(final boolean valueIsFrozen) {
        this.valueIsFrozen = valueIsFrozen;
    }

    public boolean isValueIsFrozen() {
        return valueIsFrozen;
    }

}
