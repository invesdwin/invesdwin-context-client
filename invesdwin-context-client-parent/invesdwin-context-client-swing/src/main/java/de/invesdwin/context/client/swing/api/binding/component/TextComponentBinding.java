package de.invesdwin.context.client.swing.api.binding.component;

import java.awt.event.FocusEvent;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.text.JTextComponent;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.converter.DateToStringConverter;
import de.invesdwin.context.client.swing.api.binding.converter.IConverter;
import de.invesdwin.context.client.swing.api.binding.converter.NoConverter;
import de.invesdwin.context.client.swing.api.binding.converter.NumberToStringConverter;
import de.invesdwin.context.client.swing.api.binding.converter.ObjectToStringConverter;
import de.invesdwin.context.client.swing.listener.FocusListenerSupport;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassType;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;

@NotThreadSafe
public class TextComponentBinding extends AComponentBinding<JTextComponent, Object> {

    private final IConverter<Object, String> converter;

    public TextComponentBinding(final JTextComponent component, final APropertyBeanPathElement element,
            final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        this.converter = newConverter();
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
    protected boolean isModifiable() {
        return super.isModifiable() && component.isEditable();
    }

    protected IConverter<Object, String> newConverter() {
        final BeanClassType type = element.getModifier().getBeanClassAccessor().getType();
        if (type.isNumber()) {
            return new NumberToStringConverter(element);
        } else if (type.isDate()) {
            return new DateToStringConverter(element);
        } else if (type.getType() == String.class) {
            return NoConverter.getInstance();
        } else {
            return ObjectToStringConverter.getInstance();
        }
    }

    @Override
    protected void fromModelToComponent(final Object modelValue) {
        final String newComponentValue = converter.fromModelToComponent(modelValue);
        component.setText(newComponentValue);
    }

    @Override
    protected Object fromComponentToModel() {
        final String componentValue = component.getText();
        final Object newModelValue = converter.fromComponentToModel(componentValue);
        return newModelValue;
    }

    @Override
    protected IBeanPathPropertyModifier<Object> getModifier() {
        return element.getModifier();
    }

}
