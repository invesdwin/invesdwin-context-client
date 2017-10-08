package de.invesdwin.context.client.swing.api;

import java.util.concurrent.Callable;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.swing.Icon;
import javax.swing.JComponent;

import org.jdesktop.beansbinding.BindingGroup;
import org.noos.xing.mydoggy.Content;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.client.swing.ContentPane;
import de.invesdwin.context.client.swing.api.internal.GeneratedBinding;
import de.invesdwin.context.client.swing.api.internal.ViewIdGenerator;
import de.invesdwin.context.client.swing.util.ComponentStandardizer;
import de.invesdwin.util.assertions.Assertions;

// TODO exitlisteners
@SuppressWarnings("serial")
@ThreadSafe
public abstract class AView<M extends AModel, C extends JComponent> extends AModel {

    public static final String VIEW_DESCRIPTION_KEY = "View.description";
    public static final String VIEW_ICON_KEY = "View.icon";
    public static final String VIEW_TITLE_KEY = "View.title";

    private final String id;

    private final Object modelLock = new Object();
    @GuardedBy("modelLock")
    private M model;
    private final Object componentLock = new Object();
    @GuardedBy("componentLock")
    private C component;
    @GuardedBy("componentLock")
    private BindingGroup bindingGroup;
    private final Object contentLock = new Object();
    @GuardedBy("contentLock")
    private Content content;

    public AView() {
        id = ViewIdGenerator.newId(this);
    }

    public M getModel() {
        synchronized (modelLock) {
            if (model == null) {
                model = initModel();
            }
            return model;
        }
    }

    protected abstract M initModel();

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

    protected abstract C initComponent();

    protected BindingGroup initBindingGroup(final C component) {
        return new GeneratedBinding(getModel(), component).initBindingGroup();
    }

    public BindingGroup getBindingGroup() {
        synchronized (componentLock) {
            return bindingGroup;
        }
    }

    /**
     * The ID is unique for every instance of this View.
     */
    public String getId() {
        return id;
    }

    public String getTitle() {
        final String title = getResourceMap().getString(VIEW_TITLE_KEY);
        if (title == null) {
            return getId();
        } else {
            return title;
        }
    }

    public Icon getIcon() {
        return getResourceMap().getIcon(VIEW_ICON_KEY);
    }

    /**
     * The ToolTipText.
     */
    public String getDescription() {
        return getResourceMap().getString(VIEW_DESCRIPTION_KEY);
    }

    public Content getContent() {
        synchronized (contentLock) {
            return content;
        }
    }

    /**
     * This method may only be called by the ContentPane class.
     */
    public void setContent(final ContentPane contentPane, final Content content) {
        synchronized (contentLock) {
            if (this.content == null) {
                Assertions.assertThat(content.getId()).isEqualTo(getId());
                Assertions.assertThat(content.getComponent()).isSameAs(getComponent());
                Assertions.assertThat(contentPane.containsView(this))
                        .as("ContentPane is not synchronous to the content in the View. The View is missing there despite the content being set here.")
                        .isTrue();
            } else {
                Assertions.assertThat(content).as("A View instance can only be made visible once.").isNull();
                Assertions.assertThat(contentPane.containsView(this))
                        .as("ContentPane is not synchronous to the content in the View. The View still exists there, despite the content being removed from here.")
                        .isFalse();
            }
            this.content = content;
        }
    }

}
