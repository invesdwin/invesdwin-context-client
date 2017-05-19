package de.invesdwin.common.client.swing.api.internal;

import java.awt.Component;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;

import de.invesdwin.common.client.swing.api.AModel;
import de.invesdwin.common.client.swing.api.internal.action.ActionBinding;
import de.invesdwin.common.client.swing.api.internal.property.PropertyBinding;
import de.invesdwin.common.client.swing.util.AComponentFinder;
import de.invesdwin.util.assertions.Assertions;
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

    private final AModel model;
    private final Component component;
    private BindingGroup bindingGroup;

    public GeneratedBinding(final AModel model, final Component component) {
        this.model = model;
        this.component = component;
    }

    public BindingGroup initBindingGroup() {
        bindingGroup = new BindingGroup();
        bindingGroup.addBindingListener(new ErrorBindingListener());
        final List<Component> components = NAMED_COMPONENT_FINDER.findAll(component);
        for (final Component c : components) {
            new ActionBinding(model, c).initBinding();
            new PropertyBinding(model, c, bindingGroup).initBinding();
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
