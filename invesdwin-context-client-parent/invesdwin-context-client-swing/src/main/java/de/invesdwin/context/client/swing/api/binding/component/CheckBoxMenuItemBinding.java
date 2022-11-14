package de.invesdwin.context.client.swing.api.binding.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;

import javax.annotation.concurrent.Immutable;
import javax.swing.JCheckBoxMenuItem;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.util.lang.Objects;

@Immutable
public class CheckBoxMenuItemBinding extends AComponentBinding<JCheckBoxMenuItem, Boolean> {

    public CheckBoxMenuItemBinding(final JCheckBoxMenuItem component, final APropertyBeanPathElement element,
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
    protected Optional<Boolean> fromModelToComponent(final Boolean modelValue) {
        if (prevModelValue == null || !Objects.equals(modelValue, prevModelValue.orElse(null))) {
            component.setSelected(modelValue);
            return Optional.ofNullable(modelValue);
        } else {
            return prevModelValue;
        }
    }

    @Override
    protected Boolean fromComponentToModel() {
        return component.isSelected();
    }

    @Override
    public void reset() {
        super.reset();
        prevModelValue = null;
    }
}
