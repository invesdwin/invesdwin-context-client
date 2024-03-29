package de.invesdwin.context.client.swing.api.binding.component.label;

import java.util.Optional;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JLabel;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.component.AComponentBinding;
import de.invesdwin.context.client.swing.api.binding.converter.IConverter;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.swing.Components;

@NotThreadSafe
public class LabelBinding extends AComponentBinding<JLabel, Object> {

    private final IConverter<Object, String> converter;
    private Optional<String> prevComponentValue;

    public LabelBinding(final JLabel component, final APropertyBeanPathElement element,
            final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        this.converter = newConverter();
    }

    protected IConverter<Object, String> newConverter() {
        return IConverter.newConverter(element);
    }

    @Override
    protected Optional<Object> fromModelToComponent(final Object modelValue) {
        final String convertedValue = converter.fromModelToComponent(modelValue);
        final String newComponentValue = bindingGroup.i18n(convertedValue);
        if (prevComponentValue == null || !Objects.equals(newComponentValue, prevComponentValue.orElse(null))) {
            Components.setText(component, newComponentValue);
            prevComponentValue = Optional.ofNullable(newComponentValue);
            return Optional.ofNullable(modelValue);
        } else {
            return prevModelValue;
        }
    }

    @Override
    protected Object fromComponentToModel() {
        throw new UnsupportedOperationException("not modifiable");
    }

    @Override
    protected IBeanPathPropertyModifier<Object> getModifier() {
        return element.getModifier();
    }

    @Override
    protected boolean isModifiable() {
        return false;
    }

    @Override
    protected void resetCaches() {
        prevComponentValue = null;
    }

}
