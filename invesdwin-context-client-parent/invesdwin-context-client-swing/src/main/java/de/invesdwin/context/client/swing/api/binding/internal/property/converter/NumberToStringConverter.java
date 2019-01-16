package de.invesdwin.context.client.swing.api.binding.internal.property.converter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.annotation.concurrent.Immutable;

import org.jdesktop.beansbinding.Converter;

import de.invesdwin.norva.beanpath.spi.element.IPropertyBeanPathElement;
import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.math.decimal.Decimal;

@Immutable
public class NumberToStringConverter extends Converter<Object, Object> {

    private final NumberFormat format;
    private final Class<?> type;

    public NumberToStringConverter(final IPropertyBeanPathElement element) {
        final String formatStr = element.getFormatString();
        if (formatStr != null) {
            format = Decimal.newDecimalFormatInstance(formatStr);
        } else {
            format = Decimal.newDecimalFormatInstance(Decimal.DEFAULT_DECIMAL_FORMAT);
        }
        this.type = element.getModifier().getBeanClassAccessor().getType().getType();
    }

    @Override
    public Object convertForward(final Object value) {
        if (value == null) {
            return null;
        }
        final Number number = (Number) value;
        return format.format(number.doubleValue());
    }

    @Override
    public Object convertReverse(final Object value) {
        if (value == null) {
            return null;
        }
        final String str = (String) value;
        try {
            final Number number = format.parse(str);
            if (type == byte.class || type == Byte.class) {
                return number.byteValue();
            } else if (type == short.class || type == Short.class) {
                return number.shortValue();
            } else if (type == int.class || type == Integer.class) {
                return number.intValue();
            } else if (type == long.class || type == Long.class) {
                return number.longValue();
            } else if (type == float.class || type == Float.class) {
                return number.floatValue();
            } else if (type == double.class || type == Double.class) {
                return number.doubleValue();
            } else if (type == BigInteger.class) {
                return new BigInteger(number.toString());
            } else if (type == BigDecimal.class) {
                return new BigDecimal(number.doubleValue());
            } else if (type == Decimal.class) {
                return new Decimal(number.doubleValue());
            } else if (type == Number.class) {
                return number;
            } else {
                throw UnknownArgumentException.newInstance(Class.class, type);
            }
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
