package de.invesdwin.context.client.swing.api.guiservice;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.jdesktop.application.Application;

import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.intern.CDockable;
import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;
import de.invesdwin.context.client.swing.api.AModel;
import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.api.DockableContent;
import de.invesdwin.util.assertions.Assertions;

@ThreadSafe
public class ContentPane {

    @GuardedBy("@EventDispatchThread")
    private final Map<String, AView<?, ?>> id_visibleView = new HashMap<String, AView<?, ?>>();

    @Inject
    private de.invesdwin.context.client.swing.internal.content.ContentPaneView contentPaneView;

    /**
     * Throws an exception if the View has already been added.
     */
    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public void addView(final AView<?, ?> view) {
        Assertions.assertThat(containsView(view)).as("View [%s] is already being displayed.", view.getId()).isFalse();
        final DockableContent content = contentPaneView.addView(ContentPane.this, view);
        view.setDockable(ContentPane.this, content);
        Assertions.assertThat(id_visibleView.put(view.getId(), view)).isNull();
    }

    public AView<?, ?> findViewWithEqualModel(final AModel model) {
        for (final AView<?, ?> view : id_visibleView.values()) {
            if (view.getModel().equals(model)) {
                return view;
            }
        }
        return null;
    }

    /**
     * Throws an exception if the View has not been added yet.
     */
    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public void removeView(final AView<?, ?> view) {
        Assertions.assertThat(id_visibleView.remove(view.getId())).isNotNull();
        //May also be called by contentRemoved, in that case we should not trigger that again.
        if (containsView(view)) {
            Assertions.assertThat(contentPaneView.removeView(view)).isTrue();
        }
        view.setDockable(ContentPane.this, null);
    }

    public void removeDockable(final CDockable dockable) {
        removeDockable((SingleCDockable) dockable);
    }

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public void removeDockable(final SingleCDockable dockable) {
        final AView<?, ?> view = id_visibleView.get(dockable.getUniqueId());
        Assertions.assertThat(view).as("No View for Content [%s] found.", dockable.getUniqueId()).isNotNull();
        removeView(view);
    }

    public boolean containsDockable(final CDockable dockable) {
        return containsDockable((SingleCDockable) dockable);
    }

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public boolean containsDockable(final SingleCDockable dockable) {
        return id_visibleView.containsKey(dockable.getUniqueId());
    }

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public boolean containsDockable(final DockableContent dockable) {
        return id_visibleView.containsKey(dockable.getUniqueId());
    }

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public boolean containsView(final AView<?, ?> view) {
        return contentPaneView.containsView(view);
    }

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public void reset() {
        for (final AView<?, ?> view : id_visibleView.values()) {
            removeView(view);
        }
    }

    public void showMainView() {
        final de.invesdwin.context.client.swing.internal.app.DelegateRichApplication application = (de.invesdwin.context.client.swing.internal.app.DelegateRichApplication) Application
                .getInstance();
        application.showMainView();
    }

}
