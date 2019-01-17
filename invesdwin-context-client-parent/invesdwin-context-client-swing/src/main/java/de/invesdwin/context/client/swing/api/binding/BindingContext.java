package de.invesdwin.context.client.swing.api.binding;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class BindingContext {

    private final List<IBinding> bindings = new ArrayList<>();

    public void add(final IBinding binding) {
        bindings.add(binding);
    }

    public void update() {
        for (int i = 0; i < bindings.size(); i++) {
            bindings.get(i).update();
        }
    }

}
