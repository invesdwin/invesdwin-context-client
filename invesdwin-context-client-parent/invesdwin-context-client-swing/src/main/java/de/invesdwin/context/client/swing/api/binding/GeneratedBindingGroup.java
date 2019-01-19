package de.invesdwin.context.client.swing.api.binding;

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
import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.api.binding.internal.components.ButtonBinding;
import de.invesdwin.context.client.swing.api.binding.internal.components.TextComponentBinding;
import de.invesdwin.context.client.swing.api.binding.submit.DefaultSubmitButtonExceptionHandler;
import de.invesdwin.context.client.swing.util.AComponentFinder;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContainer;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContext;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassProcessor;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassType;
import de.invesdwin.norva.beanpath.spi.element.AActionBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.IBeanPathElement;
import de.invesdwin.util.collections.loadingcache.ALoadingCache;
import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.lang.Strings;

@NotThreadSafe
public final class GeneratedBindingGroup {

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

    protected BindingGroup bindingGroup;
    private final AView<?, ?> view;
    private final Component component;

    public GeneratedBindingGroup(final AView<?, ?> view, final Component component) {
        this.view = view;
        this.component = component;
    }

    protected DefaultSubmitButtonExceptionHandler newSubmitButtonExceptionHandler() {
        return new DefaultSubmitButtonExceptionHandler();
    }

    protected <T> T getElement(final Component component) {
        final T element = bindingGroup.getModelContext().getElementRegistry().getElement(component.getName());
        if (element == null) {
            throw new IllegalArgumentException("No " + IBeanPathElement.class.getSimpleName() + " found for "
                    + component.getClass().getSimpleName() + " with name: " + component.getName());
        } else {
            return element;
        }
    }

    public BindingGroup bind() {
        bindingGroup = new BindingGroup(view, MODELCLASS_CONTEXT.get(view.getModel().getClass()),
                newSubmitButtonExceptionHandler());

        final List<Component> components = NAMED_COMPONENT_FINDER.findAll(component);
        for (final Component c : components) {
            final IBinding binding;
            if (component instanceof JButton) {
                binding = bindJButton((JButton) component);
            } else if (component instanceof JTextComponent) {
                binding = bindJTextComponent((JTextComponent) component);
            } else if (component instanceof JLabel) {
                binding = bindJLabel((JLabel) component);
            } else if (component instanceof JProgressBar) {
                binding = bindJProgressBar((JProgressBar) component);
            } else if (component instanceof JComboBox) {
                binding = bindJComboBox((JComboBox) component);
            } else if (component instanceof JList) {
                binding = bindJList((JList) component);
            } else if (component instanceof JTable) {
                binding = bindJTable((JTable) component);
            } else if (component instanceof JCheckBox) {
                binding = bindJCheckBox((JCheckBox) component);
            } else {
                throw UnknownArgumentException.newInstance(Class.class, c.getClass());
            }
            bindingGroup.add(binding);
        }
        return bindingGroup;
    }

    protected IBinding bindJCheckBox(final JCheckBox component) {
        return null;
    }

    protected IBinding bindJTable(final JTable component) {
        return null;
    }

    protected IBinding bindJList(final JList component) {
        return null;
    }

    protected IBinding bindJComboBox(final JComboBox component) {
        return null;
    }

    protected IBinding bindJProgressBar(final JProgressBar component) {
        return null;
    }

    protected IBinding bindJLabel(final JLabel component) {
        return null;
    }

    protected IBinding bindJTextComponent(final JTextComponent component) {
        final APropertyBeanPathElement element = getElement(component);
        return new TextComponentBinding(component, element, bindingGroup);
    }

    protected IBinding bindJButton(final JButton component) {
        final AActionBeanPathElement element = getElement(component);
        return new ButtonBinding(component, element, bindingGroup);
    }

}