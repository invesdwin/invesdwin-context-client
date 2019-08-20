package de.invesdwin.context.client.swing.impl.content;

import java.awt.Component;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.Icon;
import javax.swing.JComponent;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.context.client.swing.api.view.IDockable;
import de.invesdwin.util.swing.AComponentFinder;

@NotThreadSafe
public class ContentPaneDockable extends DefaultSingleCDockable implements IDockable {

    private AView<?, ?> view;

    public ContentPaneDockable(final String id, final Icon icon, final String title, final JComponent component) {
        super(id, icon, title, component);
        setCloseable(true);
    }

    @Override
    public JComponent getComponent() {
        return (JComponent) getContentPane().getComponent(0);
    }

    @Override
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
            return AComponentFinder.DEFAULT_FOCUS.find(getComponent());
        }
    }

    @Override
    public JComponent getContentPane() {
        return (JComponent) super.getContentPane();
    }

    @Override
    public void requestFocus() {
        toFront(getFocusComponent());
    }

    @Override
    public void setView(final AView<?, ?> view) {
        this.view = view;
    }

    @Override
    public AView<?, ?> getView() {
        return view;
    }

}
