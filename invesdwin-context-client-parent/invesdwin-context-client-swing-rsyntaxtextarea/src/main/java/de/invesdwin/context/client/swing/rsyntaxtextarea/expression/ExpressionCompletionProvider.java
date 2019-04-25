package de.invesdwin.context.client.swing.rsyntaxtextarea.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion.Parameter;
import org.fife.ui.autocomplete.VariableCompletion;

import de.invesdwin.util.lang.Strings;
import de.invesdwin.util.math.expression.ExpressionParser;
import de.invesdwin.util.math.expression.AFunction;
import de.invesdwin.util.math.expression.IFunctionParameterInfo;
import de.invesdwin.util.math.expression.eval.VariableReference;
import de.invesdwin.util.math.expression.variable.IVariable;

@NotThreadSafe
public class ExpressionCompletionProvider extends DefaultCompletionProvider {

    public static final int RELEVANCE_FUNCTION = 1;
    public static final int RELEVANCE_VARIABLE = 2;

    @Override
    protected boolean isValidChar(final char ch) {
        return super.isValidChar(ch) || ch == '#';
    }

    public void registerDefaultCompletions(final Set<String> duplicateExpressionFilter) {
        for (final VariableReference variableReference : ExpressionParser.getDefaultVariables()) {
            final IVariable v = variableReference.getVariable();
            final String expressionName = v.getExpressionName();
            if (duplicateExpressionFilter.add(expressionName)) {
                final VariableCompletion c = newVariable(expressionName, v.getName(), v.getDescription(),
                        v.getType().toString());
                addCompletion(c);
            }
        }

        for (final AFunction f : ExpressionParser.getDefaultFunctions()) {
            final String expressionName = f.getExpressionName();
            if (duplicateExpressionFilter.add(expressionName)) {
                final IFunctionParameterInfo[] parameters = f.getParameterInfos();

                if (parameters.length > 0) {
                    final FunctionCompletion c = newFunction(expressionName, f.getName(), f.getDescription(),
                            f.getReturnType().toString());

                    final List<Parameter> params = new ArrayList<>();
                    for (int i = 0; i < parameters.length; i++) {
                        final IFunctionParameterInfo parameter = parameters[i];
                        final Parameter p = newParameter(parameter.getExpressionName(), parameter.getDescription(),
                                parameter.getType().toString());
                        params.add(p);
                    }
                    c.setParams(params);

                    addCompletion(c);
                } else {
                    final VariableCompletion c = newVariable(expressionName, f.getName(), f.getDescription(),
                            f.getReturnType().toString());
                    addCompletion(c);
                }
            }
        }
    }

    public Parameter newParameter(final String expressionName, final String description, final String type) {
        final Parameter p = new Parameter(type, expressionName);
        p.setDescription(description);
        return p;
    }

    public VariableCompletion newVariable(final String expressionName, final String name, final String description,
            final String type) {
        final VariableCompletion c = new VariableCompletion(this, expressionName, type);
        c.setShortDescription(newNamedDescription(name, description));
        c.setRelevance(RELEVANCE_VARIABLE);
        return c;
    }

    public FunctionCompletion newFunction(final String expressionName, final String name, final String description,
            final String returnType) {
        final FunctionCompletion c = new FunctionCompletion(this, expressionName, returnType);
        c.setShortDescription(newNamedDescription(name, description));
        c.setRelevance(RELEVANCE_FUNCTION);
        return c;
    }

    public static String newNamedDescription(final String name, final String description) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<b style='font-size: large'>");
        sb.append(name);
        sb.append("</b>");
        if (Strings.isNotBlank(description)) {
            sb.append("<br><br>");
            sb.append(description);
        }
        return sb.toString();
    }

}
