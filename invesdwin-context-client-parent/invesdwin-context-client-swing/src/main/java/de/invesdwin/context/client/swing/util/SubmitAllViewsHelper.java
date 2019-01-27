package de.invesdwin.context.client.swing.util;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.Immutable;

import com.jgoodies.common.base.Strings;

import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;

@Immutable
public class SubmitAllViewsHelper {

    public void process(final Component component) {
        final List<AView<?, ?>> views = getViews(component);
        submit(views);
        final String invalidMessage = validate(views);
        if (invalidMessage == null) {
            commit(views);
        } else {
            showInvalidMessage(invalidMessage);
            rollback(views);
        }
        update(views);
    }

    protected void showInvalidMessage(final String invalidMessage) {
        GuiService.get().getStatusBar().error(invalidMessage);
    }

    protected void submit(final List<AView<?, ?>> views) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).getBindingGroup().submit();
        }
    }

    protected String validate(final List<AView<?, ?>> views) {
        String combinedInvalidMessage = null;
        for (int i = 0; i < views.size(); i++) {
            final String invalidMessage = views.get(i).getBindingGroup().validate();
            if (Strings.isNotBlank(invalidMessage)) {
                if (combinedInvalidMessage != null) {
                    combinedInvalidMessage += "\n";
                    combinedInvalidMessage += invalidMessage;
                } else {
                    combinedInvalidMessage = invalidMessage;
                }
            }
        }
        return combinedInvalidMessage;
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
                if (view.getBindingGroup() != null) {
                    views.add(view);
                }
            }
        }.visitAll(getRootComponent(component));
        return views;
    }

    protected Component getRootComponent(final Component component) {
        return Components.getRootComponentInDockable(component);
    }

}
