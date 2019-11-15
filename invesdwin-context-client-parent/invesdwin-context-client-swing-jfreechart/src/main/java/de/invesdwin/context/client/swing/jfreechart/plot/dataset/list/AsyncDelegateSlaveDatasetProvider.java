package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;

import javax.annotation.concurrent.ThreadSafe;

import org.jfree.data.xy.OHLCDataItem;

import de.invesdwin.context.jfreechart.dataset.XYDataItemOHLC;
import de.invesdwin.util.concurrent.priority.IPriorityRunnable;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.time.fdate.FDate;

@ThreadSafe
public class AsyncDelegateSlaveDatasetProvider implements ISlaveLazyDatasetProvider {

    private final class AsyncRunnable implements IPriorityRunnable {
        private final FDate key;
        //use weak reference to not load already evicted items due to fast scrolling
        private final WeakReference<XYDataItemOHLC> itemRef;

        private AsyncRunnable(final FDate key, final XYDataItemOHLC item) {
            this.key = key;
            this.itemRef = new WeakReference<XYDataItemOHLC>(item);
        }

        @Override
        public void run() {
            final XYDataItemOHLC item = itemRef.get();
            if (item == null || item.getOHLC() == null) {
                return;
            }
            loadData(item);
        }

        private void loadData(final XYDataItemOHLC item) {
            final XYDataItemOHLC value = delegate.getValue(key);
            if (value != null) {
                item.setOHLC(value.getOHLC());
            }
        }

        @Override
        public double getPriority() {
            //update ascending (because that is faster with historical caches)
            return key.millisValue();
        }
    }

    private final ExecutorService executor;
    private final ISlaveLazyDatasetProvider delegate;

    public AsyncDelegateSlaveDatasetProvider(final ExecutorService executor, final ISlaveLazyDatasetProvider delegate) {
        this.executor = executor;
        this.delegate = delegate;
    }

    @Override
    public XYDataItemOHLC getValue(final FDate key) {
        final XYDataItemOHLC item = new XYDataItemOHLC(
                new OHLCDataItem(key.dateValue(), Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN));
        executor.execute(new AsyncRunnable(key, item));
        return item;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).with(delegate).toString();
    }

}
