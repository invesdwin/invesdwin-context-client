package de.invesdwin.context.client.swing.api.binding.component;

import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.Optional;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;

import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.view.AModel;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.norva.beanpath.impl.object.BeanObjectContainer;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.norva.beanpath.spi.element.utility.ValidateBeanPathElement;
import de.invesdwin.util.lang.Strings;
import de.invesdwin.util.swing.Components;

@NotThreadSafe
public abstract class AComponentBinding<C extends JComponent, V> implements IComponentBinding {

    public static final Border INVALID_MESSAGE_BORDER = BorderFactory.createLineBorder(Color.RED);

    public static final String TITLE = "title";
    public static final String TITLE_PLACEHOLDER = "$(" + TITLE + ")";
    public static final String TITLE_SURROUNDING = "'";

    protected final C component;
    protected final APropertyBeanPathElement element;
    protected final ValidateBeanPathElement validateElement;
    protected final BindingGroup bindingGroup;
    protected final Runnable eagerSubmitRunnable;
    protected final Border originalBorder;
    protected Optional<V> prevModelValue;
    protected boolean submitted = false;
    protected String invalidMessage = null;
    protected String showingInvalidMessage = null;
    protected boolean updating = false;
    private boolean frozen;

    public AComponentBinding(final C component, final APropertyBeanPathElement element,
            final BindingGroup bindingGroup) {
        this.component = component;
        this.element = element;
        this.validateElement = element.getValidateElement();
        this.bindingGroup = bindingGroup;
        this.eagerSubmitRunnable = newEagerSubmitRunnable();
        this.originalBorder = component.getBorder();
    }

    protected BindingGroup getBindingGroup() {
        return bindingGroup;
    }

    private Runnable newEagerSubmitRunnable() {
        if (isEager()) {
            if (isForced()) {
                return new Runnable() {
                    private final BindingSubmitAllViewsHelper helper = new BindingSubmitAllViewsHelper(
                            AComponentBinding.this) {
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

                    private final BindingSubmitAllViewsHelper helper = new BindingSubmitAllViewsHelper(
                            AComponentBinding.this) {
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
        if (isFrozen()) {
            return;
        }
        if (!isModifiable()) {
            setInvalidMessage(null);
            return;
        }
        final AModel model = bindingGroup.getModel();
        try {
            prevModelValue = Optional.ofNullable(getValueFromRoot(model));
            final V newModelValue = fromComponentToModel();
            if (validateElement != null) {
                final String invalid = validateElement.validateFromRoot(model, newModelValue);
                if (Strings.isNotBlank(invalid)) {
                    setInvalidMessage(invalid);
                    return;
                }
            }
            setValueFromRoot(model, newModelValue);
            setInvalidMessage(null);
            submitted = true;
        } catch (final Throwable t) {
            Err.process(t);
            setInvalidMessage(t.getLocalizedMessage());
            submitted = false;
        }
    }

    protected abstract IBeanPathPropertyModifier<V> getModifier();

    @Override
    public String validate() {
        if (isFrozen()) {
            return invalidMessage;
        }
        if (!isModifiable()) {
            return invalidMessage;
        }
        if (invalidMessage != null) {
            return invalidMessage;
        }
        if (validateElement != null) {
            //validate using custom validator only once all properties have been synchronized
            final AModel model = bindingGroup.getModel();
            final Object modelValue = getValueFromRoot(model);
            final String invalid = validateElement.validateFromRoot(model, modelValue);
            if (Strings.isNotBlank(invalid)) {
                setInvalidMessage(invalid);
                return invalidMessage;
            }
        }
        return null;
    }

    @Override
    public void setInvalidMessage(final String invalidMessage) {
        if (Strings.isNotBlank(invalidMessage)) {
            this.invalidMessage = normalizeInvalidMessage(invalidMessage);
        } else {
            this.invalidMessage = null;
        }
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
        if (isFrozen()) {
            return;
        }
        if (invalidMessage != null) {
            rollback();
        }
        if (!submitted) {
            return;
        }
        submitted = false;
        invalidMessage = null;
        showingInvalidMessage = null;
    }

    @Override
    public void rollback() {
        if (isFrozen()) {
            return;
        }
        if (!isModifiable()) {
            return;
        }
        if (!submitted) {
            return;
        }
        if (invalidMessage == null) {
            //keep valid values, only roll back issues
            commit();
            return;
        }
        final AModel model = bindingGroup.getModel();
        try {
            setValueFromRoot(model, prevModelValue.orElse(null));
        } catch (final Throwable t) {
            Err.process(t);
            setInvalidMessage(t.getLocalizedMessage());
        }
        submitted = false;
        showingInvalidMessage = invalidMessage;
        invalidMessage = null;
    }

    protected void setValueFromRoot(final AModel root, final V value) {
        getModifier().setValueFromRoot(root, value);
    }

    @Override
    public void update() {
        if (isFrozen()) {
            return;
        }
        updating = true;
        try {
            final AModel model = bindingGroup.getModel();
            final V modelValue = getValueFromRoot(model);
            prevModelValue = fromModelToComponent(modelValue);

            final Object target = getTarget();
            setEnabled(element.isEnabled(target));
            Components.setVisible(component, element.isVisible(target));

            updateValidation(target);
        } finally {
            updating = false;
        }
    }

    protected void updateValidation(final Object target) {
        if (showingInvalidMessage != null) {
            setBorder(INVALID_MESSAGE_BORDER);
            String combinedToolTip = bindingGroup.i18n(element.getTooltip(target));
            if (Strings.isNotBlank(combinedToolTip)) {
                combinedToolTip += "\n\n" + showingInvalidMessage;
            } else {
                combinedToolTip = showingInvalidMessage;
            }
            setToolTipText(combinedToolTip);
        } else {
            setBorder(originalBorder);
            setToolTipText(bindingGroup.i18n(element.getTooltip(target)));
        }
    }

    protected void setBorder(final Border border) {
        Components.setBorder(component, border);
    }

    protected void setToolTipText(final String toolTipText) {
        Components.setToolTipText(component, toolTipText);
    }

    protected void setEnabled(final boolean enabled) {
        Components.setEnabled(component, enabled);
    }

    protected V getValueFromRoot(final AModel model) {
        return getModifier().getValueFromRoot(model);
    }

    protected Object getTarget() {
        final BeanObjectContainer container = (BeanObjectContainer) element.getContainer();
        return container.getObject();
    }

    protected boolean isModifiable() {
        return component.isVisible() && component.isEnabled();
    }

    protected abstract Optional<V> fromModelToComponent(V modelValue);

    protected abstract V fromComponentToModel();

    protected String normalizeInvalidMessage(final String m) {
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

    protected String renderChoice(final Object choice) {
        if (choice == null) {
            return null;
        } else if (choice instanceof Enum) {
            final Enum<?> cChoice = (Enum<?>) choice;
            final String id = choice.getClass().getSuperclass().getSimpleName() + "." + cChoice.name();
            final String choiceStr = bindingGroup.i18n(id, choice.toString());
            return choiceStr;
        } else {
            final String choiceStr = bindingGroup.i18n(choice.toString());
            return choiceStr;
        }
    }

    public void setFrozen(final boolean frozen) {
        this.frozen = frozen;
    }

    public boolean isFrozen() {
        return frozen;
    }

}
