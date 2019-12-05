package de.invesdwin.context.client.swing.jfreechart.panel.helper;

import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.OHLCDataItem;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.listener.IRangeListener;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.IChartPanelAwareDatasetList;
import de.invesdwin.util.collections.factory.ILockCollectionFactory;
import de.invesdwin.util.collections.fast.IFastIterableSet;
import de.invesdwin.util.math.Doubles;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.util.time.fdate.FTimeUnit;

@NotThreadSafe
public class PlotZoomHelper {

    public static final int MAX_ZOOM_ITEM_COUNT = 10_000;
    private static final int MIN_ZOOM_ITEM_COUNT = 10;

    private static final double ZOOM_FACTOR = 0.1D;
    private static final double ZOOM_OUT_FACTOR = 1D + ZOOM_FACTOR;
    private static final double ZOOM_IN_FACTOR = 1 / ZOOM_OUT_FACTOR;
    private static final Duration ZOOMABLE_THRESHOLD = new Duration(10, FTimeUnit.MILLISECONDS);
    private static final double EDGE_ANCHOR_TOLERANCE = 0.1D;
    private Instant lastZoomable = new Instant();

    private final InteractiveChartPanel chartPanel;

    private final IFastIterableSet<IRangeListener> rangeListeners = ILockCollectionFactory.getInstance(false)
            .newFastIterableLinkedSet();

    public PlotZoomHelper(final InteractiveChartPanel chartPanel) {
        this.chartPanel = chartPanel;
    }

    public void mouseWheelMoved(final MouseWheelEvent e) {
        final double zoomFactor;
        final int clicks = e.getWheelRotation();
        if (clicks < 0) {
            zoomFactor = ZOOM_IN_FACTOR;
        } else {
            zoomFactor = ZOOM_OUT_FACTOR;
        }
        final Point2D point = this.chartPanel.getChartPanel().translateScreenToJava2D(e.getPoint());
        handleZoomable(point, zoomFactor);
    }

    private void handleZoomable(final Point2D point, final double zoomFactor) {
        synchronized (this) {
            if (lastZoomable.isLessThan(ZOOMABLE_THRESHOLD)) {
                return;
            }
            if (chartPanel.isUpdating()) {
                return;
            }
            lastZoomable = new Instant();
        }
        final XYPlot plot = (XYPlot) this.chartPanel.getChart().getPlot();

        // don't zoom unless the mouse pointer is in the plot's data area
        final ChartRenderingInfo info = this.chartPanel.getChartPanel().getChartRenderingInfo();
        final PlotRenderingInfo pinfo = info.getPlotInfo();
        if (!pinfo.getDataArea().contains(point)) {
            return;
        }

        final Range rangeBefore = chartPanel.getDomainAxis().getRange();
        final int lengthBefore = (int) rangeBefore.getLength();
        if (lengthBefore >= MAX_ZOOM_ITEM_COUNT && zoomFactor == ZOOM_OUT_FACTOR
                || lengthBefore <= MIN_ZOOM_ITEM_COUNT && zoomFactor == ZOOM_IN_FACTOR) {
            return;
        }

        chartPanel.incrementUpdatingCount();
        try {
            // do not notify while zooming each axis
            final boolean notifyState = plot.isNotify();
            plot.setNotify(false);
            plot.zoomDomainAxes(zoomFactor, pinfo, point, true);

            final double anchor = plot.getDomainAxis()
                    .java2DToValue(point.getX(), pinfo.getDataArea(), plot.getDomainAxisEdge());
            applyEdgeAnchor(rangeBefore, lengthBefore, anchor);
            plot.setNotify(notifyState); // this generates the change event too
            chartPanel.update();
        } finally {
            chartPanel.decrementUpdatingCount();
        }
    }

    private void applyEdgeAnchor(final Range rangeBefore, final int lengthBefore, final double anchor) {
        final Range rangeAfter = chartPanel.getDomainAxis().getRange();
        final double lengthAfter = rangeAfter.getLength();
        final List<? extends OHLCDataItem> data = chartPanel.getMasterDataset().getData();
        final double minLowerBound = getMinLowerBound(data);
        final double maxUpperBound = getMaxUpperBound(data);
        final double anchorUpperEdgeTolerance = rangeAfter.getUpperBound() - (lengthBefore * (EDGE_ANCHOR_TOLERANCE));
        final int gapAfter = chartPanel.getAllowedRangeGap(lengthAfter);
        if (anchor >= anchorUpperEdgeTolerance) {
            if (rangeBefore.getUpperBound() >= maxUpperBound) {
                //limit on max upper bound
                final double maxUpperBoundWithGap = maxUpperBound + gapAfter;
                chartPanel.getDomainAxis()
                        .setRange(new Range(maxUpperBoundWithGap - lengthAfter, maxUpperBoundWithGap));
            } else {
                //limit on max upper bound
                final double upperBoundWithoutGap = rangeBefore.getUpperBound();
                chartPanel.getDomainAxis()
                        .setRange(new Range(upperBoundWithoutGap - lengthAfter, upperBoundWithoutGap));
            }

        } else {
            final double anchorLowerEdgeTolerance = rangeAfter.getLowerBound() + (lengthBefore * EDGE_ANCHOR_TOLERANCE);
            if (anchor <= anchorLowerEdgeTolerance) {
                if (rangeBefore.getLowerBound() <= minLowerBound) {
                    //limit on min lower bound
                    final double minLowerBoundWithGap = minLowerBound - gapAfter;
                    chartPanel.getDomainAxis()
                            .setRange(new Range(minLowerBoundWithGap, minLowerBoundWithGap + lengthAfter));
                } else {
                    //limit on lower bound
                    final double minLowerBoundWithoutGap = rangeBefore.getLowerBound();
                    chartPanel.getDomainAxis()
                            .setRange(new Range(minLowerBoundWithoutGap, minLowerBoundWithoutGap + lengthAfter));
                }
            }
        }
    }

    public void zoomOut() {
        final ChartRenderingInfo info = this.chartPanel.getChartPanel().getChartRenderingInfo();
        final PlotRenderingInfo pinfo = info.getPlotInfo();
        handleZoomable(new Point2D.Double(pinfo.getDataArea().getMaxX() - 1, pinfo.getDataArea().getCenterY()),
                ZOOM_OUT_FACTOR);
    }

    public void zoomIn() {
        final ChartRenderingInfo info = this.chartPanel.getChartPanel().getChartRenderingInfo();
        final PlotRenderingInfo pinfo = info.getPlotInfo();
        handleZoomable(new Point2D.Double(pinfo.getDataArea().getMaxX() - 1, pinfo.getDataArea().getCenterY()),
                ZOOM_IN_FACTOR);
    }

    public Set<IRangeListener> getRangeListeners() {
        return rangeListeners;
    }

    public boolean limitRange() {
        final NumberAxis domainAxis = chartPanel.getDomainAxis();
        Range range = domainAxis.getRange();
        final MutableBoolean rangeChanged = new MutableBoolean(false);
        if (!rangeListeners.isEmpty()) {
            final IRangeListener[] array = rangeListeners.asArray(IRangeListener.class);
            for (int i = 0; i < array.length; i++) {
                range = array[i].beforeLimitRange(range, rangeChanged);
            }
        }
        final int length = (int) range.getLength();
        final int gap = chartPanel.getAllowedRangeGap(length);
        final List<? extends OHLCDataItem> data = chartPanel.getMasterDataset().getData();
        final double minLowerBound = getMinLowerBoundWithGap(data, gap);
        final double maxUpperBound = getMaxUpperBoundWithGap(data, gap);
        if (range.getLowerBound() < minLowerBound) {
            final double difference = minLowerBound - range.getLowerBound();
            range = new Range(minLowerBound, Doubles.min(range.getUpperBound() + difference, maxUpperBound));
            rangeChanged.setTrue();
        }
        if (range.getUpperBound() > maxUpperBound) {
            final double difference = range.getUpperBound() - maxUpperBound;
            range = new Range(Doubles.max(minLowerBound, range.getLowerBound() - difference), maxUpperBound);
            rangeChanged.setTrue();
        }
        range = limitRangeZoom(range, rangeChanged, minLowerBound, maxUpperBound);
        if (!rangeListeners.isEmpty()) {
            final IRangeListener[] array = rangeListeners.asArray(IRangeListener.class);
            for (int i = 0; i < array.length; i++) {
                range = array[i].afterLimitRange(range, rangeChanged);
            }
        }
        if (rangeChanged.booleanValue()) {
            domainAxis.setRange(range, true, false);
            return true;
        } else {
            return false;
        }
    }

    private double getMaxUpperBoundWithGap(final List<? extends OHLCDataItem> data, final int gap) {
        final double maxUpperBound = getMaxUpperBound(data);
        if (maxUpperBound >= data.size() - 1) {
            return maxUpperBound + gap;
        } else {
            return maxUpperBound;
        }
    }

    private double getMinLowerBoundWithGap(final List<? extends OHLCDataItem> data, final int gap) {
        final double minLowerBound = getMinLowerBound(data);
        if (minLowerBound <= 0) {
            return -gap;
        } else {
            return minLowerBound;
        }
    }

    private int getMaxUpperBound(final List<?> data) {
        if (data instanceof IChartPanelAwareDatasetList) {
            final IChartPanelAwareDatasetList cData = (IChartPanelAwareDatasetList) data;
            return cData.getMaxUpperBound();
        } else {
            return data.size() - 1;
        }
    }

    private int getMinLowerBound(final List<?> data) {
        if (data instanceof IChartPanelAwareDatasetList) {
            final IChartPanelAwareDatasetList cData = (IChartPanelAwareDatasetList) data;
            return cData.getMinLowerBound();
        } else {
            return 0;
        }
    }

    private Range limitRangeZoom(final Range newRange, final MutableBoolean rangeChanged, final double minLowerBound,
            final double maxUpperBound) {
        Range range = newRange;
        final int itemCount = (int) range.getLength();
        if (itemCount <= MIN_ZOOM_ITEM_COUNT) {
            final int gap = MIN_ZOOM_ITEM_COUNT / 2;
            range = new Range(range.getCentralValue() - gap, range.getCentralValue() + gap);
            if (range.getUpperBound() > maxUpperBound) {
                range = new Range(maxUpperBound - MIN_ZOOM_ITEM_COUNT, maxUpperBound);
            }
            if (range.getLowerBound() < minLowerBound) {
                range = new Range(minLowerBound, MIN_ZOOM_ITEM_COUNT);
            }
            rangeChanged.setTrue();
        }
        if (itemCount >= MAX_ZOOM_ITEM_COUNT) {
            if (range.getUpperBound() >= maxUpperBound) {
                //limit on upper bound
                range = new Range(maxUpperBound - MAX_ZOOM_ITEM_COUNT, maxUpperBound);
            } else if (range.getLowerBound() <= minLowerBound) {
                //limit on lower bound
                range = new Range(minLowerBound, minLowerBound + MAX_ZOOM_ITEM_COUNT);
            } else {
                //limit in the middle
                final int gap = MAX_ZOOM_ITEM_COUNT / 2;
                range = new Range(range.getCentralValue() - gap, range.getCentralValue() + gap);
                if (range.getUpperBound() > maxUpperBound) {
                    range = new Range(maxUpperBound - MAX_ZOOM_ITEM_COUNT, maxUpperBound);
                }
                if (range.getLowerBound() < minLowerBound) {
                    range = new Range(minLowerBound, MAX_ZOOM_ITEM_COUNT);
                }
            }
            rangeChanged.setTrue();
        }
        return range;
    }

}
