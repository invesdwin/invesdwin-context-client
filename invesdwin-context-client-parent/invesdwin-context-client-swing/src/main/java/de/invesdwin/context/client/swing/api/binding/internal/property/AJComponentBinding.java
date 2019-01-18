package de.invesdwin.context.client.swing.api.binding.internal.property;

import java.awt.Component;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JComponent;

import com.jgoodies.common.base.Strings;

import de.invesdwin.context.client.swing.api.AModel;
import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.api.binding.IBinding;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.util.SubmitAllViewsHelper;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContainer;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.utility.ValidateBeanPathElement;
import de.invesdwin.util.lang.Objects;

@NotThreadSafe
public abstract class AJComponentBinding<C extends JComponent> implements IBinding {

    protected final C component;
    protected final APropertyBeanPathElement element;
    protected final ValidateBeanPathElement validateElement;
    protected final AModel model;
    protected final Runnable eagerSubmitRunnable;
    protected Object prevModelValue;
    protected boolean submitted = false;
    protected String invalidMessage = null;

    public AJComponentBinding(final C component, final APropertyBeanPathElement element, final AModel model) {
        this.component = component;
        this.element = element;
        this.validateElement = element.getValidateElement();
        this.model = model;
        this.eagerSubmitRunnable = newEagerSubmitRunnable();
    }

    private Runnable newEagerSubmitRunnable() {
        if (element.isEager()) {
            if (element.isForced()) {
                return new Runnable() {
                    private final SubmitAllViewsHelper helper = new SubmitAllViewsHelper() {
                        @SuppressWarnings("unused")
                        protected boolean validate(final AView<?, ?> views) {
                            //only show conversion errors, ignore any other validation errors
                            if (Strings.isNotBlank(invalidMessage)) {
                                GuiService.get().getStatusBar().error(invalidMessage);
                                return false;
                            }
                            return true;
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

    @Override
    public void submit() {
        if (!isModifiable()) {
            return;
        }
        prevModelValue = element.getModifier().getValueFromRoot(model);
        final Object newModelValue = fromComponentToModel();
        if (!Objects.equals(prevModelValue, newModelValue)) {
            try {
                element.getModifier().setValueFromRoot(model, newModelValue);
                invalidMessage = null;
                submitted = true;
            } catch (final Throwable t) {
                invalidMessage = element.getTitle() + ": " + t.toString();
                submitted = false;
            }
        } else {
            prevModelValue = null;
            submitted = false;
        }
    }

    @Override
    public boolean validate() {
        if (Strings.isNotBlank(invalidMessage)) {
            GuiService.get().getStatusBar().error(invalidMessage);
            return false;
        }
        if (validateElement != null) {
            //validate using custom validator only once all properties have been synchronized
            final Object modelValue = element.getModifier().getValueFromRoot(model);
            final String invalid = validateElement.validate(modelValue);
            if (Strings.isNotBlank(invalid)) {
                invalidMessage = element.getTitle() + ": " + invalid;
                GuiService.get().getStatusBar().error(invalidMessage);
                return false;
            }
        }
        return true;
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
        element.getModifier().setValueFromRoot(model, prevModelValue);
        prevModelValue = null;
        submitted = false;
        invalidMessage = null;
    }

    @Override
    public void update() {
        final Object modelValue = element.getModifier().getValueFromRoot(model);
        fromModelToComponent(modelValue);

        final BeanClassContainer container = (BeanClassContainer) element.getContainer();
        final Object target = container.getObjectFromRoot(model);
        component.setEnabled(element.isEnabled(target));
        component.setVisible(element.isVisible(target));
    }

    protected boolean isModifiable() {
        return component.isVisible() && component.isEnabled();
    }

    protected abstract void fromModelToComponent(Object modelValue);

    protected abstract Object fromComponentToModel();

}
