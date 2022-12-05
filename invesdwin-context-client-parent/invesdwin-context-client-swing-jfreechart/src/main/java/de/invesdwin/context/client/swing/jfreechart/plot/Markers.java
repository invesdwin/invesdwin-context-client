package de.invesdwin.context.client.swing.jfreechart.plot;

import java.lang.reflect.Field;

import javax.annotation.concurrent.Immutable;

import org.jfree.chart.plot.ValueMarker;

import de.invesdwin.util.lang.reflection.field.UnsafeField;

@Immutable
public final class Markers {
    private static final UnsafeField<Double> VALUEMARKER_VALUE_FIELD;

    private Markers() {
        super();
    }

    static {
        try {
            final Field valueMarkerValueField = ValueMarker.class.getDeclaredField("value");
            VALUEMARKER_VALUE_FIELD = new UnsafeField<>(valueMarkerValueField);
        } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setValue(final ValueMarker valueMarker, final double value) {
        VALUEMARKER_VALUE_FIELD.put(valueMarker, -1D);
    }
}
