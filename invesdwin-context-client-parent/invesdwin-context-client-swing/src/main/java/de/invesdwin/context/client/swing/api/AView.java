package de.invesdwin.context.client.swing.api;

import java.util.concurrent.Callable;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.swing.Icon;
import javax.swing.JComponent;

import org.jdesktop.beansbinding.BindingGroup;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.client.swing.ContentPane;
import de.invesdwin.context.client.swing.api.internal.GeneratedBinding;
import de.invesdwin.context.client.swing.api.internal.ViewIdGenerator;
import de.invesdwin.context.client.swing.util.ComponentStandardizer;
import de.invesdwin.norva.beanpath.annotation.Hidden;
import de.invesdwin.util.assertions.Assertions;

@ThreadSafe
public abstract class AView<M extends AModel, C extends JComponent> extends AModel {

    public static final String VIEW_DESCRIPTION_KEY = "View.description";
    public static final String VIEW_ICON_KEY = "View.icon";
    public static final String VIEW_TITLE_KEY = "View.title";

    private final String id;

    private final M model;
    private final Object componentLock = new Object();
    @GuardedBy("componentLock")
    private C component;
    @GuardedBy("componentLock")
    private BindingGroup bindingGroup;
    private final Object dockableLock = new Object();
    @GuardedBy("dockableLock")
    private DockableContent dockable;

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
                            return component;
                        }
                    });
                    if (getModel() != null) {
                        final C componentCopy = component;
                        bindingGroup = EventDispatchThreadUtil.invokeAndWait(new Callable<BindingGroup>() {
                            @Override
                            public BindingGroup call() throws Exception {
                                return initBindingGroup(componentCopy);
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
    protected BindingGroup initBindingGroup(final C component) {
        return new GeneratedBinding(getModel(), component).initBindingGroup();
    }

    @Hidden(skip = true)
    public BindingGroup getBindingGroup() {
        synchronized (componentLock) {
            return bindingGroup;
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
                Assertions.assertThat(dockable.getId()).isEqualTo(getId());
                Assertions.assertThat(dockable.getComponent()).isSameAs(getComponent());
                Assertions.assertThat(contentPane.containsView(this))
                        .as("ContentPane is not synchronous to the content in the View. The View is missing there despite the content being set here.")
                        .isTrue();
                this.dockable = dockable;
                onOpen();
            } else {
                Assertions.assertThat(dockable).as("A View instance can only be made visible once.").isNull();
                Assertions.assertThat(contentPane.containsView(this))
                        .as("ContentPane is not synchronous to the content in the View. The View still exists there, despite the content being removed from here.")
                        .isFalse();
                this.dockable = null;
                onClose();
            }
        }
    }

    @Hidden(skip = true)
    protected void onOpen() {}

    @Hidden(skip = true)
    protected void onClose() {}

}
