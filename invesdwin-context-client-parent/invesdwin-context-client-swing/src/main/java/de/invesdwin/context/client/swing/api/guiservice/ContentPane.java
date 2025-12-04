package de.invesdwin.context.client.swing.api.guiservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.jdesktop.application.Application;

import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.intern.CDockable;
import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;
import de.invesdwin.context.client.swing.api.view.AModel;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.context.client.swing.api.view.IDockable;
import de.invesdwin.context.client.swing.frame.content.IWorkingAreaLocation;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.loadingcache.ALoadingCache;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.swing.EventDispatchThreadUtil;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.date.FDate;
import jakarta.inject.Inject;

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
    private de.invesdwin.context.client.swing.frame.content.ContentPaneView contentPaneView;

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
        final de.invesdwin.context.client.swing.frame.app.DelegateRichApplication application = (de.invesdwin.context.client.swing.frame.app.DelegateRichApplication) Application
                .getInstance();
        application.showMainFrame();
    }

    public void hideMainFrame() {
        final de.invesdwin.context.client.swing.frame.app.DelegateRichApplication application = (de.invesdwin.context.client.swing.frame.app.DelegateRichApplication) Application
                .getInstance();
        application.hideMainFrame();
    }

    @SuppressWarnings("unchecked")
    public <V extends AView<?, ?>> V findView(final V view) {
        if (containsView(view)) {
            return view;
        } else {
            AView<?, ?> existingView = findViewWithEqualModel(view);
            if (existingView == null) {
                //Find Placeholder-View
                final String id = view.getId();
                existingView = findView(id);
            }

            if (existingView != null) {
                if (!view.getClass().isAssignableFrom(existingView.getClass())) {
                    //placeholder view always needs to be replaced
                    return null;
                }
                return (V) existingView;
            } else {
                return null;
            }
        }
    }

    /**
     * ID can either be a dockableUniqueId or a viewId (since dockableUniqueId will inherit viewId if that is
     * specified).
     */
    public AView<?, ?> findView(final String id) {
        if (id == null) {
            return null;
        }
        return id_visibleView.get(id);
    }

    public <M extends AModel> AView<M, ?> findView(final M model) {
        for (final AView<?, ?> visibleView : id_visibleView.values()) {
            if (!model.getClass().isAssignableFrom(visibleView.getModel().getClass())) {
                continue;
            }
            if (!visibleView.isFindViewWithEqualModel()) {
                continue;
            }
            if (!Objects.equals(visibleView.getModel(), model)) {
                continue;
            }
            return null;
        }
        return null;
    }

    public <V extends AView<?, ?>> V showView(final V view, final IWorkingAreaLocation location) {
        return showView(view, location, true);
    }

    @SuppressWarnings("unchecked")
    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public <V extends AView<?, ?>> V showView(final V view, final IWorkingAreaLocation location,
            final boolean requestFocus) {
        final AView<?, ?> restoreFocusedView;
        if (!requestFocus) {
            restoreFocusedView = getFocusedView();
        } else {
            restoreFocusedView = null;
        }
        final V returnView;
        if (containsView(view)) {
            final IDockable dockable = view.getDockable();
            dockable.requestFocus();
            returnView = view;
        } else {
            AView<?, ?> existingView = findViewWithEqualModel(view);
            if (existingView == null) {
                //Find Placeholder-View
                final String id = view.getId();
                existingView = findView(id);
            }

            if (existingView != null) {
                if (isPreserveExistingView(existingView, view)) {
                    final AView<AModel, ?> cExistingView = (AView<AModel, ?>) existingView;
                    if (!isPreserveExistingModel(existingView, view, true)) {
                        cExistingView.replaceModel(view.getModel());
                    }
                    final IDockable dockable = cExistingView.getDockable();
                    dockable.requestFocus();
                    returnView = (V) existingView;
                } else if (view.replaceView(existingView)) {
                    if (isPreserveExistingModel(existingView, view, false)) {
                        final AView<AModel, ?> cView = (AView<AModel, ?>) view;
                        cView.replaceModel(existingView.getModel());
                    }
                    /*
                     * Classes will differ when we for example replace a PlaceholderView with the 'real' one. Though we
                     * always have to replace the cached view instance with the new one.
                     */
                    class_id_visibleView.get(existingView.getClass()).remove(existingView.getDockableUniqueId());
                    class_id_visibleView.get(view.getClass()).put(view.getDockableUniqueId(), view);
                    id_visibleView.put(view.getDockableUniqueId(), view);
                    final IDockable dockable = view.getDockable();
                    dockable.requestFocus();
                    returnView = view;
                } else {
                    addView(view, location);
                    returnView = view;
                }
            } else {
                addView(view, location);
                returnView = view;
            }
        }
        if (restoreFocusedView != null && restoreFocusedView.getDockable() != null) {
            EventDispatchThreadUtil.invokeLater(new Runnable() {
                @Override
                public void run() {
                    restoreFocusedView.getDockable().requestFocus();
                }
            });
        }
        return returnView;
    }

    public boolean isPreserveExistingView(final AView<?, ?> existingView, final AView<?, ?> view) {
        if (existingView == null) {
            return false;
        }
        if (view == null) {
            return true;
        }
        if (!view.getClass().isAssignableFrom(existingView.getClass())) {
            //placeholder view always needs to be replaced
            return false;
        }
        if (!view.isPreserveExistingView(existingView)) {
            return false;
        }
        if (view.isFindViewWithEqualModel()) {
            final AModel existingModel = existingView.getModel();
            final AModel model = view.getModel();
            if (Objects.equals(existingModel, model)) {
                return true;
            }
        }
        if (Objects.equals(existingView.getId(), view.getId())) {
            return true;
        }
        return false;
    }

    public boolean isPreserveExistingModel(final AView<?, ?> existingView, final AView<?, ?> view,
            final boolean existingViewPreserved) {
        if (existingView == null) {
            return false;
        }
        if (view == null) {
            return true;
        }
        if (existingView.getModel() == null) {
            //placeholder view model always needs to be replaced
            return false;
        }
        if (view.getModel() == null) {
            return true;
        }
        if (!view.getClass().isAssignableFrom(existingView.getClass())) {
            //placeholder view model always needs to be replaced
            return false;
        }
        final AModel existingModel = existingView.getModel();
        final AModel model = view.getModel();
        if (!model.getClass().isAssignableFrom(existingModel.getClass())) {
            //placeholder view model always needs to be replaced
            return false;
        }
        if (!view.isPreserveExistingModel(existingView, existingViewPreserved)) {
            return false;
        }
        if (existingModel == existingView) {
            //views that have themselves as the model can not get the model replaced
            return false;
        }
        if (view.isFindViewWithEqualModel()) {
            if (Objects.equals(existingModel, model)) {
                return true;
            }
        }
        if (Objects.equals(existingView.getId(), view.getId())) {
            return true;
        }
        return false;
    }

    public AView<?, ?> getFocusedView() {
        final SingleCDockable focusedDockable = (SingleCDockable) contentPaneView.getFocusedDockable();
        if (focusedDockable == null) {
            //Can be null on startup when we initialize PlaceholderView's
            return null;
        }

        final AView<?, ?> focusedView = id_visibleView.get(focusedDockable.getUniqueId());
        return focusedView;
    }

    private AView<?, ?> findViewWithEqualModel(final AView<?, ?> view) {
        if (!view.isFindViewWithEqualModel()) {
            return null;
        }
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
    private void addView(final AView<?, ?> view, final IWorkingAreaLocation location) {
        Assertions.checkFalse(containsView(view), "View [%s] is already being displayed.", view.getDockableUniqueId());
        final IDockable content = contentPaneView.addView(view, location);
        Assertions.checkNull(id_visibleView.put(content.getUniqueId(), view));
        Assertions.checkNull(class_id_visibleView.get(view.getClass()).put(content.getUniqueId(), view));
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

    public boolean isAltGraphDown() {
        return contentPaneView.isAltGraphDown();
    }

    public boolean isMetaDown() {
        return contentPaneView.isMetaDown();
    }

    public boolean isModifierDown() {
        return contentPaneView.isModifierDown();
    }

    public FDate getLastMouseClickTime() {
        return contentPaneView.getLastMouseClickTime();
    }

    public Instant getLastMouseClickInstant() {
        return contentPaneView.getLastMouseClickInstant();
    }

}
