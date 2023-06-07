package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.orders;

import java.util.function.Supplier;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.util.lang.comparator.ACriteriaComparator;
import de.invesdwin.util.time.date.FDate;

@NotThreadSafe
public class OrderPlottingDataItem {

    public static final ACriteriaComparator<OrderPlottingDataItem> NOTE_COMPARATOR = new ACriteriaComparator<OrderPlottingDataItem>() {
        @Override
        public Comparable<?> getCompareCriteriaNotNullSafe(final OrderPlottingDataItem e) {
            return e.getNote().get();
        }
    };

    private final double openPrice;
    private final FDate openTime;
    private final FDate closeTime;
    private final double closePrice;
    private final boolean closed;
    private final boolean profit;
    private final boolean pending;
    private final String orderId;
    private final Supplier<String> label;
    private final Supplier<String> note;
    private boolean itemLoaded;
    private int openTimeLoadedIndex = Integer.MIN_VALUE;
    private int closeTimeLoadedIndex = Integer.MIN_VALUE;

    public OrderPlottingDataItem(final double openPrice, final FDate openTime, final FDate closeTime,
            final double closePrice, final boolean closed, final boolean profit, final boolean pending,
            final String orderId, final Supplier<String> label, final Supplier<String> note) {
        this.openPrice = openPrice;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.closePrice = closePrice;
        this.closed = closed;
        this.profit = profit;
        this.pending = pending;
        this.orderId = orderId;
        this.label = label;
        this.note = note;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public FDate getOpenTime() {
        return openTime;
    }

    public FDate getCloseTime() {
        return closeTime;
    }

    public double getClosePrice() {
        return closePrice;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isProfit() {
        return profit;
    }

    public boolean isPending() {
        return pending;
    }

    public String getOrderId() {
        return orderId;
    }

    public Supplier<String> getLabel() {
        return label;
    }

    public Supplier<String> getNote() {
        return note;
    }

    public boolean isItemLoaded() {
        return itemLoaded;
    }

    public void updateItemLoaded(final long firstLoadedKeyMillis, final long lastLoadedKeyMillis,
            final boolean trailingLoaded, final OrderPlottingDataset dataset) {
        if (!trailingLoaded && getOpenTime().millisValue() > lastLoadedKeyMillis
                || getCloseTime() != null && getCloseTime().millisValue() < firstLoadedKeyMillis) {
            if (itemLoaded) {
                itemLoaded = false;
                openTimeLoadedIndex = Integer.MIN_VALUE;
                closeTimeLoadedIndex = Integer.MIN_VALUE;
            }
        } else {
            //we need to search for start time, otherwise entries will be plotted one bar too early
            this.openTimeLoadedIndex = dataset.getDateTimeStartAsItemIndex(0, openTime);
            if (closeTime != null) {
                final int endIndexFromEndTime = dataset.getDateTimeEndAsItemIndex(0, closeTime);
                //close happend either inside this bar or right at the end of it?
                this.closeTimeLoadedIndex = endIndexFromEndTime;
                final long endTimeMillis = (long) dataset.getXValueAsDateTimeEnd(0, endIndexFromEndTime);
                checkCloseInsideNextBar(dataset, endIndexFromEndTime, endTimeMillis);
            } else {
                final int lastIndex = dataset.getItemCount(0) - 1;
                this.closeTimeLoadedIndex = lastIndex;
            }
            itemLoaded = true;
        }
    }

    private void checkCloseInsideNextBar(final OrderPlottingDataset dataset, final int endIndexFromEndTime,
            final long endTimeMillis) {
        //end time is exclusive, start time is inclusive for time bars

        //close happened inside the next bar?
        if (closeTime.millisValue() < endTimeMillis) {
            return;
        }

        final int lastIndex = dataset.getItemCount(0) - 1;
        final int nextBarIndex = endIndexFromEndTime + 1;
        if (nextBarIndex > lastIndex) {
            return;
        }
        this.closeTimeLoadedIndex = nextBarIndex;
        if (closeTime.millisValue() > endTimeMillis) {
            this.closeTimeLoadedIndex = nextBarIndex;
        } else {
            final long nextStartTimeMillis = (long) dataset.getXValueAsDateTimeStart(0, nextBarIndex);
            if (nextStartTimeMillis == endTimeMillis || closeTime.millisValue() >= nextStartTimeMillis) {
                final double high = dataset.getHighValue(0, nextBarIndex);
                final double low = dataset.getLowValue(0, nextBarIndex);
                if (low <= closePrice && closePrice <= high) {
                    this.closeTimeLoadedIndex = nextBarIndex;
                }
            }
        }
    }

    public void modifyItemLoadedIndexes(final int fromIndex, final int addend) {
        if (itemLoaded) {
            if (openTimeLoadedIndex >= fromIndex) {
                openTimeLoadedIndex += addend;
            }
            if (closeTimeLoadedIndex >= fromIndex) {
                closeTimeLoadedIndex += addend;
            }
        }
    }

    public int getOpenTimeLoadedIndex() {
        assertItemLoaded();
        return openTimeLoadedIndex;
    }

    private void assertItemLoaded() {
        if (!itemLoaded) {
            throw new IllegalStateException("not loaded");
        }
    }

    public int getCloseTimeLoadedIndex() {
        assertItemLoaded();
        return closeTimeLoadedIndex;
    }

}
