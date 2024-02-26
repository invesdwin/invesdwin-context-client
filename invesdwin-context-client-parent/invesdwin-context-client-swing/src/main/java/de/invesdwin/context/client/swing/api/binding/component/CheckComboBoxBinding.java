package de.invesdwin.context.client.swing.api.binding.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.concurrent.NotThreadSafe;

import org.japura.gui.Anchor;
import org.japura.gui.BatchSelection;
import org.japura.gui.CheckComboBox;
import org.japura.gui.CheckComboBox.CheckState;
import org.japura.gui.EmbeddedComponent;
import org.japura.gui.event.ListCheckListener;
import org.japura.gui.event.ListEvent;
import org.japura.util.i18n.I18nAdapter;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.component.InvesdwinJapuraGUIHandlerString;
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

    static {
        I18nAdapter.getAdapter().registerHandler(new InvesdwinJapuraGUIHandlerString());
    }

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

        setTextFor(component, CheckComboBox.NONE);
        setTextFor(component, CheckComboBox.MULTIPLE);
        setTextFor(component, CheckComboBox.ALL);
        final BatchSelection bs = new BatchSelection.CheckBox();
        final EmbeddedComponent embeddedComp = new EmbeddedComponent(bs, Anchor.NORTH);
        component.setEmbeddedComponent(embeddedComp);
    }

    private void setTextFor(final CheckComboBox component, final CheckState checkState) {
        component.setTextFor(checkState,
                GuiService.i18n(InvesdwinJapuraGUIHandlerString.class, checkState.name().toLowerCase()));
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
