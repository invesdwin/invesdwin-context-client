package de.invesdwin.context.client.swing.api.binding;

import java.awt.Component;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.text.JTextComponent;

import org.japura.gui.CheckComboBox;

import de.invesdwin.context.client.swing.api.binding.component.CheckBoxBinding;
import de.invesdwin.context.client.swing.api.binding.component.CheckBoxMenuItemBinding;
import de.invesdwin.context.client.swing.api.binding.component.CheckComboBoxBinding;
import de.invesdwin.context.client.swing.api.binding.component.ComboBoxBinding;
import de.invesdwin.context.client.swing.api.binding.component.IComponentBinding;
import de.invesdwin.context.client.swing.api.binding.component.KeyGrabberTextFieldBinding;
import de.invesdwin.context.client.swing.api.binding.component.ListBinding;
import de.invesdwin.context.client.swing.api.binding.component.RootTitleBinding;
import de.invesdwin.context.client.swing.api.binding.component.SpinnerBinding;
import de.invesdwin.context.client.swing.api.binding.component.SpinnerChoiceBinding;
import de.invesdwin.context.client.swing.api.binding.component.TextComponentBinding;
import de.invesdwin.context.client.swing.api.binding.component.button.ActionButtonBinding;
import de.invesdwin.context.client.swing.api.binding.component.button.DefaultSubmitButtonExceptionHandler;
import de.invesdwin.context.client.swing.api.binding.component.button.SubmitButtonBinding;
import de.invesdwin.context.client.swing.api.binding.component.label.LabelBinding;
import de.invesdwin.context.client.swing.api.binding.component.label.LabelTitleBinding;
import de.invesdwin.context.client.swing.api.binding.component.table.TableChoiceBinding;
import de.invesdwin.context.client.swing.api.view.AModel;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.context.client.swing.util.NamedModelComponentFinder;
import de.invesdwin.context.client.swing.util.Views;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContext;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassProcessor;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassProcessorConfig;
import de.invesdwin.norva.beanpath.spi.BeanPathUtil;
import de.invesdwin.norva.beanpath.spi.element.AActionBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.AChoiceBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.CheckBoxBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.IBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.table.ATableBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.utility.ContainerTitleBeanPathElement;
import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.lang.string.Strings;
import de.invesdwin.util.swing.text.KeyGrabberTextField;

@NotThreadSafe
@SuppressWarnings("rawtypes")
public final class GeneratedBindingGroup {

    public static final String TITLE_NAME_SUFFIX = BeanPathUtil.BEAN_PATH_SEPARATOR
            + ContainerTitleBeanPathElement.CONTAINER_TITLE_BEAN_PATH_FRAGMENT;
    protected BindingGroup bindingGroup;

    private final AView<?, ?> view;
    private final Component rootComponent;

    public GeneratedBindingGroup(final AView<?, ?> view, final Component rootComponent) {
        this.view = view;
        this.rootComponent = rootComponent;
    }

    protected DefaultSubmitButtonExceptionHandler newSubmitButtonExceptionHandler() {
        return newDefaultSubmitButtonExceptionHandler();
    }

    public static DefaultSubmitButtonExceptionHandler newDefaultSubmitButtonExceptionHandler() {
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
        final BeanClassContext context = BeanClassProcessor
                .getContext(BeanClassProcessorConfig.getDefaultEager(view.getModel().getClass()));

        bindingGroup = new BindingGroup(view, context, newSubmitButtonExceptionHandler());

        final List<Component> components = new NamedViewComponentFinder().findAll(rootComponent);
        for (final Component c : components) {
            try {
                final IComponentBinding binding;
                if (c instanceof JCheckBoxMenuItem) {
                    binding = bindJCheckBoxMenuItem((JCheckBoxMenuItem) c);
                } else if (c instanceof JMenuItem) {
                    binding = bindJMenuItem((JMenuItem) c);
                } else if (c instanceof JButton) {
                    binding = bindJButton((JButton) c);
                    /*
                     * WARNING: JCheckBox inside JCheckBoxButton will be traversed automatically, so no additional
                     * handling required. This prevents duplicate submits.
                     */
                    //                    if (c instanceof JCheckBoxButton) {
                    //                        final JCheckBoxButton checkboxButton = (JCheckBoxButton) c;
                    //                        final IComponentBinding checkboxBinding = bindJCheckBox(checkboxButton.getCheckbox());
                    //                        if (checkboxBinding != null) {
                    //                            bindingGroup.addBinding(binding);
                    //                        }
                    //                    }
                } else if (c instanceof KeyGrabberTextField) {
                    binding = bindKeyGrabberTextField((KeyGrabberTextField) c);
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
                } else if (c instanceof JSpinner) {
                    binding = bindJSpinner((JSpinner) c);
                } else if (c instanceof CheckComboBox) {
                    binding = bindCheckComboBox((CheckComboBox) c);
                } else {
                    throw UnknownArgumentException.newInstance(Class.class, c.getClass());
                }
                if (binding != null) {
                    bindingGroup.addBinding(binding);
                }
            } catch (final Throwable t) {
                throw new RuntimeException("At: " + c.getClass().getSimpleName() + " -> " + c.getName(), t);
            }
        }
        bindingGroup.addBinding(new RootTitleBinding(bindingGroup));
        bindingGroup.finishBinding();
        bindingGroup.update();
        return bindingGroup;
    }

    private IComponentBinding bindJSpinner(final JSpinner component) {
        final APropertyBeanPathElement element = getElement(component);
        if (element instanceof AChoiceBeanPathElement) {
            final AChoiceBeanPathElement cElement = (AChoiceBeanPathElement) element;
            return new SpinnerChoiceBinding(component, cElement, bindingGroup);
        } else {
            return new SpinnerBinding(component, element, bindingGroup);
        }
    }

    protected IComponentBinding bindJCheckBox(final JCheckBox component) {
        final APropertyBeanPathElement element = getElement(component);
        return new CheckBoxBinding(component, element, bindingGroup);
    }

    protected IComponentBinding bindCheckComboBox(final CheckComboBox component) {
        final AChoiceBeanPathElement element = getElement(component);
        return new CheckComboBoxBinding(component, element, bindingGroup);
    }

    protected IComponentBinding bindJTable(final JTable component) {
        final ATableBeanPathElement element = getElement(component);
        return new TableChoiceBinding(component, element, bindingGroup);
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

    protected IComponentBinding bindKeyGrabberTextField(final KeyGrabberTextField component) {
        final APropertyBeanPathElement element = getElement(component);
        return new KeyGrabberTextFieldBinding(component, element, bindingGroup);
    }

    protected IComponentBinding bindJCheckBoxMenuItem(final JCheckBoxMenuItem component) {
        final String beanPath = component.getName();

        final CheckBoxBeanPathElement element = bindingGroup.getModelContext()
                .getElementRegistry()
                .getElement(beanPath);

        return new CheckBoxMenuItemBinding(component, element, bindingGroup);
    }

    protected IComponentBinding bindJMenuItem(final JMenuItem component) {
        //prefer action over element for menu items
        final String beanPath = component.getName();
        final Action action = view.getActionMap().get(beanPath);
        if (action != null) {
            return new ActionButtonBinding(component, action);
        }

        final AActionBeanPathElement element = bindingGroup.getModelContext().getElementRegistry().getElement(beanPath);
        if (element != null) {
            return new SubmitButtonBinding(component, element, bindingGroup);
        }

        //allow menus not to be bound
        return null;
    }

    protected IComponentBinding bindJButton(final JButton component) {
        //prefer element over action for normal buttons
        final String beanPath = component.getName();
        final AActionBeanPathElement element = bindingGroup.getModelContext().getElementRegistry().getElement(beanPath);
        if (element != null) {
            return new SubmitButtonBinding(component, element, bindingGroup);
        }

        final Action action = view.getActionMap().get(beanPath);
        if (action != null) {
            return new ActionButtonBinding(component, action);
        }

        //buttons need to be always bound
        throw newElementNotFoundException(component, beanPath);
    }

    private final class NamedViewComponentFinder extends NamedModelComponentFinder {

        @Override
        protected boolean shouldIgnoreTree(final Component rootComponent) {
            return AModel.IGNORE.equals(rootComponent.getName()) || isNotThisView(rootComponent);
        }

        private boolean isNotThisView(final Component rootComponent) {
            final AView<?, ?> viewAt = Views.getViewAt(rootComponent);
            return viewAt != null && viewAt != view;
        }
    }

}
