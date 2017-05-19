package de.invesdwin.context.client.swing.api.internal.property.selection;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JTable;

import org.jdesktop.beansbinding.Property;

import de.invesdwin.context.client.swing.api.AModel;

@SuppressWarnings("rawtypes")
@NotThreadSafe
public class JTableMultipleSelectionProperty extends ASelectionProperty<JTable, List> {

    public JTableMultipleSelectionProperty(final AModel model, final Property listProperty) {
        super(model, listProperty);
    }

    @Override
    public Class<? extends List> getWriteType(final JTable source) {
        return List.class;
    }

    @Override
    public List getValue(final JTable source) {
        return getValuesForIndices(source.getSelectedRows());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValue(final JTable source, final List value) {
        final int[] indices = getIndicesForValues(value);
        source.clearSelection();
        for (final int index : indices) {
            source.getSelectionModel().addSelectionInterval(index, index);
        }
    }
}
