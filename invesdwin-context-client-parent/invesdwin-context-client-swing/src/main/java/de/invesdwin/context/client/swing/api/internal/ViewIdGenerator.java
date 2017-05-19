package de.invesdwin.context.client.swing.api.internal;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.util.collections.loadingcache.ALoadingCache;

@ThreadSafe
public final class ViewIdGenerator {

    @SuppressWarnings("rawtypes")
    private static final ALoadingCache<Class<? extends AView>, AtomicInteger> CLASS_SEQUENCENUMBER = new ALoadingCache<Class<? extends AView>, AtomicInteger>() {
        @Override
        protected AtomicInteger loadValue(final Class<? extends AView> key) {
            return new AtomicInteger();
        }
    };

    private ViewIdGenerator() {}

    /**
     * Generates IDs in the schema of [ClassFQN]_[SequenceNumber].
     */
    public static String newId(final AView<?, ?> view) {
        final AtomicInteger sequenceNumber = CLASS_SEQUENCENUMBER.get(view.getClass());
        return view.getClass().getName() + "_" + sequenceNumber.incrementAndGet();
    }

}
