package de.invesdwin.context.client.swing.api.binding.internal.property.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.concurrent.Immutable;

import org.jdesktop.beansbinding.PropertyHelper;

import de.invesdwin.context.client.swing.api.AModel;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;

@Immutable
public class EnumValuesProperty extends PropertyHelper<AModel, List<Object>> {

    private final IBeanPathPropertyModifier<List<?>> modifier;

    public EnumValuesProperty(final IBeanPathPropertyModifier<List<?>> modifier) {
        this.modifier = modifier;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Class<? extends List<Object>> getWriteType(final AModel source) {
        return (Class) List.class;
    }

    @Override
    public List<Object> getValue(final AModel source) {
        final Object[] array = modifier.getBeanClassAccessor().getType().getType().getEnumConstants();
        final List<Object> list = new ArrayList<Object>();
        if (modifier.getAccessor().isNullable()) {
            list.add(null);
        }
        list.addAll(Arrays.asList(array));
        return list;
    }

    @Override
    public void setValue(final AModel source, final List<Object> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isReadable(final AModel source) {
        return true;
    }

    @Override
    public boolean isWriteable(final AModel source) {
        return false;
    }

}
