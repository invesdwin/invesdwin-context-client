package de.invesdwin.context.client.swing.api.binding.component;

import java.util.Optional;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.Range;

import com.jgoodies.common.base.Strings;

import de.invesdwin.context.beans.validator.DecimalRange;
import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.converter.IConverter;
import de.invesdwin.context.client.swing.api.binding.converter.NumberToNumberConverter;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.math.decimal.Decimal;
import de.invesdwin.util.swing.spinner.SpinnerDecimalEditor;
import de.invesdwin.util.swing.spinner.SpinnerDecimalModel;

@NotThreadSafe
public class SpinnerBinding extends AComponentBinding<JSpinner, Object> {

    private final IConverter<Object, Number> converter;
    private Optional<Number> prevComponentValue;

    public SpinnerBinding(final JSpinner component, final APropertyBeanPathElement element,
            final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        final String format = getFormat(element);
        final SpinnerDecimalModel model = SpinnerDecimalModel
                .newModel(element.getAccessor().getType().isIntegralNumber());
        model.setMinimum(determineMinimum());
        model.setMaximum(determineMaximum());
        component.setModel(model);
        final SpinnerDecimalEditor editor = new SpinnerDecimalEditor(component, format);
        component.setEditor(editor);
        this.converter = newConverter();
        if (eagerSubmitRunnable != null) {
            component.getModel().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(final ChangeEvent e) {
                    eagerSubmitRunnable.run();
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
        final Range range = element.getAccessor().getAnnotation(Range.class);
        if (range != null) {
            return Decimal.valueOf(range.min());
        }
        final DecimalRange decimalRange = element.getAccessor().getAnnotation(DecimalRange.class);
        if (decimalRange != null) {
            return Decimal.valueOf(decimalRange.min());
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
        final Range range = element.getAccessor().getAnnotation(Range.class);
        if (range != null) {
            return Decimal.valueOf(range.max());
        }
        final DecimalRange decimalRange = element.getAccessor().getAnnotation(DecimalRange.class);
        if (decimalRange != null) {
            return Decimal.valueOf(decimalRange.max());
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
        final Number newComponentValue = converter.fromModelToComponent(modelValue);
        if (prevComponentValue == null || !Objects.equals(newComponentValue, prevComponentValue.orElse(null))) {
            component.setValue(newComponentValue);
            prevComponentValue = Optional.ofNullable(newComponentValue);
            return Optional.ofNullable(modelValue);
        } else {
            return prevModelValue;
        }
    }

    @Override
    protected Object fromComponentToModel() {
        final Number componentValue = (Number) component.getValue();
        final Object newModelValue = converter.fromComponentToModel(componentValue);
        return newModelValue;
    }

    @Override
    protected IBeanPathPropertyModifier<Object> getModifier() {
        return element.getModifier();
    }

}
