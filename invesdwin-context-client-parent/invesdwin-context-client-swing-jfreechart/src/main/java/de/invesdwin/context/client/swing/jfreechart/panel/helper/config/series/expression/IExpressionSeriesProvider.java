package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.expression;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.LanguageAwareCompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.parser.Parser;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.SeriesRendererType;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.context.client.swing.rsyntaxtextarea.expression.ExpressionValidatingParser;
import de.invesdwin.context.client.swing.rsyntaxtextarea.expression.completion.ExpressionAutoCompletion;
import de.invesdwin.util.math.expression.IExpression;
import de.invesdwin.util.math.expression.tokenizer.ParseException;

public interface IExpressionSeriesProvider {

    /**
     * This method will be called to validate the expression. Thus eager exceptions should be thrown here.
     */
    IExpression parseExpression(String expression);

    IPlotSourceDataset newInstance(InteractiveChartPanel chartPanel, String expression, String plotPaneId,
            SeriesRendererType rendererType);

    void modifyDataset(InteractiveChartPanel chartPanel, IPlotSourceDataset dataset, String expression);

    default Parser newValidatingParser() {
        return new ExpressionValidatingParser() {
            @Override
            protected void parseExpression(final String expression) throws ParseException {
                IExpressionSeriesProvider.this.parseExpression(expression);
            }
        };
    }

    CompletionProvider newCompletionProvider();

    default AutoCompletion newAutoCompletion() {
        final CompletionProvider provider = newCompletionProvider();
        provider.setParameterizedCompletionParams('(', ", ", ')');
        final LanguageAwareCompletionProvider languageAwareProvider = new LanguageAwareCompletionProvider(provider);
        final ExpressionAutoCompletion ac = new ExpressionAutoCompletion(languageAwareProvider);
        return ac;
    }

    default void configureEditor(final RSyntaxTextArea editor) {
        editor.clearParsers();
        editor.addParser(newValidatingParser());
        newAutoCompletion().install(editor);
    }

    default String getPlotPaneId(final IExpression expression) {
        return getTitle(expression.toString());
    }

    default String getTitle(final String expression) {
        return expression.toString();
    }

}
