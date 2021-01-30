package de.invesdwin.context.client.swing.util;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.swing.api.view.AView;

@NotThreadSafe
public class UpdateAllViewsHelper {

    private static final UpdateAllViewsHelper DEFAULT_INSTANCE = new UpdateAllViewsHelper();

    public void process(final AView<?, ?> view, final Component component) {
        final List<AView<?, ?>> views = getViews(view, component);
        update(views);
    }

    protected void update(final List<AView<?, ?>> views) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).getBindingGroup().update();
        }
    }

    protected List<AView<?, ?>> getViews(final AView<?, ?> view, final Component component) {
        final List<AView<?, ?>> views = new ArrayList<>();
        final Set<AView<?, ?>> duplicateViewsFilter = Collections.newSetFromMap(new IdentityHashMap<>());
        final Component rootComponent;
        if (view != null) {
            rootComponent = getRootComponent(view.getComponent());
        } else {
            rootComponent = getRootComponent(component);
        }
        new AViewVisitor() {
            @Override
            protected void visit(final AView<?, ?> view) {
                if (duplicateViewsFilter.add(view)) {
                    if (shouldAddView(view)) {
                        views.add(view);
                    }
                }
            }

        }.visitAll(rootComponent);
        return views;
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
