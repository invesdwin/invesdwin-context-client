package de.invesdwin.context.client.swing.api.binding.component;

import java.awt.event.FocusEvent;
import java.util.Optional;
import java.util.concurrent.Future;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;

import org.hibernate.validator.constraints.Range;

import de.invesdwin.context.beans.validator.DecimalRange;
import de.invesdwin.context.beans.validator.doubl.DoubleMax;
import de.invesdwin.context.beans.validator.doubl.DoubleMin;
import de.invesdwin.context.beans.validator.doubl.DoubleRange;
import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.converter.IConverter;
import de.invesdwin.context.client.swing.api.binding.converter.NumberToNumberConverter;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.util.concurrent.future.Futures;
import de.invesdwin.util.concurrent.future.ImmutableFuture;
import de.invesdwin.util.concurrent.future.ThrowableFuture;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.lang.string.Strings;
import de.invesdwin.util.math.decimal.Decimal;
import de.invesdwin.util.swing.Components;
import de.invesdwin.util.swing.listener.DocumentListenerSupport;
import de.invesdwin.util.swing.listener.FocusListenerSupport;
import de.invesdwin.util.swing.spinner.SpinnerDecimalEditor;
import de.invesdwin.util.swing.spinner.SpinnerDecimalModel;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@NotThreadSafe
public class SpinnerBinding extends AComponentBinding<JSpinner, Object> {

    private final IConverter<Object, Number> converter;
    private final SpinnerDecimalEditor editor;
    private Future<Number> prevComponentValue;
    private boolean isFocusOwner = false;
    private boolean isSettingText = false;
    private Future<Number> pendingComponentValue;

    public SpinnerBinding(final JSpinner component, final APropertyBeanPathElement element,
            final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        final String format = getFormat(element);
        final SpinnerDecimalModel model = SpinnerDecimalModel
                .newModel(element.getAccessor().getType().isIntegralNumber());
        model.setMinimum(determineMinimum());
        model.setMaximum(determineMaximum());
        component.setModel(model);
        this.editor = new SpinnerDecimalEditor(component, format);
        editor.getTextField().setFocusLostBehavior(JFormattedTextField.COMMIT);
        component.setEditor(editor);
        component.addMouseWheelListener(new JSpinnerMouseWheelListener(component));
        this.converter = newConverter();
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
                            try {
                                final String text = textField.getText();
                                final Number formattedValue;
                                if (Strings.isBlank(text)) {
                                    //might happen during document replace (which happens during install) which is a remove and insert separately
                                    if (editor.getFormatter().isInstalling()) {
                                        return;
                                    }
                                    formattedValue = null;
                                } else {
                                    final AbstractFormatter formatter = textField.getFormatter();
                                    formattedValue = (Number) formatter.stringToValue(text);
                                }
                                pendingComponentValue = ImmutableFuture.of(formattedValue);
                                prevComponentValue = pendingComponentValue;
                            } catch (final Throwable t) {
                                setInvalidMessage(exceptionToString(t));
                                pendingComponentValue = ThrowableFuture.of(t);
                            }
                            final boolean prevStateChangeEventFiring = model.setStateChangeEventFiring(true);
                            try {
                                eagerSubmitRunnable.run();
                            } finally {
                                model.setStateChangeEventFiring(prevStateChangeEventFiring);
                            }
                        } finally {
                            pendingComponentValue = null;
                        }
                    }
                }
            });
        }
    }

    private Decimal determineMinimum() {
        final Min min = element.getAccessor().getAnnotation(Min.class);
        if (min != null) {
            return Decimal.valueOf(min.value());
        }
        final DecimalMin decimalMin = element.getAccessor().getAnnotation(DecimalMin.class);
        if (decimalMin != null) {
            return Decimal.valueOf(decimalMin.value());
        }
        final DoubleMin doubleMin = element.getAccessor().getAnnotation(DoubleMin.class);
        if (doubleMin != null) {
            return Decimal.valueOf(doubleMin.value());
        }
        final Range range = element.getAccessor().getAnnotation(Range.class);
        if (range != null) {
            return Decimal.valueOf(range.min());
        }
        final DecimalRange decimalRange = element.getAccessor().getAnnotation(DecimalRange.class);
        if (decimalRange != null) {
            return Decimal.valueOf(decimalRange.min());
        }
        final DoubleRange doubleRange = element.getAccessor().getAnnotation(DoubleRange.class);
        if (doubleRange != null) {
            return Decimal.valueOf(doubleRange.min());
        }
        return null;
    }

    private Decimal determineMaximum() {
        final Max max = element.getAccessor().getAnnotation(Max.class);
        if (max != null) {
            return Decimal.valueOf(max.value());
        }
        final DecimalMax decimalMax = element.getAccessor().getAnnotation(DecimalMax.class);
        if (decimalMax != null) {
            return Decimal.valueOf(decimalMax.value());
        }
        final DoubleMax doubleMax = element.getAccessor().getAnnotation(DoubleMax.class);
        if (doubleMax != null) {
            return Decimal.valueOf(doubleMax.value());
        }
        final Range range = element.getAccessor().getAnnotation(Range.class);
        if (range != null) {
            return Decimal.valueOf(range.max());
        }
        final DecimalRange decimalRange = element.getAccessor().getAnnotation(DecimalRange.class);
        if (decimalRange != null) {
            return Decimal.valueOf(decimalRange.max());
        }
        final DoubleRange doubleRange = element.getAccessor().getAnnotation(DoubleRange.class);
        if (doubleRange != null) {
            return Decimal.valueOf(doubleRange.max());
        }
        return null;
    }

    protected String getFormat(final APropertyBeanPathElement element) {
        final String format = element.getFormatString();
        if (Strings.isNotBlank(format)) {
            return format;
        }
        if (element.getAccessor().getType().isIntegralNumber()) {
            return SpinnerDecimalEditor.INTEGER_FORMAT;
        } else {
            return SpinnerDecimalEditor.DECIMAL_FORMAT;
        }
    }

    protected IConverter<Object, Number> newConverter() {
        return new NumberToNumberConverter(element);
    }

    @Override
    protected Optional<Object> fromModelToComponent(final Object modelValue) {
        if (pendingComponentValue != null) {
            /*
             * ignore during edit to prevent java.lang.IllegalStateException: Attempt to mutate in notification at
             * java.desktop/javax.swing.text.AbstractDocument.writeLock(AbstractDocument.java:1372)
             * 
             * will be synced after focus lost, validation errors will be properly shown during edit though
             */
            return Optional.ofNullable(modelValue);
        }
        final Number newComponentValue = converter.fromModelToComponent(modelValue);
        if (prevComponentValue == null || !Objects.equals(Decimal.valueOf(newComponentValue),
                Decimal.valueOf(Futures.getNoInterrupt(prevComponentValue)))) {
            isSettingText = true;
            try {
                Components.setValue(component, newComponentValue);
            } finally {
                isSettingText = false;
            }
            prevComponentValue = ImmutableFuture.of(newComponentValue);
            return Optional.ofNullable(modelValue);
        } else {
            return prevModelValue;
        }
    }

    @Override
    protected Object fromComponentToModel() throws Exception {
        final Number componentValue;
        if (pendingComponentValue != null) {
            componentValue = Futures.getRethrowingNoInterrupt(pendingComponentValue);
        } else {
            /*
             * always go through formatter to get a ParseException here (JSpinner will show the previous valid input
             * instead)
             */
            final JFormattedTextField textField = editor.getTextField();
            final AbstractFormatter formatter = textField.getFormatter();

            final String text = textField.getText();
            if (Strings.isBlank(text)) {
                componentValue = null;
            } else {
                componentValue = (Number) formatter.stringToValue(textField.getText());
            }
        }
        final Object newModelValue = converter.fromComponentToModel(componentValue);
        return newModelValue;
    }

    @Override
    protected IBeanPathPropertyModifier<Object> getModifier() {
        return element.getModifier();
    }

    @Override
    protected void resetCaches() {
        prevComponentValue = null;
    }
}
