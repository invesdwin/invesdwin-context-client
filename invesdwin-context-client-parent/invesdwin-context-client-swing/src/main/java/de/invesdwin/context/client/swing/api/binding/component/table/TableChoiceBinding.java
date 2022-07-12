package de.invesdwin.context.client.swing.api.binding.component.table;

import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.component.AComponentBinding;
import de.invesdwin.context.client.swing.api.view.AModel;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.norva.beanpath.spi.element.table.ATableBeanPathElement;
import de.invesdwin.util.swing.Components;
import de.invesdwin.util.swing.listener.MouseListenerSupport;

@NotThreadSafe
public class TableChoiceBinding extends AComponentBinding<JTable, List<?>> {

    private final ATableBeanPathElement element;
    private final GeneratedTableModel tableModel;
    private final GeneratedTableSelectionModel selectionModel;
    private final TableSelectionBinding selectionBinding;

    public TableChoiceBinding(final JTable component, final ATableBeanPathElement element,
            final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        this.element = element;
        this.selectionModel = new GeneratedTableSelectionModel();
        this.tableModel = new GeneratedTableModel(new Runnable() {
            @Override
            public void run() {
                if (selectionBinding != null) {
                    final Runnable runnable = selectionBinding.getEagerSubmitRunnable();
                    if (runnable != null) {
                        runnable.run();
                    }
                } else {
                    if (eagerSubmitRunnable != null) {
                        eagerSubmitRunnable.run();
                    }
                }
            }
        }, element, bindingGroup, selectionModel);
        component.setModel(tableModel);
        component.setSelectionModel(selectionModel);
        component.setAutoCreateColumnsFromModel(true);
        selectionBinding = configureSelectionMode(component);
        if (selectionBinding != null) {
            bindingGroup.addBinding(selectionBinding);
        }

        final TableCellRenderer defaultRenderer = component.getDefaultRenderer(Object.class);
        for (final Class<?> type : new Class<?>[] { Object.class, Number.class, String.class }) {
            component.setDefaultRenderer(type, new GeneratedTableCellRenderer(tableModel, defaultRenderer));
            component.setDefaultEditor(type, null);
        }
        component.setDefaultRenderer(Boolean.class,
                new GeneratedTableCellRenderer(tableModel, component.getDefaultRenderer(Boolean.class)));

        //support removing sorting via MIDDLE_CLICK
        component.getTableHeader().addMouseListener(new MouseListenerSupport() {
            @Override
            public void mouseReleased(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2) {
                    //https://stackoverflow.com/questions/10627927/how-to-un-sort-a-jtable
                    component.getRowSorter().setSortKeys(null);
                }
            }
        });
    }

    protected TableSelectionBinding configureSelectionMode(final JTable component) {
        component.setCellSelectionEnabled(false);
        component.setColumnSelectionAllowed(false);
        //        component.getTableHeader().setReorderingAllowed(false);
        component.setAutoCreateRowSorter(true);

        //model selection is handled via a special checkbox column
        if (element.getSelectionButtonColumn() != null) {
            if (element.getColumnsFromRoot(getBindingGroup().getModel()).contains(element.getSelectionButtonColumn())) {
                //selection via button column
                component.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                component.setRowSelectionAllowed(false);
                return null;
            } else {
                if (element.isMultiSelection()) {
                    //multi select
                    component.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                } else {
                    //single select
                    component.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                }
                component.setRowSelectionAllowed(true);
                return new TableSelectionBinding(component, element, bindingGroup, tableModel, selectionModel);
            }
        } else {
            //no selection
            component.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            component.setRowSelectionAllowed(false);
            return null;
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
    protected Optional<List<?>> fromModelToComponent(final List<?> modelValue) {
        tableModel.fromModelToComponent(modelValue);
        if (selectionBinding != null) {
            selectionBinding.update();
        }
        if (mouseEnteredListener.isMouseEntered()) {
            Components.updateToolTip(component);
        }
        return Optional.ofNullable(modelValue);
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
        return getModifier().getValueFromRoot(getTarget());
    }

}
