package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.orders;

import java.util.function.Supplier;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.util.lang.comparator.ACriteriaComparator;
import de.invesdwin.util.math.Doubles;
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
    private final double conditionalOpenPrice;
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

    public OrderPlottingDataItem(final double openPrice, final double conditionalOpenPrice, final FDate openTime,
            final FDate closeTime, final double closePrice, final boolean closed, final boolean profit,
            final boolean pending, final String orderId, final Supplier<String> label, final Supplier<String> note) {
        this.openPrice = openPrice;
        this.conditionalOpenPrice = conditionalOpenPrice;
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

    /**
     * WARNING: use getExecutionPrice instead to get the actual price on which the order would be filled.
     * 
     * If conditionalOpenPrice is not NaN, this is the triggerPrice that should be drawn in e.g. purple.
     */
    @Deprecated
    public double getOpenPrice() {
        return openPrice;
    }

    /**
     * WARNING: use getTriggerPrice instead to know when to draw the trigger line.
     * 
     * If this conditionalOpenPrice is not NaN, we have a two-step order and this is the actual openPrice that becomes
     * active after the triggerPrice=getOpenPrice is reached
     */
    @Deprecated
    public double getConditionalOpenPrice() {
        return conditionalOpenPrice;
    }

    /**
     * This line is optional, if it exists it is drawn (e.g. purple)
     */
    public double getTriggerPrice() {
        if (!Doubles.isNaN(conditionalOpenPrice)) {
            return openPrice;
        } else {
            return Double.NaN;
        }
    }

    /**
     * This is the normal line we draw for the order, might only not exist for market orders
     */
    public double getExecutionPrice() {
        if (!Doubles.isNaN(conditionalOpenPrice)) {
            return conditionalOpenPrice;
        } else {
            return openPrice;
        }
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
