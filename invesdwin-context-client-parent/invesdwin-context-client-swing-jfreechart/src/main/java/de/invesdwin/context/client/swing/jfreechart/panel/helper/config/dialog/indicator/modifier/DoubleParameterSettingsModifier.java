package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.dialog.indicator.modifier;

import java.math.BigDecimal;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.indicator.IIndicatorSeriesParameter;
import de.invesdwin.util.math.Doubles;
import de.invesdwin.util.math.expression.ExpressionType;
import de.invesdwin.util.math.expression.IExpression;
import de.invesdwin.util.math.expression.eval.ConstantExpression;
import de.invesdwin.util.swing.spinner.JSpinnerDecimal;

@NotThreadSafe
public class DoubleParameterSettingsModifier extends AParameterSettingsModifier {
    private final JSpinnerDecimal component;

    public DoubleParameterSettingsModifier(final IIndicatorSeriesParameter parameter,
            final Runnable modificationListener) {
        super(parameter);

        this.component = new JSpinnerDecimal();
        this.component.getModel().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent e) {
                DoubleParameterSettingsModifier.super.setValue(
                        new ConstantExpression(Doubles.checkedCast(component.getValue()), ExpressionType.Double));
                modificationListener.run();
            }
        });
    }

    @Override
    public void setValue(final IExpression value) {
        super.setValue(value);
        component.setValue(new BigDecimal(value.toString()));
    }

    @Override
    public JComponent getComponent() {
        return component;
    }

}
