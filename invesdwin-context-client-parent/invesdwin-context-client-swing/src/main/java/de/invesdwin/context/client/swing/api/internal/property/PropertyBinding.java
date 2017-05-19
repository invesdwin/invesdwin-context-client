package de.invesdwin.context.client.swing.api.internal.property;

import java.awt.Component;
import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.text.JTextComponent;

import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.Property;
import org.jdesktop.swingbinding.JComboBoxBinding;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;

import com.google.common.base.Optional;

import de.invesdwin.context.client.swing.api.AModel;
import de.invesdwin.context.client.swing.api.internal.property.selection.JListMultipleSelectionProperty;
import de.invesdwin.context.client.swing.api.internal.property.selection.JTableMultipleSelectionProperty;
import de.invesdwin.context.client.swing.api.internal.property.selection.JTableSingleSelectionProperty;
import de.invesdwin.norva.beanpath.BeanPathReflections;
import de.invesdwin.norva.beanpath.spi.element.utility.ChoiceBeanPathElement;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.lang.Reflections;
import de.invesdwin.util.lang.Strings;

@NotThreadSafe
public class PropertyBinding {

    private final AModel model;
    private final Component component;
    private final BindingGroup bindingGroup;

    public PropertyBinding(final AModel model, final Component component, final BindingGroup bindingGroup) {
        this.model = model;
        this.component = component;
        this.bindingGroup = bindingGroup;
    }

    @SuppressWarnings("rawtypes")
    public void initBinding() {
        if (component instanceof JTextComponent) {
            initBinding(component, "text");
        } else if (component instanceof JLabel) {
            initBinding(component, "text");
        } else if (component instanceof JProgressBar) {
            initBinding(component, "progress");
        } else if (component instanceof JComboBox) {
            initBinding((JComboBox) component);
        } else if (component instanceof JList) {
            initBinding((JList) component);
        } else if (component instanceof JTable) {
            initBinding((JTable) component);
        } else if (component instanceof JCheckBox) {
            initBinding(component, "selected");
        }
    }

    private void initBinding(final JTable component) {
        final String listPropertyName = component.getName();
        final Property<AModel, List<Object>> listProperty = BeanProperty.create(listPropertyName);
        final JTableBinding<Object, AModel, JTable> binding = SwingBindings.createJTableBinding(
                UpdateStrategy.READ_WRITE, model, listProperty, component, listPropertyName);
        bindingGroup.addBinding(binding);

        final Class<? extends AModel> modelClass = model.getClass();
        final String listPropertyMethodName = BeanPathReflections.PROPERTY_GET_METHOD_PREFIX
                + Strings.capitalize(listPropertyName);
        final Method listPropertyMethod = Reflections.findMethod(modelClass, listPropertyMethodName);
        Assertions.assertThat(listPropertyMethod)
                .as("Method [%s] does not exist on type [%s].", listPropertyMethodName, modelClass.getName())
                .isNotNull();
        final Class<?> rowClass = Reflections.resolveReturnTypeArgument(listPropertyMethod,
                Reflections.resolveReturnType(listPropertyMethod, modelClass));
        final ResourceMap rowResourceMap = Application.getInstance().getContext().getResourceMap(rowClass);
        for (int i = 0; i < component.getModel().getColumnCount(); i++) {
            final String columnProperty = component.getModel().getColumnName(i);
            final String[] columnMethodNames = {
                    BeanPathReflections.PROPERTY_GET_METHOD_PREFIX + Strings.capitalize(columnProperty),
                    BeanPathReflections.PROPERTY_IS_METHOD_PREFIX + Strings.capitalize(columnProperty) };
            Method columnMethod = null;
            for (final String columnMethodName : columnMethodNames) {
                columnMethod = Reflections.findMethod(rowClass, columnMethodName);
                if (columnMethod != null) {
                    break;
                }
            }
            Assertions.assertThat(columnMethod)
                    .as("Method [%s] does not exist on type [%s].", Objects.toString(columnMethodNames),
                            rowClass.getName())
                    .isNotNull();
            final Class<?> columnClass = Reflections.resolveReturnType(columnMethod, rowClass);
            if (columnMethod != null
                    && columnMethod.getName().startsWith(BeanPathReflections.PROPERTY_IS_METHOD_PREFIX)) {
                Assertions.assertThat(columnClass)
                        .as("Only primitive boolean type is allowed to have an \"%s\" property accesor. Use \"%s\" instead for property: %s.%s",
                                BeanPathReflections.PROPERTY_IS_METHOD_PREFIX,
                                BeanPathReflections.PROPERTY_GET_METHOD_PREFIX, rowClass.getName(),
                                columnMethod.getName())
                        .isEqualTo(boolean.class);
            }
            final String columnName = rowResourceMap.getString(columnProperty + AModel.TEXT_RESOURCE_SUFFIX);
            binding.addColumnBinding(BeanProperty.create(columnProperty))
                    .setColumnName(Optional.fromNullable(columnName).or(columnProperty))
                    .setColumnClass(columnClass);
        }

        final String selectionPropertyName = component.getName() + ChoiceBeanPathElement.CHOICE_SUFFIX;
        if (component.getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_SELECTION) {
            initBinding(selectionPropertyName, component, new JTableSingleSelectionProperty(model, listProperty));
        } else {
            initBinding(selectionPropertyName, component, new JTableMultipleSelectionProperty(model, listProperty));
        }
    }

    @SuppressWarnings("rawtypes")
    private void initBinding(final JList component) {
        final String listPropertyName = component.getName();
        final Property<AModel, List<Object>> listProperty = BeanProperty.create(listPropertyName);
        final JListBinding<Object, AModel, JList> binding = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE,
                model, listProperty, component, listPropertyName);
        bindingGroup.addBinding(binding);
        final String selectionPropertyName = component.getName() + ChoiceBeanPathElement.CHOICE_SUFFIX;
        if (component.getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_SELECTION) {
            initBinding(selectionPropertyName, component, "selectedValue");
        } else {
            initBinding(selectionPropertyName, component, new JListMultipleSelectionProperty(model, listProperty));
        }
    }

    @SuppressWarnings("rawtypes")
    private void initBinding(final JComboBox component) {
        final String listPropertyName = component.getName();
        final Property<AModel, List<Object>> listProperty = BeanProperty.create(listPropertyName);
        final JComboBoxBinding<Object, AModel, JComboBox> binding = SwingBindings.createJComboBoxBinding(
                UpdateStrategy.READ_WRITE, model, listProperty, component, listPropertyName);
        bindingGroup.addBinding(binding);
        final String selectionPropertyName = component.getName() + ChoiceBeanPathElement.CHOICE_SUFFIX;
        initBinding(selectionPropertyName, component, "selectedItem");
    }

    private void initBinding(final Component component, final String componentPath) {
        initBinding(component, BeanProperty.create(componentPath));
    }

    @SuppressWarnings({ "rawtypes" })
    private void initBinding(final Component component, final Property componentProperty) {
        initBinding(component.getName(), component, componentProperty);
    }

    private void initBinding(final String modelProperty, final Component component, final String componentPath) {
        initBinding(modelProperty, component, BeanProperty.create(componentPath));
    }

    /**
     * Source = model
     * 
     * Target = component
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void initBinding(final String modelProperty, final Component component, final Property componentProperty) {
        final EventDispatchThreadProperty edtProperty = new EventDispatchThreadProperty(componentProperty);
        final Binding<?, ?, ?, ?> binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, model,
                BeanProperty.create(modelProperty), component, edtProperty, modelProperty);
        bindingGroup.addBinding(binding);
    }
}
