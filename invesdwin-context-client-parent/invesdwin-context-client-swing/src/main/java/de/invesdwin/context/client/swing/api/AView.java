package de.invesdwin.context.client.swing.api;

import java.awt.Container;
import java.awt.event.ContainerListener;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.swing.Icon;
import javax.swing.JComponent;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.client.swing.api.binding.BindingContext;
import de.invesdwin.context.client.swing.api.binding.internal.GeneratedBinding;
import de.invesdwin.context.client.swing.api.binding.internal.ViewIdGenerator;
import de.invesdwin.context.client.swing.api.guiservice.ContentPane;
import de.invesdwin.context.client.swing.listener.ContainerListenerSupport;
import de.invesdwin.context.client.swing.util.ComponentStandardizer;
import de.invesdwin.norva.beanpath.annotation.Hidden;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.fast.concurrent.SynchronizedSet;

@ThreadSafe
public abstract class AView<M extends AModel, C extends JComponent> extends AModel implements IDockableListener {

    public static final String VIEW_DESCRIPTION_KEY = "View.description";
    public static final String VIEW_ICON_KEY = "View.icon";
    public static final String VIEW_TITLE_KEY = "View.title";

    private final String id;

    private final M model;
    private final Object componentLock = new Object();
    @GuardedBy("componentLock")
    private C component;
    @GuardedBy("componentLock")
    private BindingContext binding;
    private final Object dockableLock = new Object();
    @GuardedBy("dockableLock")
    private DockableContent dockable;
    @GuardedBy("dockableLock")
    private Set<IDockableListener> dockableListeners;

    @SuppressWarnings("unchecked")
    public AView() {
        id = ViewIdGenerator.newId(this);
        this.model = (M) this;
    }

    public AView(final M model) {
        id = ViewIdGenerator.newId(this);
        this.model = model;
    }

    @Hidden(skip = true)
    public M getModel() {
        return model;
    }

    @Hidden(skip = true)
    public C getComponent() {
        synchronized (componentLock) {
            if (component == null) {
                try {
                    component = EventDispatchThreadUtil.invokeAndWait(new Callable<C>() {
                        @Override
                        public C call() throws Exception {
                            final C component = initComponent();
                            new ComponentStandardizer(component).standardize();
                            getResourceMap().injectComponents(component);
                            component.addContainerListener(new ViewAttachingContainerListener(AView.this));
                            return component;
                        }
                    });
                    if (getModel() != null) {
                        final C componentCopy = component;
                        binding = EventDispatchThreadUtil.invokeAndWait(new Callable<BindingContext>() {
                            @Override
                            public BindingContext call() throws Exception {
                                return initBinding(componentCopy);
                            }
                        });
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return component;
        }
    }

    @Hidden(skip = true)
    protected abstract C initComponent();

    @Hidden(skip = true)
    protected BindingContext initBinding(final C component) {
        return new GeneratedBinding(getModel(), component).initBindingGroup();
    }

    @Hidden(skip = true)
    public BindingContext getBindingGroup() {
        synchronized (componentLock) {
            return binding;
        }
    }

    /**
     * The ID is unique for every instance of this View.
     */
    @Hidden(skip = true)
    public String getId() {
        return id;
    }

    @Hidden(skip = true)
    public String getTitle() {
        final String title = getResourceMap().getString(VIEW_TITLE_KEY);
        if (title == null) {
            return getId();
        } else {
            return title;
        }
    }

    @Hidden(skip = true)
    public Icon getIcon() {
        return getResourceMap().getIcon(VIEW_ICON_KEY);
    }

    /**
     * The ToolTipText.
     */
    @Hidden(skip = true)
    public String getDescription() {
        return getResourceMap().getString(VIEW_DESCRIPTION_KEY);
    }

    @Hidden(skip = true)
    public DockableContent getDockable() {
        synchronized (dockableLock) {
            return dockable;
        }
    }

    /**
     * This method may only be called by the ContentPane class.
     */
    public void setDockable(final ContentPane contentPane, final DockableContent dockable) {
        synchronized (dockableLock) {
            if (this.dockable == null) {
                Assertions.assertThat(dockable.getUniqueId()).isEqualTo(getId());
                Assertions.assertThat(dockable.getComponent()).isSameAs(getComponent());
                Assertions.assertThat(contentPane.containsView(this))
                        .as("ContentPane is not synchronous to the content in the View. The View is missing there despite the content being set here.")
                        .isTrue();
                this.dockable = dockable;
                onOpen();
                if (dockableListeners != null) {
                    for (final IDockableListener l : dockableListeners) {
                        l.onOpen();
                    }
                }
            } else {
                Assertions.assertThat(dockable).as("A View instance can only be made visible once.").isNull();
                Assertions.assertThat(contentPane.containsView(this))
                        .as("ContentPane is not synchronous to the content in the View. The View still exists there, despite the content being removed from here.")
                        .isFalse();
                this.dockable = null;
                onClose();
                if (dockableListeners != null) {
                    for (final IDockableListener l : dockableListeners) {
                        l.onClose();
                    }
                }
            }
        }
    }

    @Hidden(skip = true)
    public Set<IDockableListener> getDockableListeners() {
        synchronized (dockableLock) {
            if (dockableListeners == null) {
                dockableListeners = new SynchronizedSet<>(new LinkedHashSet<>(), dockableLock);
            }
            return dockableListeners;
        }
    }

    @Override
    @Hidden(skip = true)
    public void onOpen() {}

    @Override
    @Hidden(skip = true)
    public void onClose() {}

    private static final class ViewAttachingContainerListener extends ContainerListenerSupport {

        private final AView<?, ?> view;

        private ViewAttachingContainerListener(final AView<?, ?> view) {
            this.view = view;
        }

        public AView<?, ?> getView() {
            return view;
        }

    }

    public static AView<?, ?> findParentView(final Container container) {
        return findParentView(container, null);
    }

    @SuppressWarnings("unchecked")
    public static <T extends AView<?, ?>> T findParentView(final Container container, final Class<T> type) {
        Container parent = container;
        while (parent != null) {
            final AView<?, ?> view = getViewAt(parent);
            if (view != null && (type == null || type.isAssignableFrom(view.getClass()))) {
                return (T) view;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private static AView<?, ?> getViewAt(final Container container) {
        final ContainerListener[] containerListeners = container.getContainerListeners();
        for (int i = 0; i < containerListeners.length; i++) {
            final ContainerListener l = containerListeners[i];
            if (l instanceof ViewAttachingContainerListener) {
                final ViewAttachingContainerListener viewL = (ViewAttachingContainerListener) l;
                final AView<?, ?> view = viewL.getView();
                return view;
            }
        }
        return null;
    }

}
