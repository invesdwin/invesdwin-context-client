package de.invesdwin.context.client.swing.api.binding;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import com.jgoodies.common.base.Strings;

import de.invesdwin.context.beans.validator.BeanValidator;
import de.invesdwin.context.client.swing.api.AModel;
import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.api.binding.component.IComponentBinding;
import de.invesdwin.context.client.swing.api.binding.component.button.ISubmitButtonExceptionHandler;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContext;
import de.invesdwin.norva.beanpath.spi.element.RootBeanPathElement;
import de.invesdwin.util.collections.fast.AFastIterableDelegateList;
import de.invesdwin.util.collections.loadingcache.ALoadingCache;

@NotThreadSafe
public class BindingGroup implements IComponentBinding {

    private final ALoadingCache<String, List<IComponentBinding>> beanPath_binding = new ALoadingCache<String, List<IComponentBinding>>() {
        @Override
        protected List<IComponentBinding> loadValue(final String key) {
            return new ArrayList<>();
        }
    };
    private final AFastIterableDelegateList<IComponentBinding> bindings = new AFastIterableDelegateList<IComponentBinding>() {
        @Override
        protected List<IComponentBinding> newDelegate() {
            return new ArrayList<>();
        }
    };

    private final AView<?, ?> view;
    private final BeanClassContext modelContext;
    private final ISubmitButtonExceptionHandler submitButtonExceptionHandler;
    private String invalidMessage = null;

    public BindingGroup(final AView<?, ?> view, final BeanClassContext modelContext,
            final ISubmitButtonExceptionHandler submitButtonExceptionHandler) {
        this.view = view;
        this.modelContext = modelContext;
        this.submitButtonExceptionHandler = submitButtonExceptionHandler;
    }

    public AView<?, ?> getView() {
        return view;
    }

    public AModel getModel() {
        return view.getModel();
    }

    public BeanClassContext getModelContext() {
        return modelContext;
    }

    public void add(final IComponentBinding binding) {
        beanPath_binding.get(binding.getBeanPath()).add(binding);
    }

    @Override
    public void submit() {
        final IComponentBinding[] array = bindings.asArray(IComponentBinding.class);
        for (int i = 0; i < array.length; i++) {
            array[i].submit();
        }
    }

    @Override
    public String validate() {
        String combinedInvalidMessage = null;
        final ConstraintViolationException exception = BeanValidator.getInstance().validate(getModel());
        if (exception != null) {
            for (final ConstraintViolation<?> violation : exception.getConstraintViolations()) {
                final String beanPath = violation.getPropertyPath().toString();
                final String invalidMessage = violation.getMessage();
                if (beanPath_binding.containsKey(beanPath)) {
                    final List<IComponentBinding> bindings = beanPath_binding.get(beanPath);
                    for (int i = 0; i < bindings.size(); i++) {
                        bindings.get(i).setInvalidMessage(invalidMessage);
                    }
                } else {
                    if (combinedInvalidMessage != null) {
                        combinedInvalidMessage += "\n";
                        combinedInvalidMessage += invalidMessage;
                    } else {
                        combinedInvalidMessage = invalidMessage;
                    }
                }
            }
        }
        final IComponentBinding[] array = bindings.asArray(IComponentBinding.class);
        for (int i = 0; i < array.length; i++) {
            final String invalidMessage = array[i].validate();
            if (Strings.isNotBlank(invalidMessage)) {
                if (combinedInvalidMessage != null) {
                    combinedInvalidMessage += "\n";
                    combinedInvalidMessage += invalidMessage;
                } else {
                    combinedInvalidMessage = invalidMessage;
                }
            }
        }
        this.invalidMessage = combinedInvalidMessage;
        return combinedInvalidMessage;
    }

    @Override
    public String getInvalidMessage() {
        return invalidMessage;
    }

    @Override
    public void setInvalidMessage(final String invalidMessage) {
        this.invalidMessage = invalidMessage;
    }

    @Override
    public void commit() {
        final IComponentBinding[] array = bindings.asArray(IComponentBinding.class);
        for (int i = 0; i < array.length; i++) {
            array[i].commit();
        }
    }

    @Override
    public void rollback() {
        final IComponentBinding[] array = bindings.asArray(IComponentBinding.class);
        for (int i = 0; i < array.length; i++) {
            array[i].rollback();
        }
    }

    @Override
    public void update() {
        final IComponentBinding[] array = bindings.asArray(IComponentBinding.class);
        for (int i = 0; i < array.length; i++) {
            array[i].update();
        }
    }

    public ISubmitButtonExceptionHandler getSubmitButtonExceptionHandler() {
        return submitButtonExceptionHandler;
    }

    @Override
    public String getBeanPath() {
        return RootBeanPathElement.ROOT_BEAN_PATH;
    }
}
