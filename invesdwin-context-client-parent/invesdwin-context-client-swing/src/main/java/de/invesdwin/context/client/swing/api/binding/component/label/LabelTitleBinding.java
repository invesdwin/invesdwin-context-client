package de.invesdwin.context.client.swing.api.binding.component.label;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JLabel;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;

@NotThreadSafe
public class LabelTitleBinding extends LabelBinding {

    public LabelTitleBinding(final JLabel component, final APropertyBeanPathElement element,
            final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected IBeanPathPropertyModifier<Object> getModifier() {
        return (IBeanPathPropertyModifier) element.getTitleModifier();
    }

}
