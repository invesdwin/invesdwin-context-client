package de.invesdwin.context.client.swing.api.binding.component;

import java.util.List;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.context.client.swing.util.SubmitAllViewsHelper;

@Immutable
public class BindingSubmitAllViewsHelper extends SubmitAllViewsHelper {

    private final AComponentBinding<?, ?> binding;

    public BindingSubmitAllViewsHelper(final AComponentBinding<?, ?> binding) {
        this.binding = binding;
    }

    @Override
    protected void submit(final List<AView<?, ?>> views) {
        binding.setFrozen(true);
        try {
            super.submit(views);
        } finally {
            binding.setFrozen(false);
        }
        binding.submit();
    }

    @Override
    protected void commit(final List<AView<?, ?>> views) {
        binding.setFrozen(true);
        try {
            super.commit(views);
        } finally {
            binding.setFrozen(false);
        }
        binding.commit();
    }

    @Override
    protected void rollback(final List<AView<?, ?>> views) {
        binding.setFrozen(true);
        try {
            super.rollback(views);
        } finally {
            binding.setFrozen(false);
        }
        binding.rollback();
    }

    @Override
    protected void update(final List<AView<?, ?>> views) {
        binding.setFrozen(true);
        try {
            super.update(views);
        } finally {
            binding.setFrozen(false);
        }
        binding.update();
    }

}
