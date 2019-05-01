package de.invesdwin.context.client.swing.rsyntaxtextarea.expression.completion;

import java.util.Set;
import java.util.TreeSet;

import javax.annotation.concurrent.NotThreadSafe;

import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.VariableCompletion;

@NotThreadSafe
public class AliasedVariableCompletion extends VariableCompletion implements IAliasedCompletion {

    private final Set<String> aliases = new TreeSet<>();

    public AliasedVariableCompletion(final CompletionProvider provider, final String name, final String type) {
        super(provider, name, type);
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

}
