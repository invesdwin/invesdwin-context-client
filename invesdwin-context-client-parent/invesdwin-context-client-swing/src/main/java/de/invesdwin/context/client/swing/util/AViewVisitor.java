package de.invesdwin.context.client.swing.util;

import java.awt.Component;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.util.collections.factory.ILockCollectionFactory;
import de.invesdwin.util.swing.AComponentVisitor;

@NotThreadSafe
public abstract class AViewVisitor extends AComponentVisitor {

    private final Set<AView<?, ?>> uniqueViews = ILockCollectionFactory.getInstance(false).newIdentitySet();

    @Override
    protected final void visit(final Component component) {
        final AView<?, ?> view = Views.getViewAt(component);
        if (view != null && uniqueViews.add(view)) {
            visit(view);
        }
    }

    protected abstract void visit(AView<?, ?> view);

}
