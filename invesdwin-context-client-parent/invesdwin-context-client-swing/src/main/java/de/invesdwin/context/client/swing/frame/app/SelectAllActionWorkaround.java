package de.invesdwin.context.client.swing.frame.app;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

import org.jdesktop.application.ApplicationAction;

import de.invesdwin.context.beans.hook.IStartupHook;

/**
 * Workaround for http://kenai.com/jira/browse/BSAF-116
 */
@NotThreadSafe
public class SelectAllActionWorkaround implements IStartupHook {

    private ApplicationAction selectAllAction;

    private final PropertyChangeListener focusOwnerPCL = new PropertyChangeListener() {
        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            final JComponent oldOwner = (JComponent) evt.getOldValue();
            final JComponent newOwner = (JComponent) evt.getNewValue();
            updateFocusOwner(oldOwner, newOwner);
        }
    };
    private final CaretListener caretListener = new CaretListener() {
        @Override
        public void caretUpdate(final CaretEvent e) {
            final JComponent newOwner = DelegateRichApplication.getInstance().getContext().getFocusOwner();
            updateFocusOwner(newOwner, newOwner);
        }
    };

    @Override
    public void startup() {
        final DelegateRichApplication application = DelegateRichApplication.getInstance();
        selectAllAction = (ApplicationAction) application.getContext().getActionMap().get("select-all");
        if (selectAllAction != null) {
            selectAllAction.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(final PropertyChangeEvent evt) {
                    if ("enabled".equals(evt.getPropertyName())) {
                        final JComponent newOwner = application.getContext().getFocusOwner();
                        updateFocusOwner(newOwner, newOwner);
                    }
                }
            });
        }

        application.getContext().addPropertyChangeListener("focusOwner", focusOwnerPCL);
        if (application.getContext().getFocusOwner() instanceof JTextComponent) {
            final JTextComponent text = (JTextComponent) application.getContext().getFocusOwner();
            updateFocusOwner(null, text);
        }
    }

    private void updateFocusOwner(final JComponent oldOwner, final JComponent newOwner) {
        if (oldOwner instanceof JTextComponent) {
            final JTextComponent text = (JTextComponent) oldOwner;
            text.removeCaretListener(caretListener);
        }
        if (newOwner instanceof JTextComponent) {
            final JTextComponent text = (JTextComponent) newOwner;
            if (selectAllAction != null) {
                final Caret caret = text.getCaret();
                final int dot = caret.getDot();
                final int mark = caret.getMark();
                final int length = text.getDocument().getLength();
                final boolean enabled = Math.abs(mark - dot) != length;
                selectAllAction.setEnabled(enabled);
                final Action proxy = selectAllAction.getProxy();
                if (proxy != null) {
                    //proxy will otherwise disable the action again, thus sync the setting there
                    proxy.setEnabled(enabled);
                }
            }
            text.addCaretListener(caretListener);
        }
    }

}
