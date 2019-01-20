package de.invesdwin.context.client.swing.api.binding.component.label;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JLabel;

import com.jgoodies.common.base.Strings;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.component.AComponentBinding;
import de.invesdwin.context.client.swing.api.binding.converter.DateToStringConverter;
import de.invesdwin.context.client.swing.api.binding.converter.IConverter;
import de.invesdwin.context.client.swing.api.binding.converter.NoConverter;
import de.invesdwin.context.client.swing.api.binding.converter.NumberToStringConverter;
import de.invesdwin.context.client.swing.api.binding.converter.ObjectToStringConverter;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassType;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;

@NotThreadSafe
public class LabelBinding extends AComponentBinding<JLabel, Object> {

    private final IConverter<Object, String> converter;

    public LabelBinding(final JLabel component, final APropertyBeanPathElement element,
            final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        this.converter = newConverter();
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
        if (Strings.isNotBlank(newComponentValue)) {
            String i18n = bindingGroup.getModel().getResourceMap().getString(newComponentValue);
            if (i18n == null) {
                i18n = bindingGroup.getView().getResourceMap().getString(newComponentValue);
            }
            if (i18n == null) {
                i18n = newComponentValue;
            }
            component.setText(i18n);
        } else {
            component.setText(newComponentValue);
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

}
