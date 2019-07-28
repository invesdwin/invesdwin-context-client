package de.invesdwin.context.client.swing.util;

import javax.annotation.concurrent.Immutable;
import javax.swing.JComboBox;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

@Immutable
public final class JComboBoxes {

    private JComboBoxes() {}

    public static void enableAutoComplete(final JComboBox<?> combobox) {
        AutoCompleteDecorator.decorate(combobox);
    }

}
