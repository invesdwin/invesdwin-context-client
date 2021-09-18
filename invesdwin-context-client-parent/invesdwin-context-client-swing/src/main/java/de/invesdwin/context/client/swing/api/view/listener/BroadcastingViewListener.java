package de.invesdwin.context.client.swing.api.view.listener;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.util.collections.factory.ILockCollectionFactory;
import de.invesdwin.util.collections.fast.IFastIterable;
import de.invesdwin.util.collections.fast.IFastIterableCollection;

@ThreadSafe
public class BroadcastingViewListener implements IViewListener, Cloneable {

    private IFastIterableCollection<IViewListener> listeners = ILockCollectionFactory.getInstance(true)
            .newFastIterableLinkedSet();

    public boolean registerListener(final IViewListener l) {
        return listeners.add(l);
    }

    public boolean unregisterListener(final IViewListener l) {
        return listeners.remove(l);
    }

    @Override
    public void onOpen() {
        final IViewListener[] array = listeners.asArray(IViewListener.EMPTY_ARRAY);
        for (int i = 0; i < array.length; i++) {
            array[i].onOpen();
        }
    }

    @Override
    public void onClose() {
        final IViewListener[] array = listeners.asArray(IViewListener.EMPTY_ARRAY);
        for (int i = 0; i < array.length; i++) {
            array[i].onClose();
        }
    }

    @Override
    public void onShowing() {
        final IViewListener[] array = listeners.asArray(IViewListener.EMPTY_ARRAY);
        for (int i = 0; i < array.length; i++) {
            array[i].onShowing();
        }
    }

    public void clear() {
        listeners.clear();
    }

    public IFastIterable<IViewListener> getListeners() {
        return listeners;
    }

    @Override
    public BroadcastingViewListener clone() {
        try {
            final BroadcastingViewListener clone = (BroadcastingViewListener) super.clone();
            clone.listeners = ILockCollectionFactory.getInstance(true).newFastIterableLinkedSet();
            clone.listeners.addAll(listeners);
            return clone;
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isEmpty() {
        return listeners.isEmpty();
    }

}
