package de.invesdwin.context.client.swing.api.guiservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.jdesktop.application.Application;

import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.intern.CDockable;
import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.context.client.swing.api.view.IDockable;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.loadingcache.ALoadingCache;
import de.invesdwin.util.lang.Objects;

@ThreadSafe
public class ContentPane {

    @GuardedBy("@EventDispatchThread")
    private final Map<String, AView<?, ?>> id_visibleView = new HashMap<String, AView<?, ?>>();
    @GuardedBy("@EventDispatchThread")
    private final ALoadingCache<Class<?>, Map<String, AView<?, ?>>> class_id_visibleView = new ALoadingCache<Class<?>, Map<String, AView<?, ?>>>() {

        @Override
        protected Map<String, AView<?, ?>> loadValue(final Class<?> key) {
            return new HashMap<>();
        }
    };

    @Inject
    private de.invesdwin.context.client.swing.impl.content.ContentPaneView contentPaneView;

    /**
     * Throws an exception if the View has not been added yet.
     */
    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public void removeView(final AView<?, ?> view) {
        //May also be called by contentRemoved, in that case we should not trigger that again.
        if (containsView(view)) {
            final String dockableUniqueId = view.getDockableUniqueId();
            view.getDockable().setView(null);
            Assertions.assertThat(contentPaneView.removeView(view)).isTrue();
            Assertions.assertThat(id_visibleView.remove(dockableUniqueId)).isNotNull();
            Assertions.assertThat(class_id_visibleView.get(view.getClass()).remove(view.getDockableUniqueId()))
                    .isNotNull();
            view.setDockable(null);
        }
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
    public boolean containsDockable(final IDockable dockable) {
        return id_visibleView.containsKey(dockable.getUniqueId());
    }

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public boolean containsView(final AView<?, ?> view) {
        return id_visibleView.containsKey(view.getDockableUniqueId());
    }

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public void reset() {
        final List<AView<?, ?>> viewsCopy = new ArrayList<>(id_visibleView.values());
        for (final AView<?, ?> view : viewsCopy) {
            removeView(view);
        }
    }

    public void showMainFrame() {
        final de.invesdwin.context.client.swing.impl.app.DelegateRichApplication application = (de.invesdwin.context.client.swing.impl.app.DelegateRichApplication) Application
                .getInstance();
        application.showMainFrame();
    }

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public void showView(final AView<?, ?> view) {
        if (containsView(view)) {
            final IDockable dockable = view.getDockable();
            dockable.requestFocus();
        } else {
            final AView<?, ?> existingView = findViewWithEqualModel(view);
            if (existingView != null) {
                view.replaceView(existingView);
                final IDockable dockable = view.getDockable();
                dockable.requestFocus();
            } else {
                addView(view);
            }
        }
    }

    private AView<?, ?> findViewWithEqualModel(final AView<?, ?> view) {
        for (final AView<?, ?> visibleView : class_id_visibleView.get(view.getClass()).values()) {
            if (Objects.equals(visibleView.getModel(), view.getModel())) {
                return visibleView;
            }
        }
        return null;
    }

    /**
     * Throws an exception if the View has already been added.
     */
    private void addView(final AView<?, ?> view) {
        Assertions.assertThat(containsView(view))
                .as("View [%s] is already being displayed.", view.getDockableUniqueId())
                .isFalse();
        final IDockable content = contentPaneView.addView(view);
        Assertions.assertThat(id_visibleView.put(content.getUniqueId(), view)).isNull();
        Assertions.assertThat(class_id_visibleView.get(view.getClass()).put(content.getUniqueId(), view)).isNull();
        view.setDockable(content);
        content.requestFocus();
    }

    public boolean isControlDown() {
        return contentPaneView.isControlDown();
    }

    public boolean isShiftDown() {
        return contentPaneView.isShiftDown();
    }

    public boolean isAltDown() {
        return contentPaneView.isAltDown();
    }

    public boolean isMetaDown() {
        return contentPaneView.isMetaDown();
    }

}
