package de.invesdwin.context.client.swing.api.binding.component;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.util.Optional;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.converter.DateToStringConverter;
import de.invesdwin.context.client.swing.api.binding.converter.IConverter;
import de.invesdwin.context.client.swing.api.binding.converter.NoConverter;
import de.invesdwin.context.client.swing.api.binding.converter.NumberToStringConverter;
import de.invesdwin.context.client.swing.api.binding.converter.ObjectToStringConverter;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassType;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.swing.Components;
import de.invesdwin.util.swing.listener.DocumentListenerSupport;
import de.invesdwin.util.swing.listener.FocusListenerSupport;

@NotThreadSafe
public class TextComponentBinding extends AComponentBinding<JTextComponent, Object> {

    private final IConverter<Object, String> converter;
    private Optional<String> prevComponentValue;
    private final Color originalBackground;
    private boolean isFocusOwner = false;
    private boolean isSettingText = false;

    public TextComponentBinding(final JTextComponent component, final APropertyBeanPathElement element,
            final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        this.converter = newConverter();
        if (eagerSubmitRunnable != null) {
            component.addFocusListener(new FocusListenerSupport() {
                @Override
                public void focusGained(final FocusEvent e) {
                    isFocusOwner = true;
                }

                @Override
                public void focusLost(final FocusEvent e) {
                    try {
                        eagerSubmitRunnable.run();
                    } finally {
                        isFocusOwner = false;
                    }
                }
            });
            component.getDocument().addDocumentListener(new DocumentListenerSupport() {
                @Override
                protected void update(final DocumentEvent e) {
                    if (!isFocusOwner && !isSettingText) {
                        eagerSubmitRunnable.run();
                    }
                }
            });
        }
        this.originalBackground = component.getBackground();

    }

    @Override
    protected boolean isModifiable() {
        return super.isModifiable() && component.isEditable();
    }

    protected IConverter<Object, String> newConverter() {
        final BeanClassType type = element.getModifier().getBeanClassAccessor().getType();
        if (type.isNumber()) {
            return new NumberToStringConverter(element);
        } else if (type.isDate()) {
            return new DateToStringConverter(element);
        } else if (type.getType() == String.class) {
            return NoConverter.getInstance();
        } else {
            return ObjectToStringConverter.getInstance();
        }
    }

    @Override
    protected Optional<Object> fromModelToComponent(final Object modelValue) {
        final String newComponentValue = converter.fromModelToComponent(modelValue);
        if (prevComponentValue == null || !Objects.equals(newComponentValue, prevComponentValue.orElse(null))) {
            isSettingText = true;
            try {
                Components.setText(component, newComponentValue); //need to double check edit because undo/redo might have modified this
            } finally {
                isSettingText = false;
            }
            prevComponentValue = Optional.ofNullable(newComponentValue);
            return Optional.ofNullable(modelValue);
        } else {
            return prevModelValue;
        }
    }

    @Override
    protected Object fromComponentToModel() {
        final String componentValue = component.getText();
        final Object newModelValue = converter.fromComponentToModel(componentValue);
        return newModelValue;
    }

    @Override
    protected IBeanPathPropertyModifier<Object> getModifier() {
        return element.getModifier();
    }

    @Override
    protected void setEnabled(final boolean enabled) {
        Components.setEditable(component, enabled);
        if (enabled) {
            if (originalBackground == null) {
                Components.setBackground(component, UIManager.getColor("TextField.background"));
            } else {
                Components.setBackground(component, originalBackground);
            }
        } else {
            Components.setBackground(component, UIManager.getColor("TextField.inactiveBackground"));
        }
    }

    @Override
    public void reset() {
        super.reset();
        prevComponentValue = null;
    }
}
