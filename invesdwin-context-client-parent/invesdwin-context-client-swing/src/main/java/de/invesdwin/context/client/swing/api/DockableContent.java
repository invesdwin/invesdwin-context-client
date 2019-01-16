package de.invesdwin.context.client.swing.api;

import java.awt.Component;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.text.JTextComponent;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import de.invesdwin.context.client.swing.util.AComponentFinder;

@NotThreadSafe
public class DockableContent extends DefaultSingleCDockable {

    private static final AComponentFinder DEFAULT_FOCUS_FINDER = new AComponentFinder() {
        @Override
        protected boolean matches(final Component component) {
            if (component instanceof JComponent
                    && (component instanceof JTextComponent || component instanceof JToggleButton)) {
                final JComponent cComponent = (JComponent) component;
                if (cComponent.isFocusable()) {
                    return true;
                }
            }
            return false;
        }
    };

    public DockableContent(final String id, final Icon icon, final String title, final JComponent component) {
        super(id, icon, title, component);
        setCloseable(true);
    }

    public JComponent getComponent() {
        return (JComponent) getContentPane().getComponent(0);
    }

    public void setComponent(final JComponent component) {
        getContentPane().removeAll();
        getContentPane().add(component);
    }

    @Override
    public Component getFocusComponent() {
        final Component focusComponent = super.getFocusComponent();
        if (focusComponent != null) {
            return focusComponent;
        } else {
            //fallback
            return DEFAULT_FOCUS_FINDER.find(getComponent());
        }
    }

    @Override
    public JComponent getContentPane() {
        return (JComponent) super.getContentPane();
    }

}
