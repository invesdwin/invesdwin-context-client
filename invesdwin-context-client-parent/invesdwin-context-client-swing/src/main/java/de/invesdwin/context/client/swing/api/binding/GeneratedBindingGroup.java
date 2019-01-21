package de.invesdwin.context.client.swing.api.binding;

import java.awt.Component;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.text.JTextComponent;

import de.invesdwin.context.client.swing.api.AModel;
import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.api.binding.component.CheckBoxBinding;
import de.invesdwin.context.client.swing.api.binding.component.ComboBoxBinding;
import de.invesdwin.context.client.swing.api.binding.component.IComponentBinding;
import de.invesdwin.context.client.swing.api.binding.component.ListBinding;
import de.invesdwin.context.client.swing.api.binding.component.TextComponentBinding;
import de.invesdwin.context.client.swing.api.binding.component.button.ActionButtonBinding;
import de.invesdwin.context.client.swing.api.binding.component.button.DefaultSubmitButtonExceptionHandler;
import de.invesdwin.context.client.swing.api.binding.component.button.SubmitButtonBinding;
import de.invesdwin.context.client.swing.api.binding.component.label.LabelBinding;
import de.invesdwin.context.client.swing.api.binding.component.label.LabelTitleBinding;
import de.invesdwin.context.client.swing.api.binding.component.table.TableBinding;
import de.invesdwin.context.client.swing.util.AComponentFinder;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContainer;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContext;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassProcessor;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassType;
import de.invesdwin.norva.beanpath.spi.BeanPathUtil;
import de.invesdwin.norva.beanpath.spi.element.AActionBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.AChoiceBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.ATableBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.IBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.utility.ContainerTitleBeanPathElement;
import de.invesdwin.util.collections.loadingcache.ALoadingCache;
import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.lang.Strings;

@NotThreadSafe
@SuppressWarnings("rawtypes")
public final class GeneratedBindingGroup {

    public static final String TITLE_NAME_SUFFIX = BeanPathUtil.BEAN_PATH_SEPARATOR
            + ContainerTitleBeanPathElement.CONTAINER_TITLE_BEAN_PATH_FRAGMENT;
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
    private final Component rootComponent;

    public GeneratedBindingGroup(final AView<?, ?> view, final Component rootComponent) {
        this.view = view;
        this.rootComponent = rootComponent;
    }

    protected DefaultSubmitButtonExceptionHandler newSubmitButtonExceptionHandler() {
        return new DefaultSubmitButtonExceptionHandler();
    }

    protected <T> T getElement(final Component component) {
        final String beanPath = component.getName();
        return getElement(component, beanPath);
    }

    protected <T> T getElement(final Component component, final String beanPath) {
        final T element = bindingGroup.getModelContext().getElementRegistry().getElement(beanPath);
        if (element == null) {
            throw newElementNotFoundException(component, beanPath);
        } else {
            return element;
        }
    }

    private IllegalArgumentException newElementNotFoundException(final Component component, final String beanPath) {
        return new IllegalArgumentException("No " + IBeanPathElement.class.getSimpleName() + " found for "
                + component.getClass().getSimpleName() + " with name: " + beanPath);
    }

    public BindingGroup bind() {
        bindingGroup = new BindingGroup(view, MODELCLASS_CONTEXT.get(view.getModel().getClass()),
                newSubmitButtonExceptionHandler());

        final List<Component> components = NAMED_COMPONENT_FINDER.findAll(rootComponent);
        for (final Component c : components) {
            final IComponentBinding binding;
            if (c instanceof JMenuItem) {
                binding = bindJMenuItem((JMenuItem) c);
            } else if (c instanceof JButton) {
                binding = bindJButton((JButton) c);
            } else if (c instanceof JTextComponent) {
                binding = bindJTextComponent((JTextComponent) c);
            } else if (c instanceof JLabel) {
                binding = bindJLabel((JLabel) c);
            } else if (c instanceof JComboBox) {
                binding = bindJComboBox((JComboBox) c);
            } else if (c instanceof JList) {
                binding = bindJList((JList) c);
            } else if (c instanceof JTable) {
                binding = bindJTable((JTable) c);
            } else if (c instanceof JCheckBox) {
                binding = bindJCheckBox((JCheckBox) c);
            } else {
                throw UnknownArgumentException.newInstance(Class.class, c.getClass());
            }
            if (binding != null) {
                bindingGroup.add(binding);
            }
        }
        bindingGroup.update();
        return bindingGroup;
    }

    protected IComponentBinding bindJCheckBox(final JCheckBox component) {
        final APropertyBeanPathElement element = getElement(component);
        return new CheckBoxBinding(component, element, bindingGroup);
    }

    protected IComponentBinding bindJTable(final JTable component) {
        final ATableBeanPathElement element = getElement(component);
        return new TableBinding(component, element, bindingGroup);
    }

    protected IComponentBinding bindJList(final JList component) {
        final AChoiceBeanPathElement element = getElement(component);
        return new ListBinding(component, element, bindingGroup);
    }

    protected IComponentBinding bindJComboBox(final JComboBox component) {
        final AChoiceBeanPathElement element = getElement(component);
        return new ComboBoxBinding(component, element, bindingGroup);
    }

    protected IComponentBinding bindJLabel(final JLabel component) {
        if (component.getName().endsWith(TITLE_NAME_SUFFIX)) {
            final String beanPath = Strings.removeEnd(component.getName(), TITLE_NAME_SUFFIX);
            final APropertyBeanPathElement element = getElement(component, beanPath);
            return new LabelTitleBinding(component, element, bindingGroup);
        } else {
            final APropertyBeanPathElement element = getElement(component);
            return new LabelBinding(component, element, bindingGroup);
        }
    }

    protected IComponentBinding bindJTextComponent(final JTextComponent component) {
        final APropertyBeanPathElement element = getElement(component);
        return new TextComponentBinding(component, element, bindingGroup);
    }

    protected IComponentBinding bindJMenuItem(final JMenuItem component) {
        final AActionBeanPathElement element = bindingGroup.getModelContext()
                .getElementRegistry()
                .getElement(component.getName());
        if (element != null) {
            return new SubmitButtonBinding(component, element, bindingGroup);
        }
        final Action action = view.getActionMap().get(component.getName());
        if (action != null) {
            return new ActionButtonBinding(component, action);
        } else {
            //allow menus not to be bound
            return null;
        }
    }

    protected IComponentBinding bindJButton(final JButton component) {
        final String beanPath = component.getName();
        final AActionBeanPathElement element = bindingGroup.getModelContext().getElementRegistry().getElement(beanPath);
        if (element != null) {
            return new SubmitButtonBinding(component, element, bindingGroup);
        }
        final Action action = view.getActionMap().get(beanPath);
        if (action != null) {
            return new ActionButtonBinding(component, action);
        } else {
            throw newElementNotFoundException(component, beanPath);
        }
    }

}
