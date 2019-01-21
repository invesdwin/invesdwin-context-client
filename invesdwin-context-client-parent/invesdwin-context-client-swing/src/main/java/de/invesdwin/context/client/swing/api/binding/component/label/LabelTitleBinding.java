package de.invesdwin.context.client.swing.api.binding.component.label;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JLabel;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;

@NotThreadSafe
public class LabelTitleBinding extends LabelBinding {

    private final de.invesdwin.norva.beanpath.spi.element.simple.modifier.internal.FixedValueBeanPathModifier<Object> modifier;

    public LabelTitleBinding(final JLabel component, final APropertyBeanPathElement element,
            final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        this.modifier = new de.invesdwin.norva.beanpath.spi.element.simple.modifier.internal.FixedValueBeanPathModifier<>(
                element.getAccessor(), null);
    }

    @Override
    protected void fromModelToComponent(final Object modelValue) {
        component.setText(getTitle());
    }

    @Override
    protected IBeanPathPropertyModifier<Object> getModifier() {
        return modifier;
    }

}
