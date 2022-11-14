package de.invesdwin.context.client.swing.api.binding.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.view.AModel;
import de.invesdwin.norva.beanpath.spi.element.AChoiceBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.util.collections.Collections;
import de.invesdwin.util.lang.Objects;

@NotThreadSafe
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ListBinding extends AComponentBinding<JList, List<?>> {

    private final AChoiceBeanPathElement element;
    private List prevChoices = Collections.emptyList();
    private String[] prevRenderedChoices;
    private int[] prevSelectedIndices;

    public ListBinding(final JList component, final AChoiceBeanPathElement element, final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        this.element = element;
        if (eagerSubmitRunnable != null) {
            component.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(final ListSelectionEvent e) {
                    eagerSubmitRunnable.run();
                }
            });
        }
        configureSelectionMode(component, element);
    }

    protected void configureSelectionMode(final JList component, final AChoiceBeanPathElement element) {
        if (element.isMultiSelection()) {
            component.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        } else {
            component.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
    }

    @Override
    protected Optional<List<?>> fromModelToComponent(final List<?> modelValue) {
        final List<?> choices = element.getChoiceModifier().getValueFromRoot(bindingGroup.getModel());
        final String[] renderedChoices = new String[choices.size()];
        for (int i = 0; i < renderedChoices.length; i++) {
            renderedChoices[i] = renderChoice(choices.get(i));
        }
        boolean choicesChanged = false;
        if (!Objects.equals(renderedChoices, prevRenderedChoices)) {
            final DefaultListModel<Object> model = new DefaultListModel<>();
            for (final String renderedChoice : renderedChoices) {
                model.addElement(renderedChoice);
            }
            component.setModel(model);
            prevChoices = new ArrayList<>(choices);
            prevRenderedChoices = renderedChoices;
            choicesChanged = true;
        }
        final int[] selectedIndices = new int[modelValue.size()];
        for (int i = 0; i < selectedIndices.length; i++) {
            final int index = choices.indexOf(modelValue.get(i));
            selectedIndices[i] = index;
        }
        if (choicesChanged || !Objects.equals(selectedIndices, prevSelectedIndices)) {
            component.setSelectedIndices(selectedIndices);
            prevSelectedIndices = selectedIndices;
            return Optional.ofNullable(modelValue);
        } else {
            return prevModelValue;
        }
    }

    @Override
    protected List<?> fromComponentToModel() {
        final int[] selectedIndices = component.getSelectedIndices();
        if (selectedIndices.length == 0) {
            return Collections.emptyList();
        } else {
            final List<Object> selectedValues = new ArrayList<>(selectedIndices.length);
            for (int i = 0; i < selectedIndices.length; i++) {
                selectedValues.add(prevChoices.get(selectedIndices[i]));
            }
            return selectedValues;
        }
    }

    @Override
    protected IBeanPathPropertyModifier<List<?>> getModifier() {
        return element.getSelectionModifier();
    }

    @Override
    protected void setValueFromRoot(final AModel root, final List<?> value) {
        if (element.isChoiceOnly()) {
            return;
        }
        super.setValueFromRoot(root, value);
    }

    @Override
    public void reset() {
        super.reset();
        prevChoices = Collections.emptyList();
        prevRenderedChoices = null;
        prevSelectedIndices = null;
    }

}
