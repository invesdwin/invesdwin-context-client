package de.invesdwin.context.client.swing.api.binding;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContext;

@NotThreadSafe
public class BindingGroup implements IBinding {

    private final List<IBinding> bindings = new ArrayList<>();
    private final BeanClassContext context;

    public BindingGroup(final BeanClassContext context) {
        this.context = context;
    }

    public BeanClassContext getContext() {
        return context;
    }

    public void add(final IBinding binding) {
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
}
