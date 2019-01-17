package de.invesdwin.context.client.swing.api.binding;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.norva.beanpath.impl.clazz.BeanClassContext;

@NotThreadSafe
public class BindingGroup {

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

    public void update() {
        for (int i = 0; i < bindings.size(); i++) {
            bindings.get(i).update();
        }
    }

}
