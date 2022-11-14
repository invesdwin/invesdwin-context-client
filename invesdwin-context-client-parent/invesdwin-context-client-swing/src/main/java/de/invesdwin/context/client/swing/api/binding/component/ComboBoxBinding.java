package de.invesdwin.context.client.swing.api.binding.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JComboBox;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.norva.beanpath.spi.element.AChoiceBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.util.collections.Collections;
import de.invesdwin.util.lang.Objects;

@NotThreadSafe
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ComboBoxBinding extends AComponentBinding<JComboBox, Object> {

    private final AChoiceBeanPathElement element;
    private List<Object> prevChoices = Collections.emptyList();
    private String[] prevRenderedChoices;
    private Optional<String> prevRenderedModelValue;

    public ComboBoxBinding(final JComboBox component, final AChoiceBeanPathElement element,
            final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        this.element = element;
        if (eagerSubmitRunnable != null) {
            component.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    //autocomplete can send intermediary action events which we have to ignore
                    if (component.getSelectedIndex() >= 0) {
                        eagerSubmitRunnable.run();
                    }
                }
            });
        }
    }

    @Override
    protected boolean isModifiable() {
        return super.isModifiable() && !element.isChoiceOnly();
    }

    @Override
    protected Optional<Object> fromModelToComponent(final Object modelValue) {
        final List<?> choices = element.getChoiceModifier().getValueFromRoot(bindingGroup.getModel());
        final String[] renderedChoices = new String[choices.size()];
        for (int i = 0; i < renderedChoices.length; i++) {
            renderedChoices[i] = renderChoice(choices.get(i));
        }
        boolean choicesChanged = false;
        if (!Objects.equals(renderedChoices, prevRenderedChoices)) {
            component.removeAllItems();
            for (final String renderedChoice : renderedChoices) {
                component.addItem(renderedChoice);
            }
            prevChoices = new ArrayList<>(choices);
            prevRenderedChoices = renderedChoices;
            choicesChanged = true;
        }
        final String renderedModelValue = renderChoice(modelValue);
        if (choicesChanged || prevRenderedModelValue == null
                || !Objects.equals(renderedModelValue, prevRenderedModelValue.orElse(null))) {
            component.setSelectedItem(renderedModelValue);
            prevRenderedModelValue = Optional.ofNullable(renderedModelValue);
            return Optional.ofNullable(modelValue);
        } else {
            return prevModelValue;
        }
    }

    @Override
    protected Object fromComponentToModel() {
        final int selectedIndex = component.getSelectedIndex();
        return prevChoices.get(selectedIndex);
    }

    @Override
    protected IBeanPathPropertyModifier<Object> getModifier() {
        return element.getModifier();
    }

    @Override
    public void reset() {
        super.reset();
        prevChoices = Collections.emptyList();
        prevRenderedChoices = null;
        prevRenderedModelValue = null;
    }
}
