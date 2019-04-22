package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.expression;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.LanguageAwareCompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.parser.Parser;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.context.client.swing.rsyntaxtextarea.expression.ExpressionCompletionCellRenderer;
import de.invesdwin.context.client.swing.rsyntaxtextarea.expression.ExpressionValidatingParser;
import de.invesdwin.util.lang.Strings;
import de.invesdwin.util.math.expression.IExpression;
import de.invesdwin.util.math.expression.tokenizer.ParseException;

public interface IExpressionSeriesProvider {

    /**
     * This method will be called to validate the expression. Thus eager exceptions should be thrown here.
     */
    IExpression parseExpression(String expression);

    IPlotSourceDataset newInstance(InteractiveChartPanel chartPanel, String expression, String plotPaneId);

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
        final AutoCompletion ac = new AutoCompletion(new LanguageAwareCompletionProvider(provider));
        ac.setListCellRenderer(new ExpressionCompletionCellRenderer());
        ac.setShowDescWindow(true);
        ac.setParameterAssistanceEnabled(true);
        return ac;
    }

    default void configureEditor(final RSyntaxTextArea editor) {
        editor.addParser(newValidatingParser());
        newAutoCompletion().install(editor);
    }

    default String getPlotPaneId(final IExpression expression) {
        return "Expression: " + getTitle(expression.toString());
    }

    default String getTitle(final String expression) {
        return Strings.eventuallyAddPrefix(Strings.eventuallyAddSuffix(expression.toString(), ")"), "(");
    }

}
