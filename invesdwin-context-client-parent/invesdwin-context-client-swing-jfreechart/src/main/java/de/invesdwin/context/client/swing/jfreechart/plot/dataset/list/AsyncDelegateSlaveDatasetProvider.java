package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import java.util.concurrent.Executor;

import javax.annotation.concurrent.ThreadSafe;

import org.jfree.data.xy.OHLCDataItem;

import de.invesdwin.context.jfreechart.dataset.XYDataItemOHLC;
import de.invesdwin.util.concurrent.priority.IPriorityRunnable;
import de.invesdwin.util.time.fdate.FDate;

@ThreadSafe
public class AsyncDelegateSlaveDatasetProvider implements ISlaveLazyDatasetProvider {

    private final Executor executor;
    private final ISlaveLazyDatasetProvider delegate;

    public AsyncDelegateSlaveDatasetProvider(final Executor executor, final ISlaveLazyDatasetProvider delegate) {
        this.executor = executor;
        this.delegate = delegate;
    }

    @Override
    public XYDataItemOHLC getValue(final FDate key) {
        final XYDataItemOHLC item = new XYDataItemOHLC(
                new OHLCDataItem(key.dateValue(), Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN));
        executor.execute(new IPriorityRunnable() {
            @Override
            public void run() {
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
        });
        return item;
    }

}
