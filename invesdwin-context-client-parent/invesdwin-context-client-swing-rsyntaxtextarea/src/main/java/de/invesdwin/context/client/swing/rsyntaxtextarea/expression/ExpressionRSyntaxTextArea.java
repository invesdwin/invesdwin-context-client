package de.invesdwin.context.client.swing.rsyntaxtextarea.expression;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JPopupMenu;
import javax.swing.undo.UndoManager;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RUndoManager;

import de.invesdwin.util.swing.text.IUndoManagerAware;

@NotThreadSafe
public class ExpressionRSyntaxTextArea extends RSyntaxTextArea implements IUndoManagerAware {

    private UndoManager undoManager;

    @Override
    protected RUndoManager createUndoManager() {
        undoManager = super.createUndoManager();
        return (RUndoManager) undoManager;
    }

    @Override
    public UndoManager getUndoManager() {
        return undoManager;
    }

    @Override
    public void replaceUndoManager(final UndoManager undoManager) {
        getDocument().removeUndoableEditListener(this.undoManager);
        this.undoManager.discardAllEdits();
        this.undoManager = undoManager;
        setPopupMenu(null);
        getDocument().addUndoableEditListener(undoManager);
    }

    @Override
    public boolean isUndoManagerReplaceable() {
        //popup menu can be replaced
        return true;
    }

    @Override
    protected void configurePopupMenu(final JPopupMenu popupMenu) {
        if (undoManager instanceof RUndoManager) {
            super.configurePopupMenu(popupMenu);
        }
    }

}
