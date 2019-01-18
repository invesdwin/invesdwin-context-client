package de.invesdwin.context.client.swing.api.binding.internal;

import java.awt.Component;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.text.JTextComponent;

import de.invesdwin.context.client.swing.api.AModel;
import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.IBinding;
import de.invesdwin.context.client.swing.api.binding.internal.property.JTextComponentBinding;
import de.invesdwin.context.client.swing.util.AComponentFinder;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContainer;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContext;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassProcessor;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassType;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.util.collections.loadingcache.ALoadingCache;
import de.invesdwin.util.error.UnknownArgumentException;
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

    public BindingGroup bind() {
        bindingGroup = new BindingGroup(MODELCLASS_CONTEXT.get(model.getClass()));

        final List<Component> components = NAMED_COMPONENT_FINDER.findAll(component);
        for (final Component c : components) {
            final IBinding binding;
            if (component instanceof JButton) {
                binding = initBinding((JButton) component);
            } else if (component instanceof JTextComponent) {
                binding = initBinding((JTextComponent) component);
            } else if (component instanceof JLabel) {
                binding = initBinding((JLabel) component);
            } else if (component instanceof JProgressBar) {
                binding = initBinding((JProgressBar) component);
            } else if (component instanceof JComboBox) {
                binding = initBinding((JComboBox) component);
            } else if (component instanceof JList) {
                binding = initBinding((JList) component);
            } else if (component instanceof JTable) {
                binding = initBinding((JTable) component);
            } else if (component instanceof JCheckBox) {
                binding = initBinding((JCheckBox) component);
            } else {
                throw UnknownArgumentException.newInstance(Class.class, c.getClass());
            }
            bindingGroup.add(binding);
        }
        return bindingGroup;
    }

    protected IBinding initBinding(final JCheckBox component) {
        return null;
    }

    protected IBinding initBinding(final JTable component) {
        return null;
    }

    protected IBinding initBinding(final JList component) {
        return null;
    }

    protected IBinding initBinding(final JComboBox component) {
        return null;
    }

    protected IBinding initBinding(final JProgressBar component) {
        return null;
    }

    protected IBinding initBinding(final JLabel component) {
        return null;
    }

    protected IBinding initBinding(final JTextComponent component) {
        final APropertyBeanPathElement element = bindingGroup.getContext()
                .getElementRegistry()
                .getElement(component.getName());
        return new JTextComponentBinding(component, element, model);
    }

    protected IBinding initBinding(final JButton component) {
        return null;
    }

}
