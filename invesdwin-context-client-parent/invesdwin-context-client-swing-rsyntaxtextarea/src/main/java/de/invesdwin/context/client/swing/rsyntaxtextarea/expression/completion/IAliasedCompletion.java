package de.invesdwin.context.client.swing.rsyntaxtextarea.expression.completion;

import org.fife.ui.autocomplete.Completion;

public interface IAliasedCompletion extends Completion {

    int RELEVANCE_FUNCTION = 100;
    int RELEVANCE_VARIABLE = 200;
    int RELEVANCE_ALIAS = 300;

    void putAlias(String alias, String description);

    IAliasedCompletion asAliasedReference(String inputText);

}
