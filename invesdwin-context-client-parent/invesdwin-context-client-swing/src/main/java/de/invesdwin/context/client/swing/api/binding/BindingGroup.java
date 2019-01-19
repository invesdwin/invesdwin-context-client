package de.invesdwin.context.client.swing.api.binding;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.swing.api.AModel;
import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.api.binding.component.IComponentBinding;
import de.invesdwin.context.client.swing.api.binding.component.button.ISubmitButtonExceptionHandler;
import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContext;

@NotThreadSafe
public class BindingGroup implements IComponentBinding {

    private final List<IComponentBinding> bindings = new ArrayList<>();
    private final AView<?, ?> view;
    private final BeanClassContext modelContext;
    private final ISubmitButtonExceptionHandler submitButtonExceptionHandler;

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
        bindings.add(binding);
    }

    @Override
    public void submit() {
        for (int i = 0; i < bindings.size(); i++) {
            bindings.get(i).submit();
        }
    }

    @Override
    public boolean validate() {
        boolean valid = true;
        for (int i = 0; i < bindings.size(); i++) {
            if (!bindings.get(i).validate()) {
                valid = false;
            }
        }
        return valid;
    }

    @Override
    public void commit() {
        for (int i = 0; i < bindings.size(); i++) {
            bindings.get(i).commit();
        }
    }

    @Override
    public void rollback() {
        for (int i = 0; i < bindings.size(); i++) {
            bindings.get(i).rollback();
        }
    }

    @Override
    public void update() {
        for (int i = 0; i < bindings.size(); i++) {
            bindings.get(i).update();
        }
    }

    public ISubmitButtonExceptionHandler getSubmitButtonExceptionHandler() {
        return submitButtonExceptionHandler;
    }
}
