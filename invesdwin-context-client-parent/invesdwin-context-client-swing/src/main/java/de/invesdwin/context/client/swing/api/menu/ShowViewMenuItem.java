package de.invesdwin.context.client.swing.api.menu;

import java.awt.event.ActionEvent;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenuItem;

import org.jdesktop.application.ResourceMap;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;

import de.invesdwin.context.beans.init.ApplicationContexts;
import de.invesdwin.context.client.swing.api.binding.GeneratedBindingGroup;
import de.invesdwin.context.client.swing.api.binding.component.button.ISubmitButtonExceptionHandler;
import de.invesdwin.context.client.swing.api.guiservice.ContentPane;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.context.client.swing.frame.content.IWorkingAreaLocation;
import de.invesdwin.norva.beanpath.BeanPathObjects;
import de.invesdwin.norva.beanpath.annotation.Title;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.lang.reflection.Reflections;
import de.invesdwin.util.lang.string.Strings;
import de.invesdwin.util.swing.Components;

/**
 * First tries to find the view in the Spring ApplicationContext. If this fails, the View gets instantiated by the
 * default constructor.
 * 
 * @author subes
 */
@NotThreadSafe
@Configurable
public class ShowViewMenuItem extends JMenuItem {

    private boolean caching = false;

    private final Class<? extends AView<?, ?>> viewClass;
    private final IWorkingAreaLocation location;
    private AView<?, ?> cachedViewInstance;

    @Inject
    private ApplicationContext appCtx;
    @Inject
    private ContentPane contentPane;

    public ShowViewMenuItem(final Class<? extends AView<?, ?>> viewClass, final IWorkingAreaLocation location) {
        this.viewClass = viewClass;
        this.location = location;
        initialize();
    }

    @SuppressWarnings("unchecked")
    public ShowViewMenuItem(final AView<?, ?> viewInstance, final IWorkingAreaLocation location) {
        this.viewClass = (Class<? extends AView<?, ?>>) viewInstance.getClass();
        this.cachedViewInstance = viewInstance;
        this.caching = true;
        this.location = location;
        initialize();
    }

    private void initialize() {
        final ResourceMap resourceMap = GuiService.get().getResourceMap(viewClass);
        final String title = newTitle(resourceMap);
        final String description = newDescription(resourceMap);
        Components.setToolTipText(this, description, false);
        final Icon icon = newIcon(resourceMap);
        setAction(new AbstractAction(title, icon) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    if (caching) {
                        if (cachedViewInstance == null) {
                            cachedViewInstance = createView();
                        }
                        contentPane.showView(cachedViewInstance, location);
                    } else {
                        contentPane.showView(createView(), location);
                    }
                } catch (final Throwable t) {
                    newSubmitButtonExceptionHandler().handleSubmitButtonException(ShowViewMenuItem.this, t);
                }
            }
        });
    }

    protected Icon newIcon(final ResourceMap resourceMap) {
        if (cachedViewInstance != null) {
            return cachedViewInstance.getIcon();
        }
        return resourceMap.getIcon(AView.KEY_VIEW_ICON);
    }

    protected String newDescription(final ResourceMap resourceMap) {
        if (cachedViewInstance != null) {
            return cachedViewInstance.getDescription();
        }
        return resourceMap.getString(AView.KEY_VIEW_DESCRIPTION);
    }

    protected String newTitle(final ResourceMap resourceMap) {
        if (cachedViewInstance != null) {
            return cachedViewInstance.getTitle();
        }
        String viewTitle = resourceMap.getString(AView.KEY_VIEW_TITLE);
        if (Strings.isBlank(viewTitle)) {
            final Class<?>[] generics = Reflections.resolveTypeArguments(viewClass, AView.class);
            final Class<?> modelClass = generics[0];
            Title titleAnnotation = Reflections.getAnnotation(modelClass, Title.class);
            if (titleAnnotation == null) {
                titleAnnotation = Reflections.getAnnotation(viewClass, Title.class);
            }
            if (titleAnnotation != null) {
                viewTitle = GuiService.i18n(resourceMap, titleAnnotation.value());
            } else {
                final String viewClassName = viewClass.getSimpleName();
                viewTitle = GuiService.i18n(resourceMap, viewClassName);
                if (Objects.equals(viewTitle, viewClassName)) {
                    viewTitle = BeanPathObjects.toVisibleName(modelClass.getSimpleName());
                    //might happen with anonymous classes
                    if (Strings.isBlank(viewTitle) || modelClass == Object.class) {
                        viewTitle = BeanPathObjects.toVisibleName(viewClassName);
                    }
                }
            }
        }
        return viewTitle;
    }

    protected ISubmitButtonExceptionHandler newSubmitButtonExceptionHandler() {
        return GeneratedBindingGroup.newDefaultSubmitButtonExceptionHandler();
    }

    public ShowViewMenuItem setCaching() {
        return setCaching(true);
    }

    public ShowViewMenuItem setCaching(final boolean caching) {
        if (cachedViewInstance != null) {
            //ignore change
            return this;
        }
        this.caching = caching;
        return this;
    }

    public boolean isCaching() {
        return caching;
    }

    protected AView<?, ?> createView() {
        final AView<?, ?> viewBean = ApplicationContexts.getBeanIfExists(appCtx, viewClass);
        if (viewBean != null) {
            return viewBean;
        }
        final AView<?, ?> viewInstance = Reflections.constructor().in(viewClass).newInstance();
        Assertions.assertThat(viewInstance).isNotNull();
        return viewInstance;
    }

}
