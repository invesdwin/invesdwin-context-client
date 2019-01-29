package de.invesdwin.context.client.swing.api.binding.component;

import java.awt.Component;
import java.util.List;
import java.util.Optional;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JComponent;

import de.invesdwin.context.client.swing.api.AModel;
import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.util.SubmitAllViewsHelper;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContainer;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.norva.beanpath.spi.element.utility.ValidateBeanPathElement;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.lang.Strings;

@NotThreadSafe
public abstract class AComponentBinding<C extends JComponent, V> implements IComponentBinding {

    public static final String TITLE = "title";
    public static final String TITLE_PLACEHOLDER = "$(" + TITLE + ")";
    public static final String TITLE_SURROUNDING = "'";

    protected final C component;
    protected final APropertyBeanPathElement element;
    protected final ValidateBeanPathElement validateElement;
    protected final BindingGroup bindingGroup;
    protected final Runnable eagerSubmitRunnable;
    protected Optional<V> prevModelValue;
    protected boolean submitted = false;
    protected String invalidMessage = null;
    protected boolean updating = false;

    public AComponentBinding(final C component, final APropertyBeanPathElement element,
            final BindingGroup bindingGroup) {
        this.component = component;
        this.element = element;
        this.validateElement = element.getValidateElement();
        this.bindingGroup = bindingGroup;
        this.eagerSubmitRunnable = newEagerSubmitRunnable();
    }

    private Runnable newEagerSubmitRunnable() {
        if (isEager()) {
            if (isForced()) {
                return new Runnable() {
                    private final SubmitAllViewsHelper helper = new SubmitAllViewsHelper() {
                        @Override
                        protected String validate(final List<AView<?, ?>> views) {
                            //only show conversion errors, ignore any other validation errors
                            return invalidMessage;
                        }

                        @Override
                        public void process(final Component component) {
                            if (!isModifiable()) {
                                return;
                            }
                            super.process(component);
                        }
                    };

                    @Override
                    public void run() {
                        if (!updating) {
                            helper.process(component);
                        }
                    }
                };

            } else {
                return new Runnable() {
                    private final SubmitAllViewsHelper helper = new SubmitAllViewsHelper() {
                        @Override
                        public void process(final Component component) {
                            if (!isModifiable()) {
                                return;
                            }
                            super.process(component);
                        }
                    };

                    @Override
                    public void run() {
                        if (!updating) {
                            helper.process(component);
                        }
                    }
                };
            }
        } else {
            return null;
        }
    }

    protected boolean isForced() {
        return element.isForced();
    }

    protected boolean isEager() {
        return element.isEager();
    }

    @Override
    public void submit() {
        if (!isModifiable()) {
            return;
        }
        final AModel model = bindingGroup.getModel();
        try {
            prevModelValue = Optional.ofNullable(getModifier().getValueFromRoot(model));
            final V newModelValue = fromComponentToModel();
            if (!Objects.equals(prevModelValue.orElse(null), newModelValue)) {
                if (validateElement != null) {
                    final String invalid = validateElement.validateFromRoot(model, newModelValue);
                    if (Strings.isNotBlank(invalid)) {
                        invalidMessage = normalizeValidationMessage(invalid);
                        return;
                    }
                }
                getModifier().setValueFromRoot(model, newModelValue);
                invalidMessage = null;
                submitted = true;
            } else {
                submitted = false;
            }
        } catch (final Throwable t) {
            Err.process(t);
            invalidMessage = normalizeValidationMessage(t.toString());
            submitted = false;
        }
    }

    protected abstract IBeanPathPropertyModifier<V> getModifier();

    @Override
    public String validate() {
        if (Strings.isNotBlank(invalidMessage)) {
            return invalidMessage;
        }
        if (validateElement != null) {
            //validate using custom validator only once all properties have been synchronized
            final AModel model = bindingGroup.getModel();
            final Object modelValue = getModifier().getValueFromRoot(model);
            final String invalid = validateElement.validateFromRoot(model, modelValue);
            if (Strings.isNotBlank(invalid)) {
                invalidMessage = normalizeValidationMessage(invalid);
                return invalidMessage;
            }
        }
        return null;
    }

    @Override
    public void setInvalidMessage(final String invalidMessage) {
        this.invalidMessage = invalidMessage;
    }

    @Override
    public String getInvalidMessage() {
        return invalidMessage;
    }

    @Override
    public String getBeanPath() {
        return element.getBeanPath();
    }

    @Override
    public void commit() {
        if (invalidMessage != null) {
            rollback();
        }
        if (!submitted) {
            return;
        }
        submitted = false;
        invalidMessage = null;
    }

    @Override
    public void rollback() {
        if (!submitted && invalidMessage == null) {
            return;
        }
        final AModel model = bindingGroup.getModel();
        getModifier().setValueFromRoot(model, prevModelValue.orElse(null));
        submitted = false;
        invalidMessage = null;
    }

    @Override
    public void update() {
        updating = true;
        try {
            final AModel model = bindingGroup.getModel();
            final V modelValue = getModifier().getValueFromRoot(model);
            if (prevModelValue == null || !Objects.equals(prevModelValue.orElse(null), modelValue)) {
                fromModelToComponent(modelValue);
                prevModelValue = Optional.ofNullable(modelValue);
            }

            final Object target = getTarget();
            component.setEnabled(element.isEnabled(target));
            component.setVisible(element.isVisible(target));
            component.setToolTipText(bindingGroup.i18n(element.getTooltip(target)));
        } finally {
            updating = false;
        }
    }

    protected Object getTarget() {
        final AModel model = bindingGroup.getModel();
        final BeanClassContainer container = (BeanClassContainer) element.getContainer();
        final Object target = container.getObjectFromRoot(model);
        return target;
    }

    protected boolean isModifiable() {
        return component.isVisible() && component.isEnabled();
    }

    protected abstract void fromModelToComponent(V modelValue);

    protected abstract V fromComponentToModel();

    private String normalizeValidationMessage(final String m) {
        String message = bindingGroup.i18n(m.trim());
        final String title = surroundTitle(getTitle());
        message = message.replace(TITLE_PLACEHOLDER, title);
        if (!message.contains(title)) {
            message = title + " " + message;
        }
        if (!Strings.endsWithAny(message, ".", "!", "?")) {
            message += ".";
        }
        return message;
    }

    protected String getTitle() {
        return bindingGroup.getTitle(element, getTarget());
    }

    public static String surroundTitle(final String title) {
        if (Strings.isNotBlank(title)) {
            return TITLE_SURROUNDING + title + TITLE_SURROUNDING;
        } else {
            return "";
        }
    }

    protected Object renderChoice(final Object choice) {
        final Object convertedChoice;
        if (choice == null) {
            convertedChoice = null;
        } else if (choice instanceof Enum) {
            final Enum<?> cChoice = (Enum<?>) choice;
            final String id = choice.getClass().getSuperclass().getSimpleName() + "." + cChoice.name();
            final String choiceStr = bindingGroup.i18n(id, choice.toString());
            convertedChoice = choiceStr;
        } else {
            final String choiceStr = bindingGroup.i18n(choice.toString());
            convertedChoice = choiceStr;
        }
        return convertedChoice;
    }

}
