package de.invesdwin.context.client.swing.api.menu;

import java.awt.event.ActionEvent;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import org.jdesktop.application.ResourceMap;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;

import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.api.guiservice.ContentPane;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.Reflections;

/**
 * First tries to find the view in the Spring ApplicationContext. If this fails, the View gets instantiated by the
 * default constructor.
 * 
 * @author subes
 */
@NotThreadSafe
@Configurable
public class OpenViewMenuItem<V extends AView<?, ?>> extends JMenuItem {

    private boolean cachingEnabled = true;

    private final Class<V> viewClass;
    private V cachedViewInstance;

    @Inject
    private ApplicationContext appCtx;
    @Inject
    private ContentPane contentPane;

    public OpenViewMenuItem(final Class<V> viewClass) {
        super();
        this.viewClass = viewClass;
        initialize();
    }

    private void initialize() {
        final ResourceMap resourceMap = GuiService.get().getResourceMap(viewClass);
        setText(resourceMap.getString(AView.VIEW_TITLE_KEY));
        setToolTipText(resourceMap.getString(AView.VIEW_DESCRIPTION_KEY));
        setIcon(resourceMap.getIcon(AView.VIEW_ICON_KEY));
        setAction(new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (cachingEnabled) {
                    if (cachedViewInstance == null) {
                        cachedViewInstance = createView();
                    }
                    contentPane.showView(cachedViewInstance);
                } else {
                    contentPane.showView(createView());
                }
            }
        });
    }

    public OpenViewMenuItem<V> withCachingEnabled(final boolean cachingEnabled) {
        this.cachingEnabled = cachingEnabled;
        return this;
    }

    public boolean isCachingEnabled() {
        return cachingEnabled;
    }

    protected V createView() {
        final V viewBean = appCtx.getBean(viewClass);
        if (viewBean != null) {
            return viewBean;
        }
        final V viewInstance = Reflections.constructor().in(viewClass).newInstance();
        Assertions.assertThat(viewInstance).isNotNull();
        return viewInstance;
    }

}
