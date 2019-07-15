package de.invesdwin.context.client.swing.rsyntaxtextarea.expression.completion;

import java.util.Set;

import org.fife.ui.autocomplete.Completion;

public interface IAliasedCompletion extends Completion {

    int RELEVANCE_FUNCTION = 100;
    int RELEVANCE_VARIABLE = 200;
    int RELEVANCE_ALIAS = 300;

    Set<String> getAliases();

    IAliasedCompletion asAliasedReference(String inputText);

}
