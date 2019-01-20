package de.invesdwin.context.client.swing.api.binding.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JComboBox;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.norva.beanpath.spi.element.AChoiceBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.util.lang.Objects;

@NotThreadSafe
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ComboBoxBinding extends AComponentBinding<JComboBox, Object> {

    private final AChoiceBeanPathElement element;
    private List<Object> prevChoices = new ArrayList<>();

    public ComboBoxBinding(final JComboBox component, final AChoiceBeanPathElement element,
            final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        this.element = element;
        if (eagerSubmitRunnable != null) {
            component.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    eagerSubmitRunnable.run();
                }
            });
        }
    }

    @Override
    protected void fromModelToComponent(final Object modelValue) {
        final List<?> choices = element.getChoiceModifier().getValueFromRoot(bindingGroup.getModel());
        if (!Objects.equals(choices, prevChoices)) {
            component.removeAllItems();
            for (final Object choice : choices) {
                component.addItem(choice);
            }
            prevChoices = new ArrayList<>(choices);
        }
        component.setSelectedItem(modelValue);
    }

    @Override
    protected Object fromComponentToModel() {
        return component.getSelectedItem();
    }

    @Override
    protected IBeanPathPropertyModifier<Object> getModifier() {
        return element.getModifier();
    }

}
