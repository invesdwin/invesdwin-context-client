package de.invesdwin.common.client.swing.api.internal.property.selection;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JList;

import org.jdesktop.beansbinding.Property;

import de.invesdwin.common.client.swing.api.AModel;

@SuppressWarnings("rawtypes")
@NotThreadSafe
public class JListMultipleSelectionProperty extends ASelectionProperty<JList, List> {

    public JListMultipleSelectionProperty(final AModel model, final Property listProperty) {
        super(model, listProperty);
    }

    @Override
    public Class<? extends List> getWriteType(final JList source) {
        return List.class;
    }

    @Override
    public List<Object> getValue(final JList source) {
        return getValuesForIndices(source.getSelectedIndices());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValue(final JList source, final List value) {
        source.setSelectedIndices(getIndicesForValues(value));
    }

}
