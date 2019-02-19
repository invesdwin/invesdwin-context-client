package de.invesdwin.context.client.swing.util;

import java.awt.Component;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.util.swing.AComponentVisitor;

@Immutable
public abstract class AViewVisitor extends AComponentVisitor {

    @Override
    protected final void visit(final Component component) {
        final AView<?, ?> view = Views.getViewAt(component);
        if (view != null) {
            visit(view);
        }
    }

    protected abstract void visit(AView<?, ?> view);

}
