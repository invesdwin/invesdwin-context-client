package de.invesdwin.context.client.swing.frame.content;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class ContentPaneKeyEventDispatcher implements KeyEventDispatcher {

    public static final ContentPaneKeyEventDispatcher INSTANCE = new ContentPaneKeyEventDispatcher();

    private volatile boolean controlDown;
    private volatile boolean metaDown;
    private volatile boolean shiftDown;
    private volatile boolean altDown;
    private volatile boolean altGraphDown;
    @GuardedBy("this")
    private boolean registered;

    private ContentPaneKeyEventDispatcher() {}

    @Override
    public boolean dispatchKeyEvent(final KeyEvent e) {
        switch (e.getID()) {
        case KeyEvent.KEY_PRESSED:
            updateKeyDown(e, true);
            break;
        case KeyEvent.KEY_RELEASED:
            updateKeyDown(e, false);
            break;
        default:
            break;
        }
        return false;
    }

    private void updateKeyDown(final KeyEvent e, final boolean state) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_META:
            metaDown = state;
            break;
        case KeyEvent.VK_CONTROL:
            controlDown = state;
            break;
        case KeyEvent.VK_SHIFT:
            shiftDown = state;
            break;
        case KeyEvent.VK_ALT:
            altDown = state;
            break;
        case KeyEvent.VK_ALT_GRAPH:
            altGraphDown = state;
        default:
            break;
        }
    }

    public boolean isControlDown() {
        return controlDown;
    }

    public boolean isShiftDown() {
        return shiftDown;
    }

    public boolean isAltDown() {
        return altDown;
    }

    public boolean isAltGraphDown() {
        return altGraphDown;
    }

    public boolean isMetaDown() {
        return metaDown;
    }

    public boolean isModifierDown() {
        return controlDown || shiftDown || altDown || altGraphDown || metaDown;
    }

    public synchronized void register() {
        if (!registered) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
            registered = true;
        }
    }

    public synchronized void unregister() {
        if (registered) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
            reset();
        }
    }

    private void reset() {
        controlDown = false;
        metaDown = false;
        shiftDown = false;
        altDown = false;
        altGraphDown = false;
    }
}
