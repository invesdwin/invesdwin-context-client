package de.invesdwin.context.client.swing.api.binding.component;

import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JList;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.listener.FocusListenerSupport;
import de.invesdwin.norva.beanpath.spi.element.AChoiceBeanPathElement;
import de.invesdwin.util.lang.Objects;

@NotThreadSafe
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ListBinding extends AComponentBinding<JList> {

    private final AChoiceBeanPathElement element;
    private List<Object> prevChoices = new ArrayList<>();

    public ListBinding(final JList component, final AChoiceBeanPathElement element, final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        this.element = element;
        if (eagerSubmitRunnable != null) {
            component.addFocusListener(new FocusListenerSupport() {
                @Override
                public void focusLost(final FocusEvent e) {
                    eagerSubmitRunnable.run();
                }
            });
        }
    }

    @Override
    protected void fromModelToComponent(final Object modelValue) {
        component.setSelectedItem(modelValue);
        final List<?> choices = element.getChoiceModifier().getValueFromRoot(bindingGroup.getModel());
        if (!Objects.equals(choices, prevChoices)) {
            final Object selection = component.getSelectedItem();
            component.setModel(model);.removeAllItems();
            for (final Object choice : choices) {
                component.addItem(choice);
            }
            component.setSelectedItem(selection);
            prevChoices = new ArrayList<>(choices);
        }
    }

    @Override
    protected Object fromComponentToModel() {
        return component.getSelectedItem();
    }

}
