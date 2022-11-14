package de.invesdwin.context.client.swing.api.binding.component;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.view.IDockable;
import de.invesdwin.norva.beanpath.spi.element.RootBeanPathElement;
import de.invesdwin.util.lang.Objects;

@NotThreadSafe
public class RootTitleBinding implements IComponentBinding {

    private final BindingGroup bindingGroup;

    public RootTitleBinding(final BindingGroup bindingGroup) {
        this.bindingGroup = bindingGroup;
    }

    @Override
    public String getBeanPath() {
        return RootBeanPathElement.ROOT_BEAN_PATH;
    }

    @Override
    public void submit() {}

    @Override
    public void setInvalidMessage(final String invalidMessage) {}

    @Override
    public String getInvalidMessage() {
        return null;
    }

    @Override
    public String validate(final boolean force) {
        return null;
    }

    @Override
    public void commit() {}

    @Override
    public void rollback() {}

    @Override
    public void update() {
        final IDockable dockable = bindingGroup.getView().getDockable();
        if (dockable != null) {
            final String prevTitle = dockable.getTitle();
            final String newTitle = bindingGroup.getView().getTitle();
            if (!Objects.equals(prevTitle, newTitle)) {
                //ensure we are running inside EDT, this binding is an exception to check this here
                try {
                    EventDispatchThreadUtil.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            dockable.setTitle(newTitle);
                        }
                    });
                } catch (final InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void reset() {
        //noop
    }
}
