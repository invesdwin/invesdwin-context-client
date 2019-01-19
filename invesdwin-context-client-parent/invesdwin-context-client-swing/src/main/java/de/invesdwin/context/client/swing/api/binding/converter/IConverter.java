package de.invesdwin.context.client.swing.api.binding.converter;

public interface IConverter<M, C> {

    C fromModelToComponent(M value);

    M fromComponentToModel(C value);

}
