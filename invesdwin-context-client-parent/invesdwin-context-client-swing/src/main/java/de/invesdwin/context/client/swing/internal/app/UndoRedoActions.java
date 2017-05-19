package de.invesdwin.context.client.swing.internal.app;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;

import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;
import de.invesdwin.context.beans.hook.IStartupHook;
import de.invesdwin.util.bean.AValueObject;

/**
 * Modeled after TextActions from BSAF.
 * 
 * @author subes
 * 
 */
@SuppressWarnings("serial")
@NotThreadSafe
public class UndoRedoActions extends AValueObject implements IStartupHook {

    private static final String MARKER_ACTION_KEY = UndoRedoActions.class.getSimpleName() + ".markerAction";
    private static final String UNDO_MANAGER_KEY = UndoRedoActions.class.getSimpleName() + ".undoManager";
    private final javax.swing.Action markerAction = new javax.swing.AbstractAction() {
        @Override
        public void actionPerformed(final ActionEvent e) {}
    };
    private final PropertyChangeListener focusOwnerPCL = new PropertyChangeListener() {
        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            final JComponent oldOwner = (JComponent) evt.getOldValue();
            final JComponent newOwner = (JComponent) evt.getNewValue();
            updateFocusOwner(oldOwner, newOwner);
        }
    };
    private final DocumentListener documentListener = new DocumentListener() {
        @Override
        public void removeUpdate(final DocumentEvent e) {
            updateLater();
        }

        @Override
        public void insertUpdate(final DocumentEvent e) {
            updateLater();
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
            updateLater();
        }

        /**
         * If we dont do this later in EDT, canUndo and canRedo stays on false during this cycle!
         */
        @EventDispatchThread(InvocationType.INVOKE_LATER)
        private void updateLater() {
            final JComponent focusOwner = Application.getInstance().getContext().getFocusOwner();
            if (focusOwner instanceof JTextComponent) {
                final JTextComponent text = (JTextComponent) focusOwner;
                updateUndoRedoActions(text);
            }
        }
    };

    private boolean undoEnabled;
    private boolean redoEnabled;

    /**
     * focusOwnerPCL must be set before the frame is visible. Else the proxies won't get set properly on the text
     * components.
     */
    @Override
    public void startup() {
        Application.getInstance().getContext().addPropertyChangeListener("focusOwner", focusOwnerPCL);
        if (Application.getInstance().getContext().getFocusOwner() instanceof JTextComponent) {
            final JTextComponent text = (JTextComponent) Application.getInstance().getContext().getFocusOwner();
            updateFocusOwner(null, text);
        }
    }

    private void updateFocusOwner(final JComponent oldOwner, final JComponent newOwner) {
        if (oldOwner instanceof JTextComponent) {
            final JTextComponent text = (JTextComponent) oldOwner;
            text.getDocument().removeDocumentListener(documentListener);
        }
        if (newOwner instanceof JTextComponent) {
            final JTextComponent text = (JTextComponent) newOwner;
            maybeInstallUndoRedoActions(text);
            updateUndoRedoActions(text);
            text.getDocument().addDocumentListener(documentListener);
        } else if (newOwner == null) {
            setUndoEnabled(false);
            setRedoEnabled(false);
        }
    }

    private void updateUndoRedoActions(final JTextComponent text) {
        final UndoManager undoManager = maybeInstallUndoManager(text);
        final boolean editable = text.isEditable();
        setUndoEnabled(editable && undoManager.canUndo());
        setRedoEnabled(editable && undoManager.canRedo());
    }

    private void maybeInstallUndoRedoActions(final JTextComponent text) {
        final ActionMap actionMap = text.getActionMap();
        if (actionMap.get(MARKER_ACTION_KEY) == null) {
            actionMap.put(MARKER_ACTION_KEY, markerAction);
            final ActionMap undoRedoActions = Application.getInstance().getContext().getActionMap(this);
            for (final Object key : undoRedoActions.keys()) {
                actionMap.put(key, undoRedoActions.get(key));
            }
        }
    }

    private UndoManager maybeInstallUndoManager(final JTextComponent text) {
        final Document document = text.getDocument();
        UndoManager undoManager = (UndoManager) document.getProperty(UNDO_MANAGER_KEY);
        if (undoManager == null) {
            undoManager = new UndoManager();
            document.putProperty(UNDO_MANAGER_KEY, undoManager);
            document.addUndoableEditListener(undoManager);
        }
        return undoManager;
    }

    @Action(enabledProperty = "undoEnabled")
    public void undo() {
        final JTextComponent text = (JTextComponent) Application.getInstance().getContext().getFocusOwner();
        final UndoManager undoManager = (UndoManager) text.getDocument().getProperty(UNDO_MANAGER_KEY);
        undoManager.undo();
    }

    @Action(enabledProperty = "redoEnabled")
    public void redo() {
        final JTextComponent text = (JTextComponent) Application.getInstance().getContext().getFocusOwner();
        final UndoManager undoManager = (UndoManager) text.getDocument().getProperty(UNDO_MANAGER_KEY);
        undoManager.redo();
    }

    public boolean isUndoEnabled() {
        return undoEnabled;
    }

    public void setUndoEnabled(final boolean undoEnabled) {
        this.undoEnabled = undoEnabled;
    }

    public boolean isRedoEnabled() {
        return redoEnabled;
    }

    public void setRedoEnabled(final boolean redoEnabled) {
        this.redoEnabled = redoEnabled;
    }

}
