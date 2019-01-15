package de.invesdwin.context.client.swing.api.internal.property.converter;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.concurrent.Immutable;

import org.jdesktop.beansbinding.Converter;

import de.invesdwin.norva.beanpath.spi.element.IPropertyBeanPathElement;
import de.invesdwin.util.math.Booleans;
import de.invesdwin.util.math.Bytes;
import de.invesdwin.util.math.Characters;
import de.invesdwin.util.math.Doubles;
import de.invesdwin.util.math.Floats;
import de.invesdwin.util.math.Integers;
import de.invesdwin.util.math.Longs;
import de.invesdwin.util.math.Shorts;

@Immutable
public class ArrayToListConverter extends Converter<Object, Object> {

    private final Class<?> type;

    public ArrayToListConverter(final IPropertyBeanPathElement element) {
        this.type = element.getModifier().getBeanClassAccessor().getType().getType();
    }

    @Override
    public Object convertForward(final Object value) {
        if (type == boolean.class) {
            final boolean[] array = (boolean[]) value;
            return Booleans.asListVector(array);
        } else if (type == byte.class) {
            final byte[] array = (byte[]) value;
            return Bytes.asListVector(array);
        } else if (type == char.class) {
            final char[] array = (char[]) value;
            return Characters.asListVector(array);
        } else if (type == short.class) {
            final short[] array = (short[]) value;
            return Shorts.asListVector(array);
        } else if (type == int.class) {
            final int[] array = (int[]) value;
            return Integers.asListVector(array);
        } else if (type == long.class) {
            final long[] array = (long[]) value;
            return Longs.asListVector(array);
        } else if (type == float.class) {
            final float[] array = (float[]) value;
            return Floats.asListVector(array);
        } else if (type == double.class) {
            final double[] array = (double[]) value;
            return Doubles.asListVector(array);
        } else {
            final Object[] array = (Object[]) value;
            return Arrays.asList(array);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public Object convertReverse(final Object value) {
        if (type == boolean.class) {
            final Collection<Boolean> col = (Collection<Boolean>) value;
            return Booleans.toArrayVector(col);
        } else if (type == byte.class) {
            final Collection<Byte> col = (Collection<Byte>) value;
            return Bytes.toArrayVector(col);
        } else if (type == char.class) {
            final Collection<Character> col = (Collection<Character>) value;
            return Characters.toArrayVector(col);
        } else if (type == short.class) {
            final Collection<Short> col = (Collection<Short>) value;
            return Shorts.toArrayVector(col);
        } else if (type == int.class) {
            final Collection<Integer> col = (Collection<Integer>) value;
            return Integers.toArrayVector(col);
        } else if (type == long.class) {
            final Collection<Long> col = (Collection<Long>) value;
            return Longs.toArrayVector(col);
        } else if (type == float.class) {
            final Collection<Float> col = (Collection<Float>) value;
            return Floats.toArrayVector(col);
        } else if (type == double.class) {
            final Collection<Double> col = (Collection<Double>) value;
            return Doubles.toArrayVector(col);
        } else {
            final Collection<Object> col = (Collection<Object>) value;
            return Arrays.asList(col);
        }
    }

}
