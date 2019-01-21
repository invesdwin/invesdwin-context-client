package de.invesdwin.context.client.swing.api.binding.component.button;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.AbstractButton;
import javax.swing.Action;

import de.invesdwin.context.client.swing.api.binding.component.IComponentBinding;

@NotThreadSafe
public class ActionButtonBinding implements IComponentBinding {

    private final AbstractButton component;

    public ActionButtonBinding(final AbstractButton component, final Action action) {
        this.component = component;
        component.setAction(action);
    }

    @Override
    public String getBeanPath() {
        return component.getName();
    }

    protected boolean isModifiable() {
        return component.isVisible() && component.isEnabled();
    }

    @Override
    public void submit() {
        //noop
    }

    @Override
    public String validate() {
        //noop
        return null;
    }

    @Override
    public String getInvalidMessage() {
        //noop
        return null;
    }

    @Override
    public void setInvalidMessage(final String message) {
        //noop
    }

    @Override
    public void commit() {
        //noop
    }

    @Override
    public void rollback() {
        //noop
    }

    @Override
    public void update() {
        //noop
    }

}
