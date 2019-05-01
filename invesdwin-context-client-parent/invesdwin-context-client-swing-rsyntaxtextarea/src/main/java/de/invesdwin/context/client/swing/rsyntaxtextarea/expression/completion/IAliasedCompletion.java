package de.invesdwin.context.client.swing.rsyntaxtextarea.expression.completion;

import java.util.Set;

import org.fife.ui.autocomplete.Completion;

public interface IAliasedCompletion extends Completion {

    Set<String> getAliases();

}
