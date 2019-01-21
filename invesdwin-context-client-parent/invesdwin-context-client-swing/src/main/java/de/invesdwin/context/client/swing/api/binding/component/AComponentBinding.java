package de.invesdwin.context.client.swing.api.binding.component;

import java.awt.Component;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JComponent;

import com.jgoodies.common.base.Strings;

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

@NotThreadSafe
public abstract class AComponentBinding<C extends JComponent, V> implements IComponentBinding {

    protected final C component;
    protected final APropertyBeanPathElement element;
    protected final ValidateBeanPathElement validateElement;
    protected final BindingGroup bindingGroup;
    protected final Runnable eagerSubmitRunnable;
    protected V prevModelValue;
    protected boolean submitted = false;
    protected String invalidMessage = null;

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
                        helper.process(component);
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
                        helper.process(component);
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
        prevModelValue = getModifier().getValueFromRoot(model);
        final V newModelValue = fromComponentToModel();
        if (!Objects.equals(prevModelValue, newModelValue)) {
            try {
                getModifier().setValueFromRoot(model, newModelValue);
                invalidMessage = null;
                submitted = true;
            } catch (final Throwable t) {
                Err.process(t);
                invalidMessage = element.getTitle(getTarget()) + ": " + t.toString();
                submitted = false;
            }
        } else {
            prevModelValue = null;
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
            final String invalid = validateElement.validate(modelValue);
            if (Strings.isNotBlank(invalid)) {
                invalidMessage = element.getTitle(getTarget()) + ": " + invalid;
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
        prevModelValue = null;
        submitted = false;
        invalidMessage = null;
    }

    @Override
    public void rollback() {
        if (!submitted && invalidMessage == null) {
            return;
        }
        final AModel model = bindingGroup.getModel();
        getModifier().setValueFromRoot(model, prevModelValue);
        prevModelValue = null;
        submitted = false;
        invalidMessage = null;
    }

    @Override
    public void update() {
        final AModel model = bindingGroup.getModel();
        final V modelValue = getModifier().getValueFromRoot(model);
        fromModelToComponent(modelValue);

        final Object target = getTarget();
        component.setEnabled(element.isEnabled(target));
        component.setVisible(element.isVisible(target));
        component.setToolTipText(element.getTooltip(target));
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

}
