package de.invesdwin.context.client.swing.util;

import java.awt.Component;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.client.swing.api.AView;

@Immutable
public abstract class AViewVisitor extends AComponentVisitor {

    @Override
    protected final void visit(final Component component) {
        final AView<?, ?> view = Components.getViewAt(component);
        if (view != null) {
            visit(view);
        }
    }

    protected abstract void visit(AView<?, ?> view);

}
