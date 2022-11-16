package de.invesdwin.context.client.swing.api.binding.component;

import java.util.Optional;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassType;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.swing.Components;
import de.invesdwin.util.swing.text.Hotkey;
import de.invesdwin.util.swing.text.KeyGrabberTextField;

@NotThreadSafe
public class KeyGrabberTextFieldBinding extends AComponentBinding<KeyGrabberTextField, Hotkey> {

    private Optional<Hotkey> prevComponentValue;
    private boolean isSettingText = false;

    public KeyGrabberTextFieldBinding(final KeyGrabberTextField component, final APropertyBeanPathElement element,
            final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        final BeanClassType type = element.getModifier().getBeanClassAccessor().getType();

        if (type.getType() != Hotkey.class) {
            throw new IllegalArgumentException(
                    element.getBeanPath() + " should be of type " + Hotkey.class.getSimpleName());
        }
        if (eagerSubmitRunnable != null) {
            component.addPropertyChangeListener(KeyGrabberTextField.PROP_HOTKEY, evt -> {
                if (!isSettingText) {
                    eagerSubmitRunnable.run();
                }
            });
        }
    }

    @Override
    protected boolean isModifiable() {
        return super.isModifiable() && component.isHotkeyEditable();
    }

    @Override
    protected Optional<Hotkey> fromModelToComponent(final Hotkey modelValue) {
        if (prevComponentValue == null || !Objects.equals(modelValue, prevComponentValue.orElse(null))) {
            isSettingText = true;
            try {
                Components.setHotkey(component, modelValue); //need to double check edit because undo/redo might have modified this
            } finally {
                isSettingText = false;
            }
            prevComponentValue = Optional.ofNullable(modelValue);
            return Optional.ofNullable(modelValue);
        } else {
            return prevModelValue;
        }
    }

    @Override
    protected Hotkey fromComponentToModel() {
        return component.getHotkey();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected IBeanPathPropertyModifier<Hotkey> getModifier() {
        return (IBeanPathPropertyModifier) element.getModifier();
    }

    @Override
    protected void setEnabled(final boolean enabled) {
        component.setHotkeyEditable(enabled);
    }

    @Override
    protected void resetCaches() {
        prevComponentValue = null;
    }
}
