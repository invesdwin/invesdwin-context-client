package de.invesdwin.context.client.swing.api.menu;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenuItem;

import org.jdesktop.application.ResourceMap;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;

import com.jgoodies.common.base.Strings;

import de.invesdwin.context.client.swing.api.binding.GeneratedBindingGroup;
import de.invesdwin.context.client.swing.api.binding.component.button.ISubmitButtonExceptionHandler;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.norva.beanpath.BeanPathObjects;
import de.invesdwin.norva.beanpath.annotation.Title;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.lang.Reflections;
import de.invesdwin.util.swing.Components;

/**
 * First tries to find the view in the Spring ApplicationContext. If this fails, the View gets instantiated by the
 * default constructor.
 * 
 * @author subes
 */
@NotThreadSafe
@Configurable
public class ShowModalViewMenuItem extends JMenuItem {

    private boolean caching = false;

    private final Class<? extends AView<?, ?>> viewClass;
    private final Dimension dimension;
    private AView<?, ?> cachedViewInstance;

    @Inject
    private ApplicationContext appCtx;

    public ShowModalViewMenuItem(final Class<? extends AView<?, ?>> viewClass, final Dimension dimension) {
        super();
        this.viewClass = viewClass;
        this.dimension = dimension;
        initialize();
    }

    private void initialize() {
        final ResourceMap resourceMap = GuiService.get().getResourceMap(viewClass);
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
        Components.setToolTipText(this, resourceMap.getString(AView.KEY_VIEW_DESCRIPTION), false);
        final Icon viewIcon = resourceMap.getIcon(AView.KEY_VIEW_ICON);
        setAction(new AbstractAction(viewTitle, viewIcon) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    if (caching) {
                        if (cachedViewInstance == null) {
                            cachedViewInstance = createView();
                        }
                        GuiService.get().showModalView(cachedViewInstance, dimension);
                    } else {
                        GuiService.get().showModalView(createView(), dimension);
                    }
                } catch (final Throwable t) {
                    newSubmitButtonExceptionHandler().handleSubmitButtonException(ShowModalViewMenuItem.this, t);
                }
            }

        });
    }

    protected ISubmitButtonExceptionHandler newSubmitButtonExceptionHandler() {
        return GeneratedBindingGroup.newDefaultSubmitButtonExceptionHandler();
    }

    public ShowModalViewMenuItem withCaching() {
        return withCaching(true);
    }

    public ShowModalViewMenuItem withCaching(final boolean caching) {
        this.caching = caching;
        return this;
    }

    public boolean isCaching() {
        return caching;
    }

    protected AView<?, ?> createView() {
        final AView<?, ?> viewBean = appCtx.getBean(viewClass);
        if (viewBean != null) {
            return viewBean;
        }
        final AView<?, ?> viewInstance = Reflections.constructor().in(viewClass).newInstance();
        Assertions.assertThat(viewInstance).isNotNull();
        return viewInstance;
    }

}
