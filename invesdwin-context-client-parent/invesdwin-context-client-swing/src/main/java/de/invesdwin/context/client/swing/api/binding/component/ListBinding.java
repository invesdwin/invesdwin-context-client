package de.invesdwin.context.client.swing.api.binding.component;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.norva.beanpath.spi.element.AChoiceBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;

@NotThreadSafe
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ListBinding extends AComponentBinding<JList, List<?>> {

    private final AChoiceBeanPathElement element;

    public ListBinding(final JList component, final AChoiceBeanPathElement element, final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        this.element = element;
        if (eagerSubmitRunnable != null) {
            component.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(final ListSelectionEvent e) {
                    eagerSubmitRunnable.run();
                }
            });
        }
        configureSelectionMode(component, element);
    }

    @Override
    protected boolean isModifiable() {
        return super.isModifiable() && !element.isChoiceOnly();
    }

    protected void configureSelectionMode(final JList component, final AChoiceBeanPathElement element) {
        if (element.isMultiSelection()) {
            component.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        } else {
            component.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
    }

    @Override
    protected void fromModelToComponent(final List<?> modelValue) {
        final List<?> choices = element.getChoiceModifier().getValueFromRoot(bindingGroup.getModel());
        final int[] indices = new int[modelValue.size()];
        for (int i = 0; i < indices.length; i++) {
            final int index = choices.indexOf(modelValue.get(i));
            indices[i] = index;
        }
        final DefaultListModel<Object> model = new DefaultListModel<>();
        for (final Object choice : choices) {
            model.addElement(choice);
        }
        component.setModel(model);
        component.setSelectedIndices(indices);
    }

    @Override
    protected List<?> fromComponentToModel() {
        return component.getSelectedValuesList();
    }

    @Override
    protected IBeanPathPropertyModifier<List<?>> getModifier() {
        return element.getSelectionModifier();
    }

}
