package de.invesdwin.context.client.swing.rsyntaxtextarea.expression.completion;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.concurrent.NotThreadSafe;

import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;

@NotThreadSafe
public class AliasedFunctionCompletion extends FunctionCompletion implements IAliasedCompletion {

    private final Set<String> aliases = new TreeSet<>();
    private final String aliasedReference;

    public AliasedFunctionCompletion(final CompletionProvider provider, final String name, final String returnType,
            final String aliasReference) {
        super(provider, name, returnType);
        this.aliasedReference = aliasReference;
    }

    @Override
    public Set<String> getAliases() {
        return aliases;
    }

    @Override
    public String getSummary() {
        final String summary = super.getSummary();
        return appendAliases(summary, aliases);
    }

    public static String appendAliases(final String summary, final Set<String> aliases) {
        if (!aliases.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            if (summary != null) {
                sb.append(summary);
            }
            sb.append("<b>Aliases:</b><br>");
            sb.append("<center><table width='90%'><tr><td>");
            for (final String alias : aliases) {
                sb.append(alias);
                sb.append("<br>");
            }
            sb.append("</td></tr></table></center>");

            return sb.toString();
        } else {
            return summary;
        }
    }

    public static String appendAliasOf(final String summary, final String definition) {
        final StringBuilder sb = new StringBuilder();
        if (summary != null) {
            sb.append(summary);
        }
        sb.append("<b>Alias of:</b><br>");
        sb.append("<center><table width='90%'><tr><td>");
        sb.append(definition);
        sb.append("<br>");
        sb.append("</td></tr></table></center>");

        return sb.toString();
    }

    @Override
    public IAliasedCompletion asAliasedReference(final String inputText) {
        final AliasedFunctionCompletion copy = new AliasedFunctionCompletion(getProvider(), inputText, getType(),
                null) {
            @Override
            public String getSummary() {
                final String summary = super.getSummary();
                return AliasedFunctionCompletion.appendAliasOf(summary, aliasedReference);
            }
        };
        final List<Parameter> params = new ArrayList<>(getParamCount());
        for (int i = 0; i < getParamCount(); i++) {
            params.add(getParam(i));
        }
        copy.setParams(params);
        copy.setShortDescription(getShortDescription());
        copy.setDefinedIn(getDefinedIn());
        copy.setIcon(getIcon());
        copy.setReturnValueDescription(getReturnValueDescription());
        copy.setRelevance(RELEVANCE_ALIAS);
        return copy;
    }

}
