package de.invesdwin.context.client.swing.rsyntaxtextarea.expression;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.Icon;
import javax.swing.JList;

import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionCellRenderer;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.VariableCompletion;

@SuppressWarnings("rawtypes")
@NotThreadSafe
public class ExpressionCompletionCellRenderer extends CompletionCellRenderer {

    private final Icon variableIcon;
    private final Icon functionIcon;

    public ExpressionCompletionCellRenderer() {
        variableIcon = getIcon("variable.gif");
        functionIcon = getIcon("function.gif");
    }

    @Override
    protected void prepareForOtherCompletion(final JList list, final Completion c, final int index,
            final boolean selected, final boolean hasFocus) {
        super.prepareForOtherCompletion(list, c, index, selected, hasFocus);
        setIcon(getEmptyIcon());
    }

    @Override
    protected void prepareForVariableCompletion(final JList list, final VariableCompletion vc, final int index,
            final boolean selected, final boolean hasFocus) {
        super.prepareForVariableCompletion(list, vc, index, selected, hasFocus);
        setIcon(variableIcon);
    }

    @Override
    protected void prepareForFunctionCompletion(final JList list, final FunctionCompletion fc, final int index,
            final boolean selected, final boolean hasFocus) {
        super.prepareForFunctionCompletion(list, fc, index, selected, hasFocus);
        setIcon(functionIcon);
    }

}