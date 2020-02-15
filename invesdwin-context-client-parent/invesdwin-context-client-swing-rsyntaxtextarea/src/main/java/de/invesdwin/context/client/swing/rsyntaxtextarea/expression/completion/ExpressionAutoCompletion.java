package de.invesdwin.context.client.swing.rsyntaxtextarea.expression.completion;

import javax.annotation.concurrent.NotThreadSafe;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

import de.invesdwin.context.client.swing.rsyntaxtextarea.expression.ExpressionCompletionCellRenderer;

@NotThreadSafe
public class ExpressionAutoCompletion extends AutoCompletion {

    public ExpressionAutoCompletion(final CompletionProvider provider) {
        super(provider);
        setListCellRenderer(new ExpressionCompletionCellRenderer());
        //        setAutoActivationEnabled(true);
        //        setAutoActivationDelay(1000);
        //        setAutoCompleteEnabled(true);
        setShowDescWindow(true);
        setParameterAssistanceEnabled(true);
        setAutoCompleteSingleChoices(false);
    }

    @Override
    public boolean hideChildWindows() {
        final StackTraceElement stack = Thread.currentThread().getStackTrace()[2];
        if (stack.getClassName().equals("org.fife.ui.autocomplete.AutoCompletion$ParentWindowListener")
                && stack.getMethodName().equals("componentResized")) {
            //workaround for resizing during completion due to scrollbar hiding/showing
            return false;
        }
        return super.hideChildWindows();
    }

}
