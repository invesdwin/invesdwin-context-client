package de.invesdwin.context.client.swing.api.binding.internal.property;

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
import de.invesdwin.context.client.swing.api.binding.internal.property.converter.ArrayToListConverter;
import de.invesdwin.context.client.swing.api.binding.internal.property.converter.DateToStringConverter;
import de.invesdwin.context.client.swing.api.binding.internal.property.converter.NumberToStringConverter;
import de.invesdwin.context.client.swing.api.binding.internal.property.selection.EnumValuesProperty;
import de.invesdwin.context.client.swing.api.binding.internal.property.selection.JListMultipleSelectionProperty;
import de.invesdwin.context.client.swing.api.binding.internal.property.selection.JTableMultipleSelectionProperty;
import de.invesdwin.context.client.swing.api.binding.internal.property.selection.JTableSingleSelectionProperty;
import de.invesdwin.norva.beanpath.BeanPathReflections;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContext;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassType;
import de.invesdwin.norva.beanpath.spi.element.AChoiceBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.lang.Reflections;
import de.invesdwin.util.lang.Strings;

@SuppressWarnings({ "rawtypes", "unchecked" })
@NotThreadSafe
public class PropertyBinding {

    private final BeanClassContext context;
    private final AModel model;
    private final Component component;
    private final BindingGroup bindingGroup;

    public PropertyBinding(final BeanClassContext context, final AModel model, final Component component,
            final BindingGroup bindingGroup) {
        this.context = context;
        this.model = model;
        this.component = component;
        this.bindingGroup = bindingGroup;
    }

    public void initBinding() {
        if (component instanceof JTextComponent) {
            initBinding((JTextComponent) component);
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

    private void initBinding(final JTextComponent component) {
        final APropertyBeanPathElement element = context.getElementRegistry().getElement(component.getName());
        final Binding binding = initBinding(component, "text");
        if (element.getAccessor().getType().isDate()) {
            binding.setConverter(new DateToStringConverter(element));
        } else if (element.getAccessor().getType().isNumber()) {
            binding.setConverter(new NumberToStringConverter(element));
        }
    }

    private void initBinding(final JTable component) {
        final AChoiceBeanPathElement element = context.getElementRegistry().getElement(component.getName());
        final Property<AModel, List<Object>> choiceProperty = newChoiceProperty(element);
        final JTableBinding binding = SwingBindings.createJTableBinding(UpdateStrategy.READ_WRITE, model,
                choiceProperty, component, element.getBeanPath());
        maybeAddArrayToListConverter(element, binding);
        bindingGroup.addBinding(binding);

        final Class<?> rowClass = element.getModifier().getBeanClassAccessor().getType().getType();
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

        final String choicePropertyName = element.getChoiceElement().getBeanPath();
        final String selectionPropertyName = element.getBeanPath();
        if (!selectionPropertyName.equals(choicePropertyName)) {
            if (component.getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_SELECTION) {
                initBinding(selectionPropertyName, component, new JTableSingleSelectionProperty(model, choiceProperty));
            } else {
                initBinding(selectionPropertyName, component,
                        new JTableMultipleSelectionProperty(model, choiceProperty));
            }
        }
    }

    private void initBinding(final JList component) {
        final AChoiceBeanPathElement element = context.getElementRegistry().getElement(component.getName());
        final Property<AModel, List<Object>> choiceProperty = newChoiceProperty(element);
        final JListBinding binding = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, model, choiceProperty,
                component, element.getBeanPath());
        maybeAddArrayToListConverter(element, binding);
        bindingGroup.addBinding(binding);

        final String choicePropertyName = element.getChoiceElement().getBeanPath();
        final String selectionPropertyName = element.getBeanPath();
        if (!selectionPropertyName.equals(choicePropertyName)) {
            if (component.getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_SELECTION) {
                initBinding(selectionPropertyName, component, "selectedValue");
            } else {
                initBinding(selectionPropertyName, component,
                        new JListMultipleSelectionProperty(model, choiceProperty));
            }
        }
    }

    private Property<AModel, List<Object>> newChoiceProperty(final AChoiceBeanPathElement element) {
        final IBeanPathPropertyModifier<List<?>> choiceModifier = element.getChoiceModifier();
        final BeanClassType type = choiceModifier.getBeanClassAccessor().getType();
        if (type.isEnum() && element.getChoiceElement().getBeanPath().equals(element.getBeanPath())) {
            return new EnumValuesProperty(choiceModifier);
        } else {
            final Property<AModel, List<Object>> choiceProperty = BeanProperty
                    .create(element.getChoiceElement().getBeanPath());
            return choiceProperty;
        }
    }

    private void initBinding(final JComboBox component) {
        final AChoiceBeanPathElement element = context.getElementRegistry().getElement(component.getName());
        final String choicePropertyName = element.getChoiceElement().getBeanPath();
        final Property<AModel, List<Object>> choiceProperty = newChoiceProperty(element);
        final JComboBoxBinding binding = SwingBindings.createJComboBoxBinding(UpdateStrategy.READ_WRITE, model,
                choiceProperty, component, choicePropertyName);
        maybeAddArrayToListConverter(element, binding);
        bindingGroup.addBinding(binding);
        final String selectionPropertyName = element.getBeanPath();
        if (!selectionPropertyName.equals(choicePropertyName)) {
            initBinding(selectionPropertyName, component, "selectedItem");
        }
    }

    private void maybeAddArrayToListConverter(final AChoiceBeanPathElement element, final Binding binding) {
        if (element.getChoiceModifier().getAccessor().getRawType().isArray()) {
            binding.setConverter(new ArrayToListConverter(element));
        }
    }

    private Binding initBinding(final Component component, final String componentPath) {
        return initBinding(component, BeanProperty.create(componentPath));
    }

    private Binding initBinding(final Component component, final Property componentProperty) {
        return initBinding(component.getName(), component, componentProperty);
    }

    private Binding initBinding(final String modelProperty, final Component component, final String componentPath) {
        return initBinding(modelProperty, component, BeanProperty.create(componentPath));
    }

    /**
     * Source = model
     * 
     * Target = component
     * 
     * @return
     */
    private Binding<Object, Object, Object, Object> initBinding(final String modelProperty, final Component component,
            final Property componentProperty) {
        final EventDispatchThreadProperty edtProperty = new EventDispatchThreadProperty(componentProperty);
        final Binding binding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, model,
                BeanProperty.create(modelProperty), component, edtProperty, modelProperty);
        bindingGroup.addBinding(binding);
        return binding;
    }
}
