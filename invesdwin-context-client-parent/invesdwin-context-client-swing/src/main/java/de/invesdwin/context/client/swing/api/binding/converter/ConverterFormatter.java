package de.invesdwin.context.client.swing.api.binding.converter;

import java.text.ParseException;

import javax.annotation.concurrent.Immutable;
import javax.swing.JFormattedTextField;

@Immutable
public class ConverterFormatter extends JFormattedTextField.AbstractFormatter {

    private final IConverter<Object, String> converter;
    private boolean installing;

    public ConverterFormatter(final IConverter<Object, String> converter) {
        this.converter = converter;
    }

    public boolean isInstalling() {
        return installing;
    }

    @Override
    public void install(final JFormattedTextField ftf) {
        installing = true;
        try {
            super.install(ftf);
        } finally {
            installing = false;
        }
    }

    @Override
    public Object stringToValue(final String text) throws ParseException {
        return converter.fromComponentToModel(text);
    }

    @Override
    public String valueToString(final Object value) throws ParseException {
        return converter.fromModelToComponent(value);
    }

}
