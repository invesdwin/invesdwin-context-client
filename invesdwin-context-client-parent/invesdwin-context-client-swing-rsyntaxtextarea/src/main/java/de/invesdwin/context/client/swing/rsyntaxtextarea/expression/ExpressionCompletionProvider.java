package de.invesdwin.context.client.swing.rsyntaxtextarea.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ParameterizedCompletion.Parameter;

import de.invesdwin.context.client.swing.rsyntaxtextarea.expression.completion.AliasedFunctionCompletion;
import de.invesdwin.context.client.swing.rsyntaxtextarea.expression.completion.AliasedVariableCompletion;
import de.invesdwin.context.client.swing.rsyntaxtextarea.expression.completion.IAliasedCompletion;
import de.invesdwin.util.lang.Strings;
import de.invesdwin.util.math.expression.AFunction;
import de.invesdwin.util.math.expression.ExpressionParser;
import de.invesdwin.util.math.expression.IFunctionParameterInfo;
import de.invesdwin.util.math.expression.variable.IVariable;

@NotThreadSafe
public class ExpressionCompletionProvider extends DefaultCompletionProvider {

    public static final int RELEVANCE_FUNCTION = 1;
    public static final int RELEVANCE_VARIABLE = 2;

    @Override
    protected boolean isValidChar(final char ch) {
        return super.isValidChar(ch) || ch == '#';
    }

    public void addDefaultCompletions(final Set<String> duplicateExpressionFilter,
            final Map<String, IAliasedCompletion> name_completion) {
        for (final IVariable v : ExpressionParser.getDefaultVariables()) {
            final String expressionName = v.getExpressionName();
            final String name = v.getName();
            if (duplicateExpressionFilter.add(expressionName)) {
                final IAliasedCompletion existing = name_completion.get(name);
                if (existing == null) {
                    final AliasedVariableCompletion c = newVariable(expressionName, name, v.getDescription(),
                            v.getType().toString());
                    name_completion.put(name, c);
                } else {
                    existing.getAliases().add(expressionName);
                }
            }
        }

        for (final AFunction f : ExpressionParser.getDefaultFunctions()) {
            final String expressionName = f.getExpressionName();
            final String name = f.getName();
            if (duplicateExpressionFilter.add(expressionName)) {
                final IAliasedCompletion existing = name_completion.get(name);
                if (existing == null) {
                    final IFunctionParameterInfo[] parameters = f.getParameterInfos();
                    if (parameters.length > 0) {
                        final AliasedFunctionCompletion c = newFunction(expressionName, name, f.getDescription(),
                                f.getReturnType().toString());

                        final List<Parameter> params = new ArrayList<>();
                        for (int i = 0; i < parameters.length; i++) {
                            final IFunctionParameterInfo parameter = parameters[i];
                            final Parameter p = newParameter(parameter.getExpressionName(), parameter.getDescription(),
                                    parameter.getType().toString());
                            params.add(p);
                        }
                        c.setParams(params);

                        name_completion.put(name, c);
                    } else {
                        final AliasedVariableCompletion c = newVariable(expressionName, name, f.getDescription(),
                                f.getReturnType().toString());
                        name_completion.put(name, c);
                    }
                } else {
                    final String alias = f.getExpressionString(f.getDefaultValues());
                    existing.getAliases().add(alias);
                }
            }
        }
        addCompletions(name_completion.values());
    }

    public void addCompletions(final Collection<? extends Completion> completions) {
        for (final Completion completion : completions) {
            addCompletion(completion);
        }
    }

    public Parameter newParameter(final String expressionName, final String description, final String type) {
        final Parameter p = new Parameter(type, expressionName);
        p.setDescription(description);
        return p;
    }

    public AliasedVariableCompletion newVariable(final String expressionName, final String name,
            final String description, final String type) {
        final AliasedVariableCompletion c = new AliasedVariableCompletion(this, expressionName, type);
        c.setShortDescription(newNamedDescription(name, description));
        c.setRelevance(RELEVANCE_VARIABLE);
        return c;
    }

    public AliasedFunctionCompletion newFunction(final String expressionName, final String name,
            final String description, final String returnType) {
        final AliasedFunctionCompletion c = new AliasedFunctionCompletion(this, expressionName, returnType);
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
