package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public abstract class ALazyDatasetList<E> implements List<E> {

    @GuardedBy("no synchronization for performance reasons, get handles exceptions")
    private List<E> data;

    public ALazyDatasetList() {
        newData();
    }

    protected List<E> newData() {
        if (data != null && data.isEmpty()) {
            return data;
        }
        if (data != null) {
            data = new ArrayList<>(data.size());
        } else {
            data = new ArrayList<>();
        }
        return data;
    }

    protected List<E> getData() {
        return data;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        throw newNotNeededException();
    }

    @Deprecated
    @Override
    public Iterator<E> iterator() {
        throw newNotNeededException();
    }

    private UnsupportedOperationException newNotNeededException() {
        return new UnsupportedOperationException("not needed for lazy dataset");
    }

    @Deprecated
    @Override
    public Object[] toArray() {
        throw newNotNeededException();
    }

    @Deprecated
    @Override
    public <T> T[] toArray(final T[] a) {
        throw newNotNeededException();
    }

    @Deprecated
    @Override
    public boolean add(final E e) {
        throw newNotNeededException();
    }

    @Deprecated
    @Override
    public boolean remove(final Object o) {
        throw newNotNeededException();
    }

    @Deprecated
    @Override
    public boolean containsAll(final Collection<?> c) {
        throw newNotNeededException();
    }

    @Deprecated
    @Override
    public boolean addAll(final Collection<? extends E> c) {
        throw newNotNeededException();
    }

    @Deprecated
    @Override
    public boolean addAll(final int index, final Collection<? extends E> c) {
        throw newNotNeededException();
    }

    @Deprecated
    @Override
    public boolean removeAll(final Collection<?> c) {
        throw newNotNeededException();
    }

    @Deprecated
    @Override
    public boolean retainAll(final Collection<?> c) {
        throw newNotNeededException();
    }

    @Override
    public void clear() {
        throw newNotNeededException();
    }

    @Override
    public E get(final int index) {
        try {
            if (index < 0) {
                return data.get(0);
            }
            return data.get(index);
        } catch (final Throwable e) {
            final int size = data.size();
            if (size == 0) {
                return dummyValue();
            } else {
                try {
                    return data.get(size - 1);
                } catch (final Throwable e1) {
                    return dummyValue();
                }
            }
        }
    }

    protected abstract E dummyValue();

    @Deprecated
    @Override
    public E set(final int index, final E element) {
        throw newNotNeededException();
    }

    @Deprecated
    @Override
    public void add(final int index, final E element) {
        throw newNotNeededException();
    }

    @Deprecated
    @Override
    public E remove(final int index) {
        throw newNotNeededException();
    }

    @Deprecated
    @Override
    public int indexOf(final Object o) {
        throw newNotNeededException();
    }

    @Deprecated
    @Override
    public int lastIndexOf(final Object o) {
        throw newNotNeededException();
    }

    @Deprecated
    @Override
    public ListIterator<E> listIterator() {
        throw newNotNeededException();
    }

    @Deprecated
    @Override
    public ListIterator<E> listIterator(final int index) {
        throw newNotNeededException();
    }

    @Deprecated
    @Override
    public List<E> subList(final int fromIndex, final int toIndex) {
        throw newNotNeededException();
    }

}
