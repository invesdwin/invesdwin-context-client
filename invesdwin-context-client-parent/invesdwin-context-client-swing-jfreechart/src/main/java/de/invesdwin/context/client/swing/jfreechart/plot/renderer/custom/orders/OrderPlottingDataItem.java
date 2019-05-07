package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.orders;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.util.time.fdate.FDate;

@NotThreadSafe
public class OrderPlottingDataItem {

    private final double openPrice;
    private final FDate openTime;
    private final FDate closeTime;
    private final double closePrice;
    private final boolean closed;
    private final boolean profit;
    private final boolean pending;
    private final String orderId;
    private final String label;
    private final String note;
    private boolean visible;
    private int openTimeIndex = Integer.MIN_VALUE;
    private int closeTimeIndex = Integer.MIN_VALUE;

    //CHECKSTYLE:OFF
    public OrderPlottingDataItem(final double openPrice, final FDate openTime, final FDate closeTime,
            final double closePrice, final boolean closed, final boolean profit, final boolean pending,
            final String orderId, final String label, final String note) {
        //CHECKSTYLE:ON
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

    public String getLabel() {
        return label;
    }

    public String getNote() {
        return note;
    }

    public boolean isVisible() {
        return visible;
    }

    public void updateVisibility(final long firstLoadedKeyMillis, final long lastLoadedKeyMillis,
            final OrderPlottingDataset dataset) {
        if (getOpenTime().millisValue() > lastLoadedKeyMillis
                || getCloseTime() != null && getCloseTime().millisValue() < firstLoadedKeyMillis) {
            if (visible) {
                visible = false;
                openTimeIndex = Integer.MIN_VALUE;
                closeTimeIndex = Integer.MIN_VALUE;
            }
        } else {
            this.openTimeIndex = dataset.getDateTimeAsItemIndex(0, openTime);
            if (closeTime != null) {
                this.closeTimeIndex = dataset.getDateTimeAsItemIndex(0, closeTime);
            } else {
                this.closeTimeIndex = dataset.getItemCount(0) - 1;
            }
            visible = true;
        }
    }

    public int getVisibleOpenTimeIndex() {
        assertVisible();
        return openTimeIndex;
    }

    private void assertVisible() {
        if (!visible) {
            throw new IllegalStateException("not visible");
        }
    }

    public int getVisibleCloseTimeIndex() {
        assertVisible();
        return closeTimeIndex;
    }

}
