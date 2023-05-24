package de.invesdwin.context.client.swing.api.binding.component;

import java.awt.event.FocusEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.converter.ConverterFormatter;
import de.invesdwin.context.client.swing.api.binding.converter.IConverter;
import de.invesdwin.norva.beanpath.spi.element.AChoiceBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.util.collections.Collections;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.swing.Components;
import de.invesdwin.util.swing.listener.DocumentListenerSupport;
import de.invesdwin.util.swing.listener.FocusListenerSupport;
import de.invesdwin.util.swing.spinner.JSpinnerComparableChoiceModel;
import de.invesdwin.util.swing.spinner.JSpinnerFormattedEditor;

@NotThreadSafe
public class SpinnerChoiceBinding extends AComponentBinding<JSpinner, Object> {

    private final AChoiceBeanPathElement element;
    private final JSpinnerComparableChoiceModel model;
    private Optional<Object> prevComponentValue;
    private List<Object> prevChoices = Collections.emptyList();
    private boolean isFocusOwner = false;
    private boolean isSettingText = false;
    private Optional<Object> pendingComponentValue;

    public SpinnerChoiceBinding(final JSpinner component, final AChoiceBeanPathElement element,
            final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        this.element = element;
        this.model = new JSpinnerComparableChoiceModel();
        component.setModel(model);
        final JSpinnerFormattedEditor editor = new JSpinnerFormattedEditor(component,
                new ConverterFormatter(newConverter()));
        component.setEditor(editor);

        if (eagerSubmitRunnable != null) {
            component.getModel().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(final ChangeEvent e) {
                    eagerSubmitRunnable.run();
                }
            });
            editor.getTextField().addFocusListener(new FocusListenerSupport() {
                @Override
                public void focusGained(final FocusEvent e) {
                    isFocusOwner = true;
                }

                @Override
                public void focusLost(final FocusEvent e) {
                    isFocusOwner = false;
                }
            });
            editor.getTextField().getDocument().addDocumentListener(new DocumentListenerSupport() {
                @Override
                protected void update(final DocumentEvent e) {
                    if (isFocusOwner && !isSettingText) {
                        //we have to circumvent internal sync of JSpinner or else we get exceptions based on updates during locks
                        try {
                            final JFormattedTextField textField = editor.getTextField();
                            final AbstractFormatter formatter = editor.getFormatter();
                            final Object value = formatter.stringToValue(textField.getText());
                            pendingComponentValue = Optional.ofNullable(value);
                            prevComponentValue = pendingComponentValue;
                            final boolean prevStateChangeEventFiring = model.setStateChangeEventFiring(true);
                            try {
                                eagerSubmitRunnable.run();
                            } finally {
                                model.setStateChangeEventFiring(prevStateChangeEventFiring);
                            }
                        } catch (final ParseException ex) {
                            //ignore
                        } finally {
                            pendingComponentValue = null;
                        }
                    }
                }
            });
        }
    }

    protected IConverter<Object, String> newConverter() {
        return IConverter.newConverter(element);
    }

    @Override
    protected void resetCaches() {
        prevComponentValue = null;
        prevChoices = Collections.emptyList();
    }

    @Override
    protected IBeanPathPropertyModifier<Object> getModifier() {
        return element.getModifier();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Optional<Object> fromModelToComponent(final Object modelValue) {
        final List<Comparable<Object>> choices = (List<Comparable<Object>>) element.getChoiceModifier()
                .getValueFromRoot(bindingGroup.getModel());
        if (!Objects.equals(choices, prevChoices)) {
            model.setChoice(choices);
            prevChoices = new ArrayList<>(choices);
        }
        if (pendingComponentValue != null) {
            /*
             * ignore during edit to prevent java.lang.IllegalStateException: Attempt to mutate in notification at
             * java.desktop/javax.swing.text.AbstractDocument.writeLock(AbstractDocument.java:1372)
             *
             * will be synced after focus lost, validation errors will be properly shown during edit though
             */
            return Optional.ofNullable(modelValue);
        }
        if (prevComponentValue == null || !Objects.equals(modelValue, prevComponentValue.orElse(null))) {
            isSettingText = true;
            try {
                Components.setValue(component, modelValue);
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
    protected Object fromComponentToModel() {
        final Object componentValue;
        if (pendingComponentValue != null) {
            componentValue = pendingComponentValue.orElse(null);
        } else {
            componentValue = component.getValue();
        }

        return componentValue;
    }
}
