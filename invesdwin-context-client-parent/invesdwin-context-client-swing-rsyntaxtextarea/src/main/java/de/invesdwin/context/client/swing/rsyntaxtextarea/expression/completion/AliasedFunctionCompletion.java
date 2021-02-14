package de.invesdwin.context.client.swing.rsyntaxtextarea.expression.completion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.concurrent.NotThreadSafe;

import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;

@NotThreadSafe
public class AliasedFunctionCompletion extends FunctionCompletion implements IAliasedCompletion {

    private final Map<String, String> aliases = new TreeMap<>();
    private final String aliasedReference;

    public AliasedFunctionCompletion(final CompletionProvider provider, final String name, final String returnType,
            final String aliasedReference) {
        super(provider, name, returnType);
        this.aliasedReference = aliasedReference;
    }

    @Override
    public void putAlias(final String alias, final String description) {
        aliases.put(alias, description);
    }

    @Override
    public String getSummary() {
        final String summary = super.getSummary();
        return appendAliases(summary, aliases.keySet(), null);
    }

    public static String appendAliases(final String summary, final Set<String> aliases, final String thisAlias) {
        if (!aliases.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            if (summary != null) {
                sb.append(summary);
            }
            sb.append("<b>Aliases:</b><br>");
            sb.append("<center><table width='90%'><tr><td>");
            for (final String alias : aliases) {
                if (thisAlias != null && thisAlias.equals(alias)) {
                    sb.append("<i><b>");
                    sb.append(alias);
                    sb.append("</b></i>");
                } else {
                    sb.append(alias);
                }
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
                String summary = super.getSummary();
                summary = appendAliasOf(summary, aliasedReference);
                summary = appendAliases(summary, aliases.keySet(), inputText);
                return summary;
            }
        };
        final List<Parameter> params = new ArrayList<>(getParamCount());
        for (int i = 0; i < getParamCount(); i++) {
            params.add(getParam(i));
        }
        copy.setParams(params);
        copy.setShortDescription(aliases.get(inputText));
        copy.setDefinedIn(getDefinedIn());
        copy.setIcon(getIcon());
        copy.setReturnValueDescription(getReturnValueDescription());
        copy.setRelevance(RELEVANCE_ALIAS);
        return copy;
    }

}
