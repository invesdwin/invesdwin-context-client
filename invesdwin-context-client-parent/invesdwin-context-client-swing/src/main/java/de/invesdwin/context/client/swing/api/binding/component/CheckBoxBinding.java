package de.invesdwin.context.client.swing.api.binding.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.annotation.concurrent.Immutable;
import javax.swing.JCheckBox;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;

@Immutable
public class CheckBoxBinding extends AComponentBinding<JCheckBox> {

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

    @Override
    protected void fromModelToComponent(final Object modelValue) {
        component.setSelected((boolean) modelValue);
    }

    @Override
    protected Object fromComponentToModel() {
        return component.isSelected();
    }

}
