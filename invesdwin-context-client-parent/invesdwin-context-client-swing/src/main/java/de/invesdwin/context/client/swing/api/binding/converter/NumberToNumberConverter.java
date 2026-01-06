package de.invesdwin.context.client.swing.api.binding.converter;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.norva.beanpath.spi.element.IPropertyBeanPathElement;
import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.math.Bytes;
import de.invesdwin.util.math.Integers;
import de.invesdwin.util.math.Longs;
import de.invesdwin.util.math.Shorts;
import de.invesdwin.util.math.decimal.Decimal;

@Immutable
public class NumberToNumberConverter implements IConverter<Object, Number> {

    private final Class<?> type;

    public NumberToNumberConverter(final IPropertyBeanPathElement element) {
        this.type = element.getModifier().getBeanClassAccessor().getType().getType();
    }

    @Override
    public Number fromModelToComponent(final Object value) {
        if (value == null) {
            return null;
        }
        final Number number = (Number) value;
        return number;
    }

    @Override
    public Object fromComponentToModel(final Number value) {
        if (value == null) {
            return convertNull(type);
        }
        final Number number = value;
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
    }

    public static Object convertNull(final Class<?> type) {
        if (type.isPrimitive()) {
            if (type == byte.class || type == Byte.class) {
                return Bytes.DEFAULT_MISSING_VALUE;
            } else if (type == short.class || type == Short.class) {
                return Shorts.DEFAULT_MISSING_VALUE;
            } else if (type == int.class || type == Integer.class) {
                return Integers.DEFAULT_MISSING_VALUE;
            } else if (type == long.class || type == Long.class) {
                return Longs.DEFAULT_MISSING_VALUE;
            } else if (type == float.class || type == Float.class) {
                return Float.NaN;
            } else if (type == double.class || type == Double.class) {
                return Double.NaN;
            } else {
                throw UnknownArgumentException.newInstance(Class.class, type);
            }
        } else {
            return null;
        }
    }

}
