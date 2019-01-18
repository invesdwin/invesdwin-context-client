package de.invesdwin.context.client.swing.util;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.client.swing.api.AView;

@Immutable
public class SubmitAllViewsHelper {

    public void process(final Component component) {
        final List<AView<?, ?>> views = getViews(component);
        submit(views);
        final boolean valid = validate(views);
        if (valid) {
            commit(views);
        } else {
            rollback(views);
        }
        update(views);
    }

    protected void submit(final List<AView<?, ?>> views) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).getBindingGroup().submit();
        }
    }

    protected boolean validate(final List<AView<?, ?>> views) {
        boolean valid = true;
        for (int i = 0; i < views.size(); i++) {
            if (!views.get(i).getBindingGroup().validate()) {
                valid = false;
            }
        }
        return valid;
    }

    protected void commit(final List<AView<?, ?>> views) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).getBindingGroup().commit();
        }
    }

    protected void rollback(final List<AView<?, ?>> views) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).getBindingGroup().rollback();
        }
    }

    protected void update(final List<AView<?, ?>> views) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).getBindingGroup().update();
        }
    }

    protected List<AView<?, ?>> getViews(final Component component) {
        final List<AView<?, ?>> views = new ArrayList<>();
        new AViewVisitor() {
            @Override
            protected void visit(final AView<?, ?> view) {
                views.add(view);
            }
        }.visitAll(getRootComponent(component));
        return views;
    }

    protected Component getRootComponent(final Component component) {
        return Components.getRootComponentInDockable(component);
    }

}
