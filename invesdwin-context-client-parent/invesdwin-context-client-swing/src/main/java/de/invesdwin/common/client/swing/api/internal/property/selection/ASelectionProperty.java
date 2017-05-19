package de.invesdwin.common.client.swing.api.internal.property.selection;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.jdesktop.beansbinding.Property;
import org.jdesktop.beansbinding.PropertyHelper;

import de.invesdwin.common.client.swing.api.AModel;
import de.invesdwin.util.assertions.Assertions;

@SuppressWarnings({ "unchecked", "rawtypes" })
@NotThreadSafe
public abstract class ASelectionProperty<S, V> extends PropertyHelper<S, V> {

    private final AModel model;
    private final Property<AModel, List> listProperty;

    public ASelectionProperty(final AModel model, final Property listProperty) {
        this.model = model;
        this.listProperty = listProperty;
    }

    protected List<Object> getValuesForIndices(final int[] indices) {
        final List<Object> list = listProperty.getValue(model);
        final List<Object> values = new ArrayList<Object>();
        for (int i = 0; i < indices.length; i++) {
            Assertions.assertThat(indices[i]).isGreaterThanOrEqualTo(0);
            final Object value = list.get(indices[i]);
            values.add(value);
        }
        return values;
    }

    protected int[] getIndicesForValues(final List<Object> values) {
        final List<Object> list = listProperty.getValue(model);
        final int[] indices = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            final int index = list.indexOf(values.get(i));
            Assertions.assertThat(index).isGreaterThanOrEqualTo(0);
            indices[i] = index;
        }
        return indices;
    }

    protected Object getValueForIndex(final int index) {
        if (index == -1) {
            return null;
        } else {
            final List<Object> list = listProperty.getValue(model);
            return list.get(index);
        }
    }

    protected int getIndexForValue(final Object value) {
        if (value == null) {
            return -1;
        } else {
            final List<Object> list = listProperty.getValue(model);
            final int index = list.indexOf(value);
            Assertions.assertThat(index).isGreaterThanOrEqualTo(0);
            return index;
        }
    }

    @Override
    public final boolean isReadable(final S source) {
        return true;
    }

    @Override
    public final boolean isWriteable(final S source) {
        return true;
    }

}
