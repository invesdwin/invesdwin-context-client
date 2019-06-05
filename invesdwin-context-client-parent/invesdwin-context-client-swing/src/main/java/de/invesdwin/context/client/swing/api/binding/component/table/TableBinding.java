package de.invesdwin.context.client.swing.api.binding.component.table;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import de.invesdwin.context.client.swing.api.AModel;
import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.component.AComponentBinding;
import de.invesdwin.norva.beanpath.spi.element.ATableBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;

@NotThreadSafe
public class TableBinding extends AComponentBinding<JTable, List<?>> {

    private final ATableBeanPathElement element;
    private final TableModelBinding tableModel;
    private final TableSelectionModelBinding selectionModel;

    public TableBinding(final JTable component, final ATableBeanPathElement element, final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        this.element = element;
        this.selectionModel = new TableSelectionModelBinding();
        this.tableModel = new TableModelBinding(element, bindingGroup, selectionModel);
        component.setModel(tableModel);
        component.setSelectionModel(selectionModel);
        configureSelectionMode(component);
        component.setAutoCreateColumnsFromModel(true);
    }

    protected void configureSelectionMode(final JTable component) {
        component.setCellSelectionEnabled(false);
        component.setColumnSelectionAllowed(false);
        component.getTableHeader().setReorderingAllowed(false);
        component.setAutoCreateRowSorter(true);

        //model selection is handled via a special checkbox column
        if (element.getSelectionButtonColumn() != null) {
            if (element.getColumns().contains(element.getSelectionButtonColumn())) {
                //selection via button column
                component.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                component.setRowSelectionAllowed(false);
            } else {
                if (element.isMultiSelection()) {
                    //multi select
                    component.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                    component.setRowSelectionAllowed(true);
                    selectionModel.addListSelectionListener(tableModel);
                } else {
                    //single select
                    component.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    component.setRowSelectionAllowed(true);
                    selectionModel.addListSelectionListener(tableModel);
                }
            }
        } else {
            //no selection
            component.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            component.setRowSelectionAllowed(false);
        }
    }

    @Override
    protected boolean isModifiable() {
        return false;
    }

    @Override
    protected IBeanPathPropertyModifier<List<?>> getModifier() {
        return element.getChoiceModifier();
    }

    @Override
    protected void fromModelToComponent(final List<?> modelValue) {
        tableModel.update(modelValue);
    }

    @Override
    protected List<?> getValueFromRoot(final AModel model) {
        final List<?> modelValue = super.getValueFromRoot(model);
        //filter null invalid choices
        for (int i = 0; i < modelValue.size(); i++) {
            if (modelValue.get(i) == null) {
                modelValue.remove(i);
                i--;
            }
        }
        return modelValue;
    }

    @Override
    protected List<?> fromComponentToModel() {
        throw new UnsupportedOperationException("not modifiable");
    }

}
