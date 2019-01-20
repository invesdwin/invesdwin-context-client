package de.invesdwin.context.client.swing.api.binding.component;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.norva.beanpath.spi.element.AChoiceBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.util.lang.Objects;

@NotThreadSafe
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ListBinding extends AComponentBinding<JList, List<?>> {

    private final AChoiceBeanPathElement element;
    private List<Object> prevChoices = new ArrayList<>();

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
    }

    @Override
    protected void fromModelToComponent(final List<?> modelValue) {
        final List<?> choices = element.getChoiceModifier().getValueFromRoot(bindingGroup.getModel());
        final int[] indices = new int[modelValue.size()];
        for (int i = 0; i < indices.length; i++) {
            final int index = choices.indexOf(modelValue.get(i));
            indices[i] = index;
        }
        if (!Objects.equals(choices, prevChoices)) {
            final DefaultListModel<Object> model = new DefaultListModel<>();
            for (final Object choice : choices) {
                model.addElement(choice);
            }
            component.setModel(model);
            prevChoices = new ArrayList<>(choices);
        }
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
