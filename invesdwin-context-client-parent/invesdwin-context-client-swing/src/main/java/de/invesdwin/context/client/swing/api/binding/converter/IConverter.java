package de.invesdwin.context.client.swing.api.binding.converter;

import java.text.ParseException;

import de.invesdwin.norva.beanpath.impl.clazz.BeanClassType;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;

public interface IConverter<M, C> {

    C fromModelToComponent(M value);

    M fromComponentToModel(C value) throws ParseException;

    static IConverter<Object, String> newConverter(final APropertyBeanPathElement element) {
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

}
