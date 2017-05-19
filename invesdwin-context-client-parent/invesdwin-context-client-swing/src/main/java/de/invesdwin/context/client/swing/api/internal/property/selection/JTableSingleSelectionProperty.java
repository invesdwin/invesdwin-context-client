package de.invesdwin.context.client.swing.api.internal.property.selection;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JTable;

import org.jdesktop.beansbinding.Property;

import de.invesdwin.context.client.swing.api.AModel;

@NotThreadSafe
public class JTableSingleSelectionProperty extends ASelectionProperty<JTable, Object> {

    @SuppressWarnings("rawtypes")
    public JTableSingleSelectionProperty(final AModel model, final Property listProperty) {
        super(model, listProperty);
    }

    @Override
    public Class<? extends Object> getWriteType(final JTable source) {
        return Object.class;
    }

    @Override
    public Object getValue(final JTable source) {
        return getValueForIndex(source.getSelectedRow());
    }

    @Override
    public void setValue(final JTable source, final Object value) {
        final int index = getIndexForValue(value);
        source.clearSelection();
        source.getSelectionModel().setSelectionInterval(index, index);
    }
}
