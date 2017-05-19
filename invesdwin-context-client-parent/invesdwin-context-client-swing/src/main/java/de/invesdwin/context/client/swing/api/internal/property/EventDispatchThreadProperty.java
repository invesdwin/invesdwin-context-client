package de.invesdwin.context.client.swing.api.internal.property;

import javax.annotation.concurrent.NotThreadSafe;

import org.jdesktop.beansbinding.Property;
import org.jdesktop.beansbinding.PropertyStateListener;

import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;

/**
 * Write operations must run in EDT.
 */
@NotThreadSafe
public class EventDispatchThreadProperty<S, V> extends Property<S, V> {

    private final Property<S, V> delegate;

    public EventDispatchThreadProperty(final Property<S, V> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Class<? extends V> getWriteType(final S source) {
        return delegate.getWriteType(source);
    }

    @Override
    public V getValue(final S source) {
        return delegate.getValue(source);
    }

    @Override
    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public void setValue(final S source, final V value) {
        delegate.setValue(source, value);
    }

    @Override
    public boolean isReadable(final S source) {
        return delegate.isReadable(source);
    }

    @Override
    public boolean isWriteable(final S source) {
        return delegate.isWriteable(source);
    }

    @Override
    public void addPropertyStateListener(final S source, final PropertyStateListener listener) {
        delegate.addPropertyStateListener(source, listener);
    }

    @Override
    public void removePropertyStateListener(final S source, final PropertyStateListener listener) {
        delegate.removePropertyStateListener(source, listener);
    }

    @Override
    public PropertyStateListener[] getPropertyStateListeners(final S source) {
        return delegate.getPropertyStateListeners(source);
    }

}
