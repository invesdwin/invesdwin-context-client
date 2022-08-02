package de.invesdwin.context.client.swing.api.binding.component.button;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.AbstractButton;
import javax.swing.border.Border;

import de.invesdwin.context.client.swing.api.annotation.DefaultCloseOperation;
import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.component.AComponentBinding;
import de.invesdwin.context.client.swing.api.binding.component.IComponentBinding;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.context.client.swing.util.SubmitAllViewsHelper;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContainer;
import de.invesdwin.norva.beanpath.spi.element.AActionBeanPathElement;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.lang.Strings;
import de.invesdwin.util.swing.Components;
import de.invesdwin.util.swing.MouseEnteredListener;

@NotThreadSafe
public class SubmitButtonBinding implements IComponentBinding {

    private final AbstractButton component;
    private final AActionBeanPathElement element;
    private final BindingGroup bindingGroup;
    private final Runnable submitRunnable;
    private final Border originalBorder;
    private String showingInvalidMessage;
    private final MouseEnteredListener mouseEnteredListener;

    public SubmitButtonBinding(final AbstractButton component, final AActionBeanPathElement element,
            final BindingGroup bindingGroup) {
        this.component = component;
        this.element = element;
        this.bindingGroup = bindingGroup;
        this.submitRunnable = newSubmitRunnable();
        component.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final boolean registerSubmitButtonRunning = bindingGroup.registerSubmitButtonRunning();
                try {
                    submitRunnable.run();
                } finally {
                    bindingGroup.unregisterSubmitButtonRunning(registerSubmitButtonRunning);
                }

            }
        });
        this.originalBorder = component.getBorder();
        this.mouseEnteredListener = MouseEnteredListener.get(component);
    }

    private Runnable newSubmitRunnable() {
        if (isForced()) {
            return new Runnable() {
                private final SubmitAllViewsHelper helper = new SubmitAllViewsHelper() {
                    @Override
                    public void process(final AView<?, ?> view, final Component component) {
                        if (!isModifiable()) {
                            return;
                        }
                        super.process(view, component);
                    }

                    @Override
                    protected String validate(final List<AView<?, ?>> views) {
                        //ignore all validation errors
                        invoke();
                        return null;
                    }
                };

                @Override
                public void run() {
                    helper.process(bindingGroup.getView(), component);
                }
            };

        } else {
            return new Runnable() {
                private final SubmitAllViewsHelper helper = new SubmitAllViewsHelper() {
                    @Override
                    public void process(final AView<?, ?> view, final Component component) {
                        if (!isModifiable()) {
                            return;
                        }
                        super.process(view, component);
                    }

                    @Override
                    protected String validate(final List<AView<?, ?>> views) {
                        final String invalidMessage = super.validate(views);
                        if (invalidMessage != null) {
                            showingInvalidMessage = invalidMessage;
                            return invalidMessage;
                        } else {
                            invoke();
                            //validate again after invoking
                            return super.validate(views);
                        }
                    }
                };

                @Override
                public void run() {
                    helper.process(bindingGroup.getView(), component);
                }
            };
        }
    }

    private void invoke() {
        try {
            final Object root = getRoot();
            final Object result = element.getInvoker().invokeFromRoot(root);
            processResult(result);
        } catch (final UndeclaredThrowableException e) {
            handleButtonException(component, e.getUndeclaredThrowable());
        } catch (final Throwable t) {
            handleButtonException(component, t);
        }
    }

    @Override
    public String getBeanPath() {
        return element.getBeanPath();
    }

    protected void processResult(final Object result) {
        if (shouldHideModalView()) {
            GuiService.get().hideModalView();
        } else if (shouldRemoveView()) {
            GuiService.get().getContentPane().removeView(bindingGroup.getView());
        }
        if (result != null) {
            Assertions.assertThat(result).isInstanceOf(AView.class);
            final AView<?, ?> view = (AView<?, ?>) result;
            if (element.isModalOpener()) {
                GuiService.get().showModalView(view);
            } else {
                GuiService.get().showView(view, null);
            }
        }
    }

    private boolean shouldRemoveView() {
        return element.isModalCloser();
    }

    protected boolean shouldHideModalView() {
        return GuiService.get().isModalViewShowing() && element.isModalCloser();
    }

    protected void handleButtonException(final Component component, final Throwable t) {
        bindingGroup.getSubmitButtonExceptionHandler().handleSubmitButtonException(component, t);
    }

    protected boolean isModifiable() {
        return component.isVisible() && component.isEnabled();
    }

    protected boolean isForced() {
        return element.isForced();
    }

    @Override
    public void submit() {
        showingInvalidMessage = null;
    }

    @Override
    public String validate() {
        //noop
        return null;
    }

    @Override
    public String getInvalidMessage() {
        //noop
        return null;
    }

    @Override
    public void setInvalidMessage(final String message) {
        //noop
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
        final Object root = getRoot();
        final Object target = getTarget();
        final String title = bindingGroup.getTitleFromTarget(element, target);
        Components.setText(component, title);
        Components.setEnabled(component, element.isEnabledFromTarget(root, target));
        Components.setVisible(component, element.isVisibleFromTarget(root, target));
        if (showingInvalidMessage != null) {
            Components.setBorder(component, AComponentBinding.INVALID_MESSAGE_BORDER);
            String combinedTooltip = bindingGroup.i18n(element.getTooltipFromTarget(root, target));
            if (Strings.isNotBlank(combinedTooltip)) {
                combinedTooltip += "\n\n" + showingInvalidMessage;
            } else {
                combinedTooltip = showingInvalidMessage;
            }
            Components.setToolTipText(component, combinedTooltip, mouseEnteredListener.isMouseEntered());
        } else {
            Components.setBorder(component, originalBorder);
            Components.setToolTipText(component, bindingGroup.i18n(element.getTooltipFromTarget(root, target)),
                    mouseEnteredListener.isMouseEntered());
        }
    }

    protected Object getTarget() {
        final BeanClassContainer container = element.getContainer().unwrap(BeanClassContainer.class);
        return container.getTargetFromRoot(getRoot());
    }

    protected Object getRoot() {
        return bindingGroup.getModel();
    }

    public boolean isDefaultCloseOperation() {
        return isDefaultCloseOperation(element);
    }

    public static boolean isDefaultCloseOperation(final AActionBeanPathElement element) {
        if (element.getAccessor().getAnnotation(DefaultCloseOperation.class) != null) {
            return true;
        }
        return false;
    }

    public void doClick() {
        component.doClick();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(component.getName()).toString();
    }

}
