package de.invesdwin.context.client.swing.api.guiservice.dialog;

import java.awt.Window;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JComponent;
import javax.swing.JDialog;

import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.api.IDockable;

@NotThreadSafe
public class DialogDockable extends JDialog implements IDockable {

    private final String uniqueId;
    private AView<?, ?> view;

    public DialogDockable(final String uniqueId, final Window owner) {
        super(owner);
        this.uniqueId = uniqueId;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
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
    public void setView(final AView<?, ?> view) {
        this.view = view;
    }

    @Override
    public AView<?, ?> getView() {
        return view;
    }

}
