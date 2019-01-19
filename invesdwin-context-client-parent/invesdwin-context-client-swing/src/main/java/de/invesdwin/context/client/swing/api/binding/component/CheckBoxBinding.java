package de.invesdwin.context.client.swing.api.binding.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.annotation.concurrent.Immutable;
import javax.swing.JCheckBox;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;

@Immutable
public class CheckBoxBinding extends AComponentBinding<JCheckBox, Boolean> {

    public CheckBoxBinding(final JCheckBox component, final APropertyBeanPathElement element,
            final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        if (eagerSubmitRunnable != null) {
            component.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    eagerSubmitRunnable.run();
                }
            });
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected IBeanPathPropertyModifier<Boolean> getModifier() {
        return (IBeanPathPropertyModifier) element.getModifier();
    }

    @Override
    protected void fromModelToComponent(final Boolean modelValue) {
        component.setSelected(modelValue);
    }

    @Override
    protected Boolean fromComponentToModel() {
        return component.isSelected();
    }

}
