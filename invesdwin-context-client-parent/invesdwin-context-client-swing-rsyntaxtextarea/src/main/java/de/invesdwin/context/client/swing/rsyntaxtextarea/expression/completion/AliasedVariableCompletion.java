package de.invesdwin.context.client.swing.rsyntaxtextarea.expression.completion;

import java.util.Set;
import java.util.TreeSet;

import javax.annotation.concurrent.NotThreadSafe;

import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.VariableCompletion;

@NotThreadSafe
public class AliasedVariableCompletion extends VariableCompletion implements IAliasedCompletion {

    private final Set<String> aliases = new TreeSet<>();
    private final String example;

    public AliasedVariableCompletion(final CompletionProvider provider, final String name, final String type,
            final String example) {
        super(provider, name, type);
        this.example = example;
    }

    @Override
    public Set<String> getAliases() {
        return aliases;
    }

    @Override
    public String getSummary() {
        final String summary = super.getSummary();
        return AliasedFunctionCompletion.appendAliases(summary, aliases);
    }

    @Override
    public IAliasedCompletion asAliasedReference(final String inputText) {
        final AliasedVariableCompletion copy = new AliasedVariableCompletion(getProvider(), inputText, getType(),
                null) {
            @Override
            public String getSummary() {
                final String summary = super.getSummary();
                return AliasedFunctionCompletion.appendAliasOf(summary, example);
            }
        };
        copy.setShortDescription(getShortDescription());
        copy.setIcon(getIcon());
        copy.setDefinedIn(getDefinedIn());
        copy.setRelevance(RELEVANCE_ALIAS);
        return copy;
    }

}
