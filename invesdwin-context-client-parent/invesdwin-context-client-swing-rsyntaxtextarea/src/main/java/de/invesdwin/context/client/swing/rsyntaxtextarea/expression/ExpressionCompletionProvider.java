package de.invesdwin.context.client.swing.rsyntaxtextarea.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.text.JTextComponent;

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

    private final Map<String, IAliasedCompletion> alias_completion = new HashMap<>();

    @Override
    protected boolean isValidChar(final char ch) {
        return super.isValidChar(ch) || ch == '#';
    }

    public void addDefaultCompletions(final Set<String> duplicateExpressionFilter,
            final Map<String, IAliasedCompletion> name_completion,
            final Map<String, IAliasedCompletion> alias_completion) {
        for (final IVariable v : ExpressionParser.getDefaultVariables()) {
            final String expressionName = v.getExpressionName();
            final String name = v.getName();
            if (duplicateExpressionFilter.add(expressionName)) {
                final IAliasedCompletion existing = name_completion.get(name);
                if (existing == null) {
                    final String aliasedReference = expressionName;
                    final AliasedVariableCompletion c = newVariable(expressionName, name, v.getDescription(),
                            v.getType().toString(), aliasedReference);
                    name_completion.put(name, c);
                } else {
                    existing.getAliases().add(expressionName);
                    alias_completion.put(v.getExpressionName(), existing);
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
                        final String aliasedReference = f.getExpressionString(f.getDefaultValues());
                        final AliasedFunctionCompletion c = newFunction(expressionName, name, f.getDescription(),
                                f.getReturnType().toString(), aliasedReference);

                        final List<Parameter> params = new ArrayList<>();
                        for (int i = 0; i < parameters.length; i++) {
                            final IFunctionParameterInfo parameter = parameters[i];
                            final Parameter p = newParameter(parameter.getExpressionName(),
                                    parameter.getDescriptionWithDetails(), parameter.getType().toString());
                            params.add(p);
                        }
                        c.setParams(params);

                        name_completion.put(name, c);
                    } else {
                        final String aliasedReference = expressionName;
                        final AliasedVariableCompletion c = newVariable(expressionName, name, f.getDescription(),
                                f.getReturnType().toString(), aliasedReference);
                        name_completion.put(name, c);
                    }
                } else {
                    final String alias = f.getExpressionString(f.getDefaultValues());
                    existing.getAliases().add(alias);
                    alias_completion.put(f.getExpressionName(), existing);
                }
            }
        }
        addCompletions(name_completion.values(), alias_completion);
    }

    public void addCompletions(final Collection<? extends Completion> completions,
            final Map<String, IAliasedCompletion> alias_completion) {
        for (final Completion completion : completions) {
            addCompletion(completion);
        }
        this.alias_completion.putAll(alias_completion);
    }

    public Parameter newParameter(final String expressionName, final String description, final String type) {
        final Parameter p = new Parameter(type, expressionName);
        p.setDescription(description);
        return p;
    }

    public AliasedVariableCompletion newVariable(final String expressionName, final String name,
            final String description, final String type, final String aliasedReference) {
        final AliasedVariableCompletion c = new AliasedVariableCompletion(this, expressionName, type, aliasedReference);
        c.setShortDescription(newNamedDescription(name, description));
        c.setRelevance(IAliasedCompletion.RELEVANCE_VARIABLE);
        return c;
    }

    public AliasedFunctionCompletion newFunction(final String expressionName, final String name,
            final String description, final String returnType, final String aliasedReference) {
        final AliasedFunctionCompletion c = new AliasedFunctionCompletion(this, expressionName, returnType,
                aliasedReference);
        c.setShortDescription(newNamedDescription(name, description));
        c.setRelevance(IAliasedCompletion.RELEVANCE_FUNCTION);
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

    private List<Completion> maybeAddAliasCompletion(final String inputText, final List<Completion> normalCompletions) {
        final IAliasedCompletion alias = alias_completion.get(inputText);
        if (alias != null) {
            final int indexOfAlias = normalCompletions.indexOf(alias);
            final List<Completion> extendedCompletions = new ArrayList<>(normalCompletions);
            extendedCompletions.add(0, alias.asAliasedReference(inputText));
            if (indexOfAlias < 0) {
                extendedCompletions.add(alias);
            }
            return extendedCompletions;
        }
        return normalCompletions;
    }

    @Override
    public List<Completion> getCompletionByInputText(final String inputText) {
        final List<Completion> normalCompletions = super.getCompletionByInputText(inputText);
        return maybeAddAliasCompletion(inputText, normalCompletions);
    }

    @Override
    protected List<Completion> getCompletionsImpl(final JTextComponent comp) {
        final String inputText = getAlreadyEnteredText(comp);
        final List<Completion> normalCompletions = super.getCompletionsImpl(comp);
        return maybeAddAliasCompletion(inputText, normalCompletions);
    }

}
