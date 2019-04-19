package de.invesdwin.context.client.swing.rsyntaxtextarea.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion.Parameter;
import org.fife.ui.autocomplete.VariableCompletion;

import de.invesdwin.util.math.expression.ExpressionParser;
import de.invesdwin.util.math.expression.IFunction;
import de.invesdwin.util.math.expression.IFunctionParameterInfo;
import de.invesdwin.util.math.expression.eval.VariableReference;
import de.invesdwin.util.math.expression.variable.IVariable;

@Immutable
public final class ExpressionCompletionProviders {

    public static final int RELEVANCE_FUNCTION = 1;
    public static final int RELEVANCE_VARIABLE = 2;

    private ExpressionCompletionProviders() {}

    public static void registerDefaultCompletions(final DefaultCompletionProvider provider,
            final Set<String> duplicateExpressionFilter) {
        for (final VariableReference variableReference : ExpressionParser.getDefaultVariables()) {
            final IVariable v = variableReference.getVariable();
            final String expressionName = v.getExpressionName();
            if (duplicateExpressionFilter.add(expressionName)) {
                final VariableCompletion c = new VariableCompletion(provider, expressionName, v.getType().toString());
                c.setShortDescription(newNamedDescription(v.getName(), v.getDescription()));
                c.setRelevance(RELEVANCE_VARIABLE);
                provider.addCompletion(c);
            }
        }

        for (final IFunction f : ExpressionParser.getDefaultFunctions()) {
            final String expressionName = f.getExpressionName();
            if (duplicateExpressionFilter.add(expressionName)) {
                final FunctionCompletion c = new FunctionCompletion(provider, expressionName,
                        f.getReturnType().toString());
                c.setShortDescription(newNamedDescription(f.getName(), f.getDescription()));
                c.setRelevance(RELEVANCE_FUNCTION);

                final List<Parameter> params = new ArrayList<>();
                final IFunctionParameterInfo[] parameters = f.getParameterInfos();
                for (int i = 0; i < parameters.length; i++) {
                    final IFunctionParameterInfo parameter = parameters[i];
                    final Parameter p = new Parameter(parameter.getType().toString(), parameter.getName(),
                            i == parameters.length - 1);
                    p.setDescription(parameter.getDescription());
                    params.add(p);
                }
                c.setParams(params);

                provider.addCompletion(c);
                duplicateExpressionFilter.add(c.getReplacementText());
            }
        }
    }

    public static String newNamedDescription(final String name, final String description) {
        return "<h2>" + name + "</h2>" + description;
    }

}
