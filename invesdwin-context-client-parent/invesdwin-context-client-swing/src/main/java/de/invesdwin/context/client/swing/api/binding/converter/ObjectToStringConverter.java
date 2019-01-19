package de.invesdwin.context.client.swing.api.binding.converter;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.util.lang.Strings;

@Immutable
public final class ObjectToStringConverter implements IConverter<Object, String> {

    private static final ObjectToStringConverter INSTANCE = new ObjectToStringConverter();

    @Override
    public String fromModelToComponent(final Object value) {
        return Strings.asString(value);
    }

    @Override
    public Object fromComponentToModel(final String value) {
        return value;
    }

    @SuppressWarnings("unchecked")
    public static <T> IConverter<T, String> getInstance() {
        return (IConverter<T, String>) INSTANCE;
    }

}
