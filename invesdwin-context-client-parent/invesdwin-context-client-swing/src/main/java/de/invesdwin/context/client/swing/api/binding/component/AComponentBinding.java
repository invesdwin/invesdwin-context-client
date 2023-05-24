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
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContainer;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.simple.modifier.IBeanPathPropertyModifier;
import de.invesdwin.norva.beanpath.spi.element.utility.ValidateBeanPathElement;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.lang.string.Strings;
import de.invesdwin.util.swing.Components;
import de.invesdwin.util.swing.MouseEnteredListener;

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
    protected final MouseEnteredListener mouseEnteredListener;
    protected Optional<V> prevModelValue;
    protected boolean submitted;
    protected String invalidMessage;
    protected String showingInvalidMessage;
    protected boolean updating;
    protected boolean frozen;

    public AComponentBinding(final C component, final APropertyBeanPathElement element,
            final BindingGroup bindingGroup) {
        this.component = component;
        this.element = element;
        this.validateElement = element.getValidateElement();
        this.bindingGroup = bindingGroup;
        this.eagerSubmitRunnable = newEagerSubmitRunnable();
        this.originalBorder = component.getBorder();
        this.mouseEnteredListener = MouseEnteredListener.get(component);
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
                        public void process(final AView<?, ?> view, final Component component) {
                            if (!isModifiable()) {
                                return;
                            }
                            super.process(view, component);
                        }

                    };

                    @Override
                    public void run() {
                        if (!updating) {
                            helper.process(bindingGroup.getView(), component);
                        }
                    }
                };

            } else {
                return new Runnable() {

                    private final BindingSubmitAllViewsHelper helper = new BindingSubmitAllViewsHelper(
                            AComponentBinding.this) {
                        @Override
                        public void process(final AView<?, ?> view, final Component component) {
                            if (!isModifiable()) {
                                return;
                            }
                            super.process(view, component);
                        }
                    };

                    @Override
                    public void run() {
                        if (!updating) {
                            helper.process(bindingGroup.getView(), component);
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
            final V updatedModelValue = getValueFromRoot(model);
            if (!Objects.equals(newModelValue, updatedModelValue)) {
                //force update
                prevModelValue = Optional.empty();
                //model might derive some other value to display, sync it to component as a response
                prevModelValue = fromModelToComponent(updatedModelValue);
            }
            submitted = true;
        } catch (final Exception e) {
            setInvalidMessage(exceptionToString(e));
            submitted = false;
        } catch (final Throwable t) {
            Err.process(t);
            setInvalidMessage(exceptionToString(t));
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
        invalidMessage = null;
        showingInvalidMessage = null;
        final AModel model = bindingGroup.getModel();
        final String invalid = newInvalidMessage(model);
        if (Strings.isNotBlank(invalid)) {
            setInvalidMessage(invalid);
            return invalidMessage;
        }
        return null;
    }

    private String newInvalidMessage(final AModel model) {
        try {
            final V newModelValue = fromComponentToModel();
            if (validateElement != null) {
                final String invalid = validateElement.validateFromRoot(model, newModelValue);
                return invalid;
            } else {
                return null;
            }
        } catch (final Exception e) {
            return exceptionToString(e);
        } catch (final Throwable t) {
            Err.process(t);
            return exceptionToString(t);
        }
    }

    protected String exceptionToString(final Throwable t) {
        final String str = t.getLocalizedMessage();
        if (Strings.isBlank(str)) {
            return t.getClass().getSimpleName();
        } else {
            return str;
        }
    }

    @Override
    public final void reset() {
        if (isFrozen()) {
            return;
        }
        if (!isModifiable()) {
            return;
        }
        submitted = false;
        showingInvalidMessage = null;
        invalidMessage = null;
        resetCaches();
        update();
    }

    /**
     * Ensures that the next call to update will not be skipped due to lazy checks
     */
    protected abstract void resetCaches();

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
            showingInvalidMessage = invalidMessage;
            invalidMessage = null;
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
            setInvalidMessage(exceptionToString(t));
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
            final AModel root = getRoot();
            final V modelValue = getValueFromRoot(root);
            prevModelValue = fromModelToComponent(modelValue);

            final Object target = getTarget();
            setEnabled(element.isEnabledFromTarget(root, target));
            Components.setVisible(component, element.isVisibleFromTarget(root, target));
            updateValidation(root);
        } finally {
            updating = false;
        }
    }

    protected void updateValidation(final Object root) {
        if (invalidMessage != null) {
            showingInvalidMessage = invalidMessage;
            invalidMessage = null;
        }
        if (showingInvalidMessage != null) {
            setBorder(INVALID_MESSAGE_BORDER);
            String combinedToolTip = bindingGroup.i18n(element.getTooltipFromRoot(root));
            if (Strings.isNotBlank(combinedToolTip)) {
                combinedToolTip += "\n\n" + showingInvalidMessage;
            } else {
                combinedToolTip = showingInvalidMessage;
            }
            setToolTipText(combinedToolTip);
        } else {
            setBorder(originalBorder);
            setToolTipText(bindingGroup.i18n(element.getTooltipFromRoot(root)));
        }
    }

    protected void setBorder(final Border border) {
        Components.setBorder(component, border);
    }

    protected void setToolTipText(final String toolTipText) {
        Components.setToolTipText(component, toolTipText, mouseEnteredListener.isMouseEntered());
    }

    protected void setEnabled(final boolean enabled) {
        Components.setEnabled(component, enabled);
    }

    protected V getValueFromRoot(final AModel model) {
        return getModifier().getValueFromRoot(model);
    }

    protected AModel getRoot() {
        return bindingGroup.getModel();
    }

    protected Object getTarget() {
        final BeanClassContainer container = element.getContainer().unwrap(BeanClassContainer.class);
        return container.getTargetFromRoot(getRoot());
    }

    protected boolean isModifiable() {
        return component.isVisible() && component.isEnabled();
    }

    protected abstract Optional<V> fromModelToComponent(V modelValue);

    protected abstract V fromComponentToModel() throws Exception;

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
        return bindingGroup.getTitleFromTarget(element, getTarget());
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

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(component.getName()).toString();
    }

}
