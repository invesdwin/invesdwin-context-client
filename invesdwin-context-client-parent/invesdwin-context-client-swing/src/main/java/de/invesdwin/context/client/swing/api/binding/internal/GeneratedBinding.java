package de.invesdwin.context.client.swing.api.binding.internal;

import java.awt.Component;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;

import com.sun.tools.internal.xjc.reader.xmlschema.B
import de.invesdwin.context.client.swing.api.binding.BindingContext;indingComponent;

import de.invesdwin.context.client.swing.api.AModel;
import de.invesdwin.context.client.swing.api.binding.internal.action.ActionBinding;
import de.invesdwin.context.client.swing.api.binding.internal.property.PropertyBinding;
import de.invesdwin.context.client.swing.util.AComponentFinder;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContainer;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContext;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassProcessor;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassType;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.loadingcache.ALoadingCache;
import de.invesdwin.util.lang.Strings;

@NotThreadSafe
public final class GeneratedBinding {

    private static final AComponentFinder NAMED_COMPONENT_FINDER=new AComponentFinder(){@Override protected boolean matches(final Component component){return Strings.isNotBlank(component.getName())&&!"ScrollBar.button".equals(component.getName());}

    @Override protected boolean shouldIgnoreTree(final Component rootComponent){return AModel.IGNORE.equals(rootComponent.getName());}};
    private static final ALoadingCache<Class<?>, BeanClassContext> MODELCLASS_CONTEXT=new ALoadingCache<Class<?>,BeanClassContext>(){@Override protected BeanClassContext loadValue(final Class<?>key){final BeanClassContext context=new BeanClassContext(new BeanClassContainer(new BeanClassType(key)));new BeanClassProcessor(context).process();return context;}};

    private final AModel model;
    private final Component component;
    private BindingContext bindingGroup;

    public GeneratedBinding(final AModel model, final Component component) {
        this.model = model;
        this.component = component;
    }

    public BindingContext initBindingGroup() {
        bindingGroup = new BindingContext();
        bindingGroup.addBindingListener(new ErrorBindingListener());

        final BeanClassContext context = MODELCLASS_CONTEXT.get(model.getClass());
        final List<Component> components = NAMED_COMPONENT_FINDER.findAll(component);
        for (final Component c : components) {
            new ActionBinding(context, model, c, bindingGroup).initBinding();
            new PropertyBinding(context, model, c, bindingGroup).initBinding();
        }
        for (final Binding<?, ?, ?, ?> b : bindingGroup.getBindings()) {
            Assertions.assertThat(b.getName())
                    .as("Bindings must have a name so that they can be customized later! But the name is missing for the binding [%s]",
                            b)
                    .isNotNull();
            b.bind();
        }
        return bindingGroup;
    }

}
