package de.invesdwin.context.client.swing.api.binding.internal;

import java.awt.Component;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.swing.api.AModel;
import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.internal.action.ActionBinding;
import de.invesdwin.context.client.swing.api.binding.internal.property.PropertyBinding;
import de.invesdwin.context.client.swing.util.AComponentFinder;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContainer;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContext;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassProcessor;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassType;
import de.invesdwin.util.collections.loadingcache.ALoadingCache;
import de.invesdwin.util.lang.Strings;

@NotThreadSafe
public final class GeneratedBinding {

    private static final AComponentFinder NAMED_COMPONENT_FINDER = new AComponentFinder() {
        @Override
        protected boolean matches(final Component component) {
            return Strings.isNotBlank(component.getName()) && !"ScrollBar.button".equals(component.getName());
        }

        @Override
        protected boolean shouldIgnoreTree(final Component rootComponent) {
            return AModel.IGNORE.equals(rootComponent.getName());
        }
    };
    private static final ALoadingCache<Class<?>, BeanClassContext> MODELCLASS_CONTEXT = new ALoadingCache<Class<?>, BeanClassContext>() {
        @Override
        protected BeanClassContext loadValue(final Class<?> key) {
            final BeanClassContext context = new BeanClassContext(new BeanClassContainer(new BeanClassType(key)));
            new BeanClassProcessor(context).process();
            return context;
        }
    };

    private final AModel model;
    private final Component component;
    private BindingGroup bindingGroup;

    public GeneratedBinding(final AModel model, final Component component) {
        this.model = model;
        this.component = component;
    }

    public BindingGroup initBindingGroup() {
        bindingGroup = new BindingGroup(MODELCLASS_CONTEXT.get(model.getClass()));

        final List<Component> components = NAMED_COMPONENT_FINDER.findAll(component);
        for (final Component c : components) {
            new ActionBinding(model, c, bindingGroup).initBinding();
            new PropertyBinding(model, c, bindingGroup).initBinding();
        }
        return bindingGroup;
    }

}
