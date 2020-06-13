package de.invesdwin.context.client.swing.rsyntaxtextarea.expression.completion;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.text.Document;
import javax.swing.text.Element;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;

import de.invesdwin.context.client.swing.rsyntaxtextarea.expression.ExpressionCompletionCellRenderer;

@NotThreadSafe
public class ExpressionAutoCompletion extends AutoCompletion {

    public ExpressionAutoCompletion(final CompletionProvider provider) {
        super(provider);
        setListCellRenderer(new ExpressionCompletionCellRenderer());
        setAutoActivationEnabled(true);
        setAutoActivationDelay(3000);
        setAutoCompleteEnabled(true);
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

    @Override
    protected int refreshPopupWindow() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length >= 4) {
            // prevent timer based auto completion on finished completions
            // 0: java.base/java.lang.Thread.getStackTrace(Thread.java:1598)
            // 1: de.invesdwin.context.client.swing.rsyntaxtextarea.expression.completion.ExpressionAutoCompletion.refreshPopupWindow(ExpressionAutoCompletion.java:43)
            // 2: org.fife.ui.autocomplete.AutoCompletion.doCompletion(AutoCompletion.java:292)
            // 3: org.fife.ui.autocomplete.AutoCompletion$AutoActivationListener.actionPerformed(AutoCompletion.java:1300)
            // 4: java.desktop/javax.swing.Timer.fireActionPerformed(Timer.java:317)
            final StackTraceElement stackTraceElement = stackTrace[3];
            if (stackTraceElement.getClassName()
                    .equals("org.fife.ui.autocomplete.AutoCompletion$AutoActivationListener")) {
                return maybePreventCompletionPopupForTimer(stackTrace);
            }
        }
        return super.refreshPopupWindow();
    }

    public int maybePreventCompletionPopupForTimer(final StackTraceElement[] stackTrace) {
        if (isPopupVisible()) {
            return super.refreshPopupWindow();
        }

        // A return value of null => don't suggest completions
        final String text = getCompletionProvider().getAlreadyEnteredText(getTextComponent());
        if (text == null) {
            return getLineOfCaretDuplicate();
        }

        final List<Completion> completions = getCompletionProvider().getCompletions(getTextComponent());
        if (completions == null) {
            return getLineOfCaretDuplicate();
        }
        for (final Completion completion : completions) {
            if (text.equalsIgnoreCase(completion.getReplacementText())) {
                hidePopupWindow();
                return getLineOfCaretDuplicate();
            }
        }

        return super.refreshPopupWindow();
    }

    /**
     * inherited method is private, thus need to use a suffixed name for not to get warnings
     */
    private int getLineOfCaretDuplicate() {
        final Document doc = getTextComponent().getDocument();
        final Element root = doc.getDefaultRootElement();
        return root.getElementIndex(getTextComponent().getCaretPosition());
    }

}
