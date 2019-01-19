package de.invesdwin.context.client.swing.api.binding.internal.components;

import java.awt.Component;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JComponent;

import com.jgoodies.common.base.Strings;

import de.invesdwin.context.client.swing.api.AModel;
import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.IBinding;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.util.SubmitAllViewsHelper;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContainer;
import de.invesdwin.norva.beanpath.spi.element.APropertyBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.utility.ValidateBeanPathElement;
import de.invesdwin.util.lang.Objects;

@NotThreadSafe
public abstract class AComponentBinding<C extends JComponent> implements IBinding {

    protected final C component;
    protected final APropertyBeanPathElement element;
    protected final ValidateBeanPathElement validateElement;
    protected final BindingGroup bindingGroup;
    protected final Runnable eagerSubmitRunnable;
    protected Object prevModelValue;
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
                        protected boolean validate(final List<AView<?, ?>> views) {
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
        prevModelValue = element.getModifier().getValueFromRoot(model);
        final Object newModelValue = fromComponentToModel();
        if (!Objects.equals(prevModelValue, newModelValue)) {
            try {
                element.getModifier().setValueFromRoot(model, newModelValue);
                invalidMessage = null;
                submitted = true;
            } catch (final Throwable t) {
                invalidMessage = element.getTitle(getTarget()) + ": " + t.toString();
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
            final AModel model = bindingGroup.getModel();
            final Object modelValue = element.getModifier().getValueFromRoot(model);
            final String invalid = validateElement.validate(modelValue);
            if (Strings.isNotBlank(invalid)) {
                invalidMessage = element.getTitle(getTarget()) + ": " + invalid;
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
        final AModel model = bindingGroup.getModel();
        element.getModifier().setValueFromRoot(model, prevModelValue);
        prevModelValue = null;
        submitted = false;
        invalidMessage = null;
    }

    @Override
    public void update() {
        final AModel model = bindingGroup.getModel();
        final Object modelValue = element.getModifier().getValueFromRoot(model);
        fromModelToComponent(modelValue);

        final Object target = getTarget();
        component.setEnabled(element.isEnabled(target));
        component.setVisible(element.isVisible(target));
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

    protected abstract void fromModelToComponent(Object modelValue);

    protected abstract Object fromComponentToModel();

}
