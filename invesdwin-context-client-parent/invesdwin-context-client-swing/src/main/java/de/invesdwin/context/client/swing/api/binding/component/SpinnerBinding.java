package de.invesdwin.context.client.swing.api.binding.component;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.converter.IConverter;
import de.invesdwin.context.client.swing.api.binding.converter.NumberToNumberConverter;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;

@NotThreadSafe
public class SpinnerBinding extends AComponentBinding<JSpinner, Object> {

    private final IConverter<Object, Number> converter;

    public SpinnerBinding(final JSpinner component, final APropertyBeanPathElement element,
            final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        this.converter = newConverter();
        if (eagerSubmitRunnable != null) {
            component.getModel().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(final ChangeEvent e) {
                    eagerSubmitRunnable.run();
                }
            });
        }
    }

    protected IConverter<Object, Number> newConverter() {
        return new NumberToNumberConverter(element);
    }

    @Override
    protected void fromModelToComponent(final Object modelValue) {
        final Number newComponentValue = converter.fromModelToComponent(modelValue);
        component.setValue(newComponentValue);
    }

    @Override
    protected Object fromComponentToModel() {
        final Number componentValue = (Number) component.getValue();
        final Object newModelValue = converter.fromComponentToModel(componentValue);
        return newModelValue;
    }

    @Override
    protected IBeanPathPropertyModifier<Object> getModifier() {
        return element.getModifier();
    }

}
