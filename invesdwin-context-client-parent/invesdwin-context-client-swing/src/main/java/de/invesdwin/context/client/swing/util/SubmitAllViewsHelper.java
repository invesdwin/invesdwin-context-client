package de.invesdwin.context.client.swing.util;

import java.awt.Component;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.util.lang.Strings;

@Immutable
public class SubmitAllViewsHelper extends UpdateAllViewsHelper {

    private static final SubmitAllViewsHelper DEFAULT_INSTANCE = new SubmitAllViewsHelper();

    @Override
    public void process(final AView<?, ?> view, final Component component) {
        final List<AView<?, ?>> views = getViews(view, component);
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
        final Set<String> duplicateMessageFilter = new HashSet<>();
        for (int i = 0; i < views.size(); i++) {
            final BindingGroup bindingGroup = views.get(i).getBindingGroup();
            final String invalidMessage = bindingGroup.validate();
            if (Strings.isNotBlank(invalidMessage) && duplicateMessageFilter.add(invalidMessage)) {
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

    public static void submitAllViews(final AView<?, ?> view, final Component component) {
        DEFAULT_INSTANCE.process(view, component);
    }

    public static void submitAllViews(final AView<?, ?> view) {
        DEFAULT_INSTANCE.process(view, null);
    }

    public static void submitAllViews(final Component component) {
        DEFAULT_INSTANCE.process(null, component);
    }
}
