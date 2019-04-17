package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.expression;

import org.fife.ui.rsyntaxtextarea.parser.Parser;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.context.client.swing.rsyntaxtextarea.expression.ExpressionValidatingParser;
import de.invesdwin.util.math.expression.IExpression;
import de.invesdwin.util.math.expression.tokenizer.ParseException;

public interface IExpressionSeriesProvider {

    String getPlotPaneId();

    /**
     * This method will be called to validate the expression. Thus eager exceptions should be thrown here.
     */
    IExpression parseExpression(String expression);

    IPlotSourceDataset newInstance(InteractiveChartPanel chartPanel, String expression);

    void modifyDataset(InteractiveChartPanel chartPanel, IPlotSourceDataset dataset, String expression);

    default Parser newValidatingParser() {
        return new ExpressionValidatingParser() {
            @Override
            protected void parseExpression(final String expression) throws ParseException {
                IExpressionSeriesProvider.this.parseExpression(expression);
            }
        };
    }

}
