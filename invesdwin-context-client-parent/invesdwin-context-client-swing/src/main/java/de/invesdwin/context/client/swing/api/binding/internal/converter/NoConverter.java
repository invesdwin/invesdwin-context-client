package de.invesdwin.context.client.swing.api.binding.internal.converter;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class NoConverter implements IConverter<Object, Object> {

    private static final NoConverter INSTANCE = new NoConverter();

    private NoConverter() {}

    @Override
    public Object fromModelToComponent(final Object value) {
        return value;
    }

    @Override
    public Object fromComponentToModel(final Object value) {
        return value;
    }

    @SuppressWarnings("unchecked")
    public static <M, C> IConverter<M, C> getInstance() {
        return (IConverter<M, C>) INSTANCE;
    }

}
