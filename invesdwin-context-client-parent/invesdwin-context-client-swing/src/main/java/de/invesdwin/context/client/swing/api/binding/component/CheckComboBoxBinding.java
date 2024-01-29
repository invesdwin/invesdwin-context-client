package de.invesdwin.context.client.swing.api.binding.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.concurrent.NotThreadSafe;

import org.japura.gui.CheckComboBox;
import org.japura.gui.event.ListCheckListener;
import org.japura.gui.event.ListEvent;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.norva.beanpath.spi.element.AChoiceBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.util.collections.Collections;
import de.invesdwin.util.lang.Objects;

@NotThreadSafe
public class CheckComboBoxBinding extends AComponentBinding<CheckComboBox, List<?>> {

    private final AChoiceBeanPathElement element;
    private List prevChoices = Collections.emptyList();
    private List prevSelection = Collections.emptyList();
    private String[] prevRenderedChoices;

    public CheckComboBoxBinding(final CheckComboBox component, final AChoiceBeanPathElement element,
            final BindingGroup bindingGroup) {
        super(component, element, bindingGroup);
        this.element = element;
        component.getModel().addListCheckListener(new ListCheckListener() {
            @Override
            public void removeCheck(final ListEvent event) {
                eagerSubmitRunnable.run();
            }

            @Override
            public void addCheck(final ListEvent event) {
                eagerSubmitRunnable.run();
            }
        });
    }

    @Override
    protected IBeanPathPropertyModifier<List<?>> getModifier() {
        return element.getSelectionModifier();
    }

    @Override
    protected void resetCaches() {
        prevChoices = Collections.emptyList();
        prevSelection = Collections.emptyList();
        prevRenderedChoices = null;
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
            component.getModel().setElement(renderedChoices);
            prevChoices = new ArrayList<>(choices);
            prevRenderedChoices = renderedChoices;
            choicesChanged = true;
        }

        if (choicesChanged || !Objects.equals(modelValue, this.prevSelection)) {
            modelValue.forEach(v -> component.getModel().addCheck(v));
            this.prevSelection = modelValue;
            return Optional.ofNullable(modelValue);
        } else {
            return prevModelValue;
        }
    }

    @Override
    protected List<?> fromComponentToModel() throws Exception {
        final List<Object> checkeds = component.getModel().getCheckeds();
        if (checkeds.isEmpty()) {
            return Collections.emptyList();
        } else {
            return checkeds;
        }
    }
}
