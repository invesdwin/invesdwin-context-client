package de.invesdwin.context.client.swing.api.binding.component.table;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.DefaultListSelectionModel;

@NotThreadSafe
public class TableSelectionModelBinding extends DefaultListSelectionModel {

    private boolean valueIsFrozen = false;;

    public TableSelectionModelBinding() {}

    @Override
    public void setSelectionInterval(final int index0, final int index1) {
        if (valueIsFrozen) {
            return;
        }
        super.setSelectionInterval(index0, index1);
    }

    @Override
    public void addSelectionInterval(final int index0, final int index1) {
        if (valueIsFrozen) {
            return;
        }
        super.addSelectionInterval(index0, index1);
    }

    @Override
    public void removeSelectionInterval(final int index0, final int index1) {
        if (valueIsFrozen) {
            return;
        }
        super.removeSelectionInterval(index0, index1);
    }

    @Override
    public void setAnchorSelectionIndex(final int index) {
        if (valueIsFrozen) {
            return;
        }
        super.setAnchorSelectionIndex(index);
    }

    @Override
    public void setLeadSelectionIndex(final int index) {
        if (valueIsFrozen) {
            return;
        }
        super.setLeadSelectionIndex(index);
    }

    @Override
    public void clearSelection() {
        if (valueIsFrozen) {
            return;
        }
        super.clearSelection();
    }

    @Override
    public void insertIndexInterval(final int index, final int length, final boolean before) {
        if (valueIsFrozen) {
            return;
        }
        super.insertIndexInterval(index, length, before);
    }

    @Override
    public void removeIndexInterval(final int index0, final int index1) {
        if (valueIsFrozen) {
            return;
        }
        super.removeIndexInterval(index0, index1);
    }

    public void setValueIsFrozen(final boolean valueIsFrozen) {
        this.valueIsFrozen = valueIsFrozen;
    }

    public boolean isValueIsFrozen() {
        return valueIsFrozen;
    }

}
