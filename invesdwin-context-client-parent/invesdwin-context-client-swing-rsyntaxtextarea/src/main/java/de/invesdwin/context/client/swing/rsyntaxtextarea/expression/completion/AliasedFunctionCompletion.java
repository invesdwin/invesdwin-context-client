package de.invesdwin.context.client.swing.rsyntaxtextarea.expression.completion;

import java.util.Set;
import java.util.TreeSet;

import javax.annotation.concurrent.NotThreadSafe;

import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;

@NotThreadSafe
public class AliasedFunctionCompletion extends FunctionCompletion implements IAliasedCompletion {

    private final Set<String> aliases = new TreeSet<>();

    public AliasedFunctionCompletion(final CompletionProvider provider, final String name, final String returnType) {
        super(provider, name, returnType);
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

}
