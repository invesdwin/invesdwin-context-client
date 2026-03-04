package de.invesdwin.context.client.swing.util;

import java.awt.Component;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.util.collections.factory.pool.list.ICloseableList;
import de.invesdwin.util.collections.factory.pool.list.PooledArrayList;
import de.invesdwin.util.collections.factory.pool.set.ICloseableSet;
import de.invesdwin.util.collections.factory.pool.set.identity.PooledIdentitySet;

@NotThreadSafe
public class UpdateAllViewsHelper {

    private static final UpdateAllViewsHelper DEFAULT_INSTANCE = new UpdateAllViewsHelper();

    public void process(final AView<?, ?> view, final Component component) {
        try (ICloseableList<AView<?, ?>> views = PooledArrayList.getInstance()) {
            getViews(view, component, views);
            update(views);
        }
    }

    protected void update(final List<AView<?, ?>> views) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).getBindingGroup().update();
        }
    }

    protected void getViews(final AView<?, ?> view, final Component component, final List<AView<?, ?>> result) {
        try (ICloseableSet<AView<?, ?>> duplicateViewsFilter = PooledIdentitySet.getInstance()) {
            final Component rootComponent;
            if (view != null) {
                //we prefer the view, since a menu item component won't find its parent views from a popup component
                rootComponent = getRootComponent(view.getComponent());
            } else if (component != null) {
                rootComponent = getRootComponent(component);
            } else {
                //fallback to the frame or dialog and update everything there
                rootComponent = GuiService.get().getWindow();
            }
            new AViewVisitor() {
                @Override
                protected void visit(final AView<?, ?> view) {
                    if (duplicateViewsFilter.add(view)) {
                        if (shouldAddView(view)) {
                            result.add(view);
                        }
                    }
                }

            }.visitAll(rootComponent);
        }
    }

    protected boolean shouldAddView(final AView<?, ?> view) {
        return view.getBindingGroup() != null;
    }

    protected Component getRootComponent(final Component component) {
        return Views.getRootComponentInDockable(component);
    }

    public static void updateAllViews(final AView<?, ?> view, final Component component) {
        DEFAULT_INSTANCE.process(view, component);
    }

    public static void updateAllViews(final AView<?, ?> view) {
        DEFAULT_INSTANCE.process(view, null);
    }

    public static void updateAllViews(final Component component) {
        DEFAULT_INSTANCE.process(null, component);
    }

}
