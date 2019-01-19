package de.invesdwin.context.client.swing.api.binding.component.button;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.AbstractButton;

import de.invesdwin.context.client.swing.api.AModel;
import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.component.IComponentBinding;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.util.SubmitAllViewsHelper;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContainer;
import de.invesdwin.norva.beanpath.spi.element.AActionBeanPathElement;
import de.invesdwin.util.assertions.Assertions;

@NotThreadSafe
public class SubmitButtonBinding implements IComponentBinding {

    private final AbstractButton component;
    private final AActionBeanPathElement element;
    private final BindingGroup bindingGroup;
    private final Runnable submitRunnable;

    public SubmitButtonBinding(final AbstractButton component, final AActionBeanPathElement element,
            final BindingGroup bindingGroup) {
        this.component = component;
        this.element = element;
        this.bindingGroup = bindingGroup;
        this.submitRunnable = newSubmitRunnable();
        component.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                submitRunnable.run();
            }
        });
    }

    private Runnable newSubmitRunnable() {
        if (isForced()) {
            return new Runnable() {
                private final SubmitAllViewsHelper helper = new SubmitAllViewsHelper() {
                    @Override
                    public void process(final Component component) {
                        if (!isModifiable()) {
                            return;
                        }
                        super.process(component);
                    }

                    @Override
                    protected boolean validate(final List<AView<?, ?>> views) {
                        //ignore all validation errors
                        invoke();
                        return true;
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

                    @Override
                    protected boolean validate(final List<AView<?, ?>> views) {
                        final boolean valid = super.validate(views);
                        if (valid) {
                            invoke();
                            //validate again after invoking
                            return super.validate(views);
                        }
                        return valid;
                    }
                };

                @Override
                public void run() {
                    helper.process(component);
                }
            };
        }
    }

    private void invoke() {
        try {
            final AModel model = bindingGroup.getModel();
            final Object result = element.getInvoker().invokeFromRoot(model);
            processResult(result);
        } catch (final UndeclaredThrowableException e) {
            handleButtonException(component, e.getUndeclaredThrowable());
        } catch (final Throwable t) {
            handleButtonException(component, t);
        }
    }

    protected void processResult(final Object result) {
        if (shouldHideModalView()) {
            GuiService.get().hideModalView();
        }
        if (result != null) {
            Assertions.assertThat(result).isInstanceOf(AView.class);
            final AView<?, ?> view = (AView<?, ?>) result;
            if (element.isModalOpener()) {
                GuiService.get().showModalView(view);
            } else {
                GuiService.get().showView(view);
            }
        }
    }

    protected boolean shouldHideModalView() {
        return GuiService.get().isModalViewShowing() && element.isModalCloser();
    }

    protected void handleButtonException(final Component component, final Throwable t) {
        bindingGroup.getSubmitButtonExceptionHandler().handleSubmitButtonException(element, component, t);
    }

    protected boolean isModifiable() {
        return component.isVisible() && component.isEnabled();
    }

    protected boolean isForced() {
        return element.isForced();
    }

    @Override
    public void submit() {
        //noop
    }

    @Override
    public boolean validate() {
        //noop
        return true;
    }

    @Override
    public void commit() {
        //noop
    }

    @Override
    public void rollback() {
        //noop
    }

    @Override
    public void update() {
        final Object target = getTarget();
        component.setText(element.getTitle(target));
        component.setEnabled(element.isEnabled(target));
        component.setVisible(element.isVisible(target));
    }

    protected Object getTarget() {
        final AModel model = bindingGroup.getModel();
        final BeanClassContainer container = (BeanClassContainer) element.getContainer();
        final Object target = container.getObjectFromRoot(model);
        return target;
    }

}
