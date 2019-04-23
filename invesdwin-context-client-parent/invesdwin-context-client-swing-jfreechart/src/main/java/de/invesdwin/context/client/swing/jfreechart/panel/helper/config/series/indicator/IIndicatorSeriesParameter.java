package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.indicator;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.dialog.indicator.modifier.IParameterSettingsModifier;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.SeriesParameterType;
import de.invesdwin.util.math.expression.IExpression;

public interface IIndicatorSeriesParameter {

    String getExpressionName();

    String getName();

    String getDescription();

    IExpression getDefaultValue();

    SeriesParameterType getType();

    IExpression[] getEnumerationValues();

    default IParameterSettingsModifier newModifier(final Runnable modificationListener) {
        return getType().newModifier(this, modificationListener);
    }

}
