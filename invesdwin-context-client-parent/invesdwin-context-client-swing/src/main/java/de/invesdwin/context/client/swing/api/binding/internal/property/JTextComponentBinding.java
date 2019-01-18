package de.invesdwin.context.client.swing.api.binding.internal.property;

import java.awt.event.FocusEvent;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.text.JTextComponent;

import de.invesdwin.context.client.swing.api.AModel;
import de.invesdwin.context.client.swing.api.binding.internal.converter.DateToStringConverter;
import de.invesdwin.context.client.swing.api.binding.internal.converter.IConverter;
import de.invesdwin.context.client.swing.api.binding.internal.converter.NoConverter;
import de.invesdwin.context.client.swing.api.binding.internal.converter.NumberToStringConverter;
import de.invesdwin.context.client.swing.api.binding.internal.converter.ObjectToStringConverter;
import de.invesdwin.context.client.swing.listener.FocusListenerSupport;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassType;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;

@NotThreadSafe
public class JTextComponentBinding extends AJComponentBinding<JTextComponent> {

    private final IConverter<Object, String> converter;

    public JTextComponentBinding(final JTextComponent component, final APropertyBeanPathElement element,
            final AModel model) {
        super(component, element, model);
        this.converter = newConverter();
        if (eagerHelper != null) {
            component.addFocusListener(new FocusListenerSupport() {
                @Override
                public void focusLost(final FocusEvent e) {
                    eagerHelper.process(component);
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

}
