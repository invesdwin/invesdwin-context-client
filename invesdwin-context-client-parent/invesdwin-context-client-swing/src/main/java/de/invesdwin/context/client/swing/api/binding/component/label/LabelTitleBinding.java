package de.invesdwin.context.client.swing.api.binding.component.label;

import java.util.Optional;
import java.util.concurrent.Callable;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JLabel;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.util.lang.Objects;

@NotThreadSafe
public class LabelTitleBinding extends LabelBinding {

    private final de.invesdwin.norva.beanpath.spi.element.simple.modifier.internal.CallableValueBeanPathModifier<Object> modifier;

    public LabelTitleBinding(final JLabel component, final APropertyBeanPathElement element,
            final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        this.modifier = new de.invesdwin.norva.beanpath.spi.element.simple.modifier.internal.CallableValueBeanPathModifier<Object>(
                element.getAccessor(), new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        return getTitle();
                    }
                });
    }

    @Override
    protected Optional<Object> fromModelToComponent(final Object modelValue) {
        final String title = (String) modelValue;
        if (!Objects.equals(title, component.getText())) {
            component.setText(title);
            return Optional.ofNullable(modelValue);
        } else {
            return prevModelValue;
        }
    }

    @Override
    protected IBeanPathPropertyModifier<Object> getModifier() {
        return modifier;
    }

}
