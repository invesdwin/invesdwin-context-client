package de.invesdwin.context.client.swing.api.view;

import java.util.concurrent.Callable;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.swing.Icon;
import javax.swing.JComponent;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.GeneratedBindingGroup;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.api.view.listener.BroadcastingViewListener;
import de.invesdwin.context.client.swing.api.view.listener.IViewListener;
import de.invesdwin.context.client.swing.util.AViewVisitor;
import de.invesdwin.context.client.swing.util.ComponentStandardizer;
import de.invesdwin.context.client.swing.util.Views;
import de.invesdwin.norva.beanpath.annotation.Hidden;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContext;
import de.invesdwin.norva.beanpath.spi.element.IBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.RootBeanPathElement;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.fast.IFastIterable;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.swing.EventDispatchThreadUtil;

@ThreadSafe
public abstract class AView<M extends AModel, C extends JComponent> extends AModel {

    public static final String CLIENTPROP_VIEW_INSTANCE = "VIEW_INSTANCE";
    public static final String KEY_VIEW_DESCRIPTION = "View.description";
    public static final String KEY_VIEW_ICON = "View.icon";
    public static final String KEY_VIEW_TITLE = "View.title";

    private final Object modelLock = new Object();
    @GuardedBy("modelLock")
    private M model;
    private final Object componentLock = new Object();
    @GuardedBy("componentLock")
    private C component;
    @GuardedBy("componentLock")
    private BindingGroup bindingGroup;
    private final Object dockableLock = new Object();
    @GuardedBy("dockableLock")
    private IDockable dockable;
    @GuardedBy("dockableLock")
    private BroadcastingViewListener broadcastingViewListener;

    @SuppressWarnings("unchecked")
    public AView() {
        this.model = (M) this;
    }

    public AView(final M model) {
        this.model = model;
    }

    /**
     * When this id is defined, the dockable will use it as DockableUniqueId to allow saving and restoring the layout
     * for this view. Otherwise a unique id is generated which does not allow this feature to be used.
     */
    public String getId() {
        return null;
    }

    public String getDockableUniqueId() {
        synchronized (dockableLock) {
            if (dockable != null) {
                return dockable.getUniqueId();
            } else {
                return null;
            }
        }
    }

    @Hidden(skip = true)
    public M getModel() {
        synchronized (modelLock) {
            return model;
        }
    }

    @Hidden(skip = true)
    public void setModel(final M model) {
        synchronized (modelLock) {
            this.model = model;
        }
    }

    @Hidden(skip = true)
    public C getComponent() {
        if (component == null) {
            synchronized (componentLock) {
                if (component == null) {
                    try {
                        component = EventDispatchThreadUtil.invokeAndWait(new Callable<C>() {
                            @Override
                            public C call() throws Exception {
                                final C component = initComponent();
                                new ComponentStandardizer().visitAll(component);
                                getResourceMap().injectComponents(component);
                                component.putClientProperty(CLIENTPROP_VIEW_INSTANCE, AView.this);
                                return component;
                            }
                        });
                    } catch (final InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (getModel() != null) {
                        bindingGroup = initBindingGroup(component);
                    }
                }
            }
        }
        return component;
    }

    @Hidden(skip = true)
    protected abstract C initComponent();

    @Hidden(skip = true)
    protected BindingGroup initBindingGroup(final C component) {
        return new GeneratedBindingGroup(this, component).bind();
    }

    @Hidden(skip = true)
    public BindingGroup getBindingGroup() {
        if (component == null) {
            synchronized (componentLock) {
                if (component == null) {
                    Assertions.checkNotNull(getComponent());
                }
            }
        }
        return bindingGroup;
    }

    @Hidden(skip = true)
    public String getTitle() {
        //try norva title element if available
        String visibleName = null;
        final BindingGroup bindingGroup = getBindingGroup();
        if (bindingGroup != null) {
            final BeanClassContext modelContext = bindingGroup.getModelContext();
            if (modelContext != null) {
                final IBeanPathElement rootElement = modelContext.getElementRegistry()
                        .getElement(RootBeanPathElement.ROOT_BEAN_PATH);
                final String title = rootElement.getTitleFromRoot(bindingGroup.getModel());
                if (!Objects.equals(title, rootElement.getVisibleName())) {
                    return bindingGroup.i18n(title);
                } else {
                    visibleName = title;
                }
            }
        }

        //fallback to static title from properties file
        final String classname = getClass().getSimpleName();
        final String title = GuiService.i18n(this, KEY_VIEW_TITLE, classname);
        if (Objects.equals(title, classname)) {
            return visibleName;
        } else {
            return title;
        }
    }

    public Icon getIcon() {
        return getResourceMap().getIcon(KEY_VIEW_ICON);
    }

    /**
     * The ToolTipText.
     */
    @Hidden(skip = true)
    public String getDescription() {
        return getResourceMap().getString(KEY_VIEW_DESCRIPTION);
    }

    @Hidden(skip = true)
    public IDockable getDockable() {
        synchronized (dockableLock) {
            return dockable;
        }
    }

    /**
     * This method may only be called by the ContentPane class.
     */
    public void setDockable(final IDockable dockable) {
        synchronized (dockableLock) {
            if (this.dockable == null) {
                Assertions.assertThat(dockable.getComponent()).isSameAs(getComponent());
                this.dockable = dockable;
                dockable.setView(this);
                new AViewVisitor() {
                    @Override
                    protected void visit(final AView<?, ?> view) {
                        view.triggerOnOpen();
                    }
                }.visitAll(Views.getRootComponentInDockable(getComponent()));
            } else {
                Assertions.assertThat(dockable).as("A View instance can only be made visible once.").isNull();
                this.dockable.setView(null);
                this.dockable = null;
                new AViewVisitor() {

                    @Override
                    protected void visit(final AView<?, ?> view) {
                        view.triggerOnClose();
                    }

                }.visitAll(Views.getRootComponentInDockable(getComponent()));
            }
        }
    }

    public void replaceView(final AView<?, ?> existingView) {
        synchronized (dockableLock) {
            synchronized (existingView.dockableLock) {
                //move dockable
                this.dockable = existingView.dockable;
                existingView.dockable = null;
                //replace dockable content
                if (this.dockable != null) {
                    this.dockable.setView(this);
                    this.dockable.setComponent(getComponent());
                }
                //close existing view
                new AViewVisitor() {
                    @Override
                    protected void visit(final AView<?, ?> view) {
                        view.triggerOnClose();
                    }
                }.visitAll(Views.getRootComponentInDockable(existingView.getComponent()));
                //open new view
                new AViewVisitor() {
                    @Override
                    protected void visit(final AView<?, ?> view) {
                        view.triggerOnOpen();
                    }
                }.visitAll(Views.getRootComponentInDockable(getComponent()));
            }
        }
    }

    private BroadcastingViewListener getBroadcastingViewListener() {
        synchronized (dockableLock) {
            if (broadcastingViewListener == null) {
                broadcastingViewListener = new BroadcastingViewListener();
            }
            return broadcastingViewListener;
        }
    }

    public boolean registerViewListener(final IViewListener l) {
        return getBroadcastingViewListener().registerListener(l);
    }

    public boolean unregisterViewListener(final IViewListener l) {
        synchronized (dockableLock) {
            if (broadcastingViewListener == null) {
                return false;
            } else {
                return broadcastingViewListener.unregisterListener(l);
            }
        }
    }

    public IFastIterable<IViewListener> getViewListeners() {
        return broadcastingViewListener.getListeners();
    }

    private void triggerOnOpen() {
        onOpen();
        if (broadcastingViewListener != null) {
            broadcastingViewListener.onOpen();
        }
    }

    private void triggerOnClose() {
        onClose();
        if (broadcastingViewListener != null) {
            broadcastingViewListener.onClose();
        }
    }

    @Hidden(skip = true)
    public final void triggerOnShowing() {
        onShowing();
        if (broadcastingViewListener != null) {
            broadcastingViewListener.onShowing();
        }
    }

    /**
     * Gets called on initial opening.
     */
    @Hidden(skip = true)
    protected void onOpen() {}

    /**
     * Gets called when view is removed.
     */
    @Hidden(skip = true)
    protected void onClose() {}

    /**
     * Gets called on initial show and when focus is received.
     */
    @Hidden(skip = true)
    protected void onShowing() {}

}
