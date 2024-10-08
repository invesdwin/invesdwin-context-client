package de.invesdwin.context.client.swing.jfreechart.panel.helper;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.Timer;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.Range;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.basis.CustomCombinedDomainXYPlot;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.listener.IRangeListener;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.XYPriceLineAnnotation;
import de.invesdwin.context.client.swing.jfreechart.plot.axis.AxisDragInfo;
import de.invesdwin.context.client.swing.jfreechart.plot.axis.Axises;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IndexedDateTimeOHLCDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.IChartPanelAwareDatasetList;
import de.invesdwin.context.jfreechart.axis.AxisType;
import de.invesdwin.context.jfreechart.dataset.TimeRangedOHLCDataItem;
import de.invesdwin.util.collections.factory.ILockCollectionFactory;
import de.invesdwin.util.collections.fast.IFastIterableSet;
import de.invesdwin.util.lang.string.Strings;
import de.invesdwin.util.math.Doubles;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.util.time.date.FTimeUnit;
import de.invesdwin.util.time.duration.Duration;

@NotThreadSafe
public class PlotZoomHelper {

    public static final Cursor VERTICAL_RESIZE_CURSOR = new Cursor(Cursor.N_RESIZE_CURSOR);
    public static final Cursor HORIZONTAL_RESIZE_CURSOR = new Cursor(Cursor.W_RESIZE_CURSOR);

    public static final int MAX_ZOOM_ITEM_COUNT = 100_000;
    public static final int MIN_ZOOM_ITEM_COUNT = 10;
    private static final Duration ZOOM_ANNOTATION_TIMEOUT = new Duration(500, FTimeUnit.MILLISECONDS);
    private static final Font ZOOM_ANNOTATION_FONT = XYPriceLineAnnotation.FONT;

    private static final double ZOOM_FACTOR = 0.1D;
    private static final double ZOOM_OUT_FACTOR = 1D + ZOOM_FACTOR;
    private static final double ZOOM_IN_FACTOR = 1D / ZOOM_OUT_FACTOR;
    private static final Duration ZOOMABLE_THRESHOLD = new Duration(10, FTimeUnit.MILLISECONDS);
    private static final double EDGE_ANCHOR_TOLERANCE = 0.1D;

    private Instant lastZoomable = new Instant();
    private boolean lastZoomOnRangeAxis = false;

    private final InteractiveChartPanel chartPanel;
    private final XYAnnotation zoomAnnotation;
    private final TextTitle zoomTitle;
    private final Timer zoomTitleTimer;

    private AxisDragInfo axisDragInfo;
    private Cursor prevCursor;

    private final IFastIterableSet<IRangeListener> rangeListeners = ILockCollectionFactory.getInstance(true)
            .newFastIterableLinkedSet();

    public PlotZoomHelper(final InteractiveChartPanel chartPanel) {
        this.chartPanel = chartPanel;
        this.zoomTitle = new TextTitle("", ZOOM_ANNOTATION_FONT);
        this.zoomAnnotation = new XYTitleAnnotation(0.99, 0.9875, zoomTitle, RectangleAnchor.TOP_RIGHT) {

            @Override
            public void draw(final Graphics2D g2, final XYPlot plot, final Rectangle2D dataArea,
                    final ValueAxis domainAxis, final ValueAxis rangeAxis, final int rendererIndex,
                    final PlotRenderingInfo info) {
                if (lastZoomable.isGreaterThan(ZOOM_ANNOTATION_TIMEOUT) || lastZoomOnRangeAxis) {
                    return;
                }
                final Range domainAxisRange = plot.getDomainAxis().getRange();
                if (isMaxZoomOut(domainAxisRange)) {
                    zoomTitle.setText("Zoom: MAX");
                } else if (domainAxisRange.getLength() <= MIN_ZOOM_ITEM_COUNT) {
                    zoomTitle.setText("Zoom: MIN");
                } else {
                    zoomTitle.setText("");
                    return;
                }

                super.draw(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info);
            }
        };
        zoomTitleTimer = new Timer(ZOOM_ANNOTATION_TIMEOUT.intValue(FTimeUnit.MILLISECONDS) * 2, new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (Strings.isNotBlank(zoomTitle.getText())) {
                    chartPanel.repaint();
                }
            }
        });
        zoomTitleTimer.setRepeats(false);
    }

    public void init() {
        final XYPlot plot = this.chartPanel.getMasterDataset().getPlot();
        if (plot != null) {
            plot.addAnnotation(zoomAnnotation);
        }
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
            if (chartPanel.isLoading()) {
                return;
            }
            if (chartPanel.getMasterDataset().getData().isEmpty()) {
                return;
            }
            lastZoomable = new Instant();
            zoomTitleTimer.restart();
        }
        final CustomCombinedDomainXYPlot plot = chartPanel.getCombinedPlot();

        // don't zoom unless the mouse pointer is in the plot's data area
        final PlotRenderingInfo plotInfo = this.chartPanel.getChartPanel().getChartRenderingInfo().getPlotInfo();
        if (plotInfo.getDataArea().contains(point)) {
            handleZoomableDataArea(point, zoomFactor, plot);
        } else if (!plotInfo.getDataArea().contains(point) && plotInfo.getPlotArea().contains(point)) {
            final int subplotIndex = Axises.getSubplotIndexFromPlotArea(chartPanel, point);
            if (subplotIndex == -1) {
                //The zoom was on the domain-axis
                handleZoomableDataArea(point, zoomFactor, plot);
            } else {
                //The zoom was on a range-axis
                handleZoomableRangeAxisArea(point, zoomFactor, plot, subplotIndex);
            }
        }
    }

    private void handleZoomableDataArea(final Point2D point, final double zoomFactor,
            final CustomCombinedDomainXYPlot plot) {
        lastZoomOnRangeAxis = false;
        final Range rangeBefore = chartPanel.getDomainAxis().getRange();
        final int lengthBefore = (int) rangeBefore.getLength();
        if (lengthBefore >= MAX_ZOOM_ITEM_COUNT && zoomFactor == ZOOM_OUT_FACTOR
                || lengthBefore <= MIN_ZOOM_ITEM_COUNT && zoomFactor == ZOOM_IN_FACTOR) {
            chartPanel.repaint();
            return;
        }

        chartPanel.incrementUpdatingCount();
        try {
            // do not notify while zooming each axis
            final boolean notifyState = plot.isNotify();
            plot.setNotify(false);
            final PlotRenderingInfo plotInfo = this.chartPanel.getChartPanel().getChartRenderingInfo().getPlotInfo();

            final Range currentRange = chartPanel.getDomainAxis().getRange();
            final int maxUpperBound = getMaxUpperBound();
            final int minLowerBound = getMinLowerBound(chartPanel.getMasterDataset().getData());
            final boolean isGapLeft = chartPanel.getDomainAxis().getRange().getLowerBound() < minLowerBound;
            final boolean isGapRight = chartPanel.getUserGapRateRight() > 0;

            if ((isGapLeft || isGapRight)) {
                final double length = plot.getDomainAxis().getRange().getLength();
                final double newLength = length * zoomFactor;
                final double gapRateLeft = (minLowerBound - chartPanel.getDomainAxis().getRange().getLowerBound())
                        / length;
                final double gapRateRight = chartPanel.getUserGapRateRight();
                if (zoomFactor > 1) {
                    //ZoomOut when we have a UserGap on either side
                    /*
                     * If we have a UserGap on one side and ZoomOut. We only zoom to the other side till we reached the
                     * the same gapRate in that direction. If we have a UserGap on both sides we will keep the UserGap
                     * on the side with the bigger Rate constant and only ZoomOut as far till the USerGapRate on the
                     * other side is equal.
                     */
                    if (!isMaxZoomOut(currentRange)) {
                        /*
                         * No need to round the gapRate's here because the isMaxZoomOut-check already does it and would
                         * prevent's that we get here if gapRateLeft == gapRateRight (rounded)
                         */
                        if (gapRateLeft < gapRateRight) {
                            zoomKeepUserGapRight(currentRange, minLowerBound, maxUpperBound, newLength);
                        } else if (gapRateLeft > gapRateRight) {
                            zoomKeepUserGapLeft(currentRange, minLowerBound, maxUpperBound, newLength, gapRateLeft);
                        }
                    }
                } else {
                    //ZoomIn when we have a UserGap on either side
                    /*
                     * We ZoomIn while keeping the UserGap on whichever side has the bigger userGap. If gapRateLeft ==
                     * gapRateRight we let the right-side win.
                     */
                    if (Doubles.round(gapRateLeft, 3) <= Doubles.round(gapRateRight, 3)) {
                        zoomKeepUserGapRight(currentRange, minLowerBound, maxUpperBound, newLength);
                    } else {
                        zoomKeepUserGapLeft(currentRange, minLowerBound, maxUpperBound, newLength, gapRateLeft);
                    }
                }
            } else {
                //Regular zoom depending on the mouse position
                plot.zoomDomainAxes(zoomFactor, plotInfo, point, true);
                applyEdgeAnchor(rangeBefore, lengthBefore, point.getX(), plotInfo.getDataArea().getWidth());
                chartPanel.updateUserGapRateRight(maxUpperBound);
            }

            plot.setNotify(notifyState); // this generates the change event too
            chartPanel.update();
        } finally {
            chartPanel.decrementUpdatingCount();
        }
    }

    /**
     * Zoom (in or out) while keeping the userGapLeft (which we don't keep track of as a separate variable, like the
     * userGapRight).
     */
    protected void zoomKeepUserGapLeft(final Range oldRange, final int minLowerBound, final int maxUpperBound,
            final double newLength, final double gapRateLeft) {
        /*
         * We have a gap in the past (to the left, which we don't track separately) and want to zoom. We want to keep
         * the gap and only zoom on the right.. until we are live.
         */

        final double gap = gapRateLeft * newLength;
        final double newLowerBound = minLowerBound - gap;
        final double newUpperBound = newLowerBound + newLength;
        Range newRange = new Range(newLowerBound, newUpperBound);

        final Range fullDatasetVisibleLimitRange = getFullDatasetVisibleLimitRange(oldRange, minLowerBound,
                maxUpperBound);
        if (fullDatasetVisibleLimitRange != null) {
            final double newGapRateRight = (newRange.getUpperBound() - maxUpperBound) / newRange.getLength();
            if (newGapRateRight > gapRateLeft) {
                newRange = fullDatasetVisibleLimitRange;
            }
        }

        final Range limitRange = getLimitRange(newRange);
        if (limitRange != null) {
            newRange = limitRange;
        }

        chartPanel.getDomainAxis().setRange(newRange);

        //Update the userGapRight in case we scrolled so far out that we reached live-data.
        chartPanel.updateUserGapRateRight(chartPanel.getPlotZoomHelper().getMaxUpperBound());
    }

    /**
     * Zoom (in or out) while keeping the userGapRight.
     */
    protected void zoomKeepUserGapRight(final Range oldRange, final int minLowerBound, final int maxUpperBound,
            final double newLength) {
        final double gap = chartPanel.getUserGapRateRight() * newLength;
        final double newUpperBound = maxUpperBound + gap;
        final double newLowerBound = newUpperBound - newLength;
        Range newRange = new Range(newLowerBound, newUpperBound);

        final Range fullDatasetVisibleLimitRange = getFullDatasetVisibleLimitRange(oldRange, minLowerBound,
                maxUpperBound);
        if (fullDatasetVisibleLimitRange != null) {
            final double newGapRateLeft = (minLowerBound - newLowerBound) / newRange.getLength();
            if (newGapRateLeft > chartPanel.getUserGapRateRight()) {
                newRange = fullDatasetVisibleLimitRange;
            }
        }

        final Range limitRange = getLimitRange(newRange);
        if (limitRange != null) {
            newRange = limitRange;
        }

        chartPanel.getDomainAxis().setRange(newRange);
    }

    private void handleZoomableRangeAxisArea(final Point2D point, final double zoomFactor,
            final CustomCombinedDomainXYPlot plot, final int subplotIndex) {
        lastZoomOnRangeAxis = true;
        final XYPlot xyPlot = chartPanel.getCombinedPlot().getSubplots().get(subplotIndex);
        final ValueAxis rangeAxis = Axises.getRangeAxis(chartPanel, point, xyPlot);
        if (rangeAxis != null) {
            rangeAxis.setAutoRange(false);

            final Range range = rangeAxis.getRange();
            final double halfLength = range.getLength() * zoomFactor / 2;

            final Range autoRange = Axises.calculateAutoRange(rangeAxis);
            final double autoCentralValue = autoRange.getCentralValue();
            final double centralValue = range.getCentralValue();
            final double centralValueOffset = centralValue - autoCentralValue;
            final double adjustedCentralValueOffset = centralValueOffset * zoomFactor;

            final Range adjusted = new Range(autoCentralValue - halfLength + adjustedCentralValueOffset,
                    autoCentralValue + halfLength + adjustedCentralValueOffset);

            rangeAxis.setRange(adjusted);
            xyPlot.setRangePannable(true);
        }
    }

    private void applyEdgeAnchor(final Range rangeBefore, final int lengthBefore, final double anchor,
            final double width) {
        final Range rangeAfter = chartPanel.getDomainAxis().getRange();
        final double lengthAfter = rangeAfter.getLength();
        final List<? extends TimeRangedOHLCDataItem> data = chartPanel.getMasterDataset().getData();

        final int gapAfter = chartPanel.getAllowedMaximumRangeGap(lengthAfter);
        final double minLowerBound = getMinLowerBoundWithGap(data, gapAfter);
        final double maxUpperBound = getMaxUpperBoundWithGap(data, gapAfter);

        final double anchorUpperEdgeTolerance = width - (width * EDGE_ANCHOR_TOLERANCE);
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
            final double anchorLowerEdgeTolerance = width * EDGE_ANCHOR_TOLERANCE;
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
        Point2D point = chartPanel.getPlotCrosshairHelper().getCrosshairLastMousePoint();
        if (point == null) {
            point = new Point2D.Double(pinfo.getDataArea().getMaxX() - 1, pinfo.getDataArea().getCenterY());
        }
        handleZoomable(point, ZOOM_OUT_FACTOR);
    }

    public void zoomIn() {
        final ChartRenderingInfo info = this.chartPanel.getChartPanel().getChartRenderingInfo();
        final PlotRenderingInfo pinfo = info.getPlotInfo();
        Point2D point = chartPanel.getPlotCrosshairHelper().getCrosshairLastMousePoint();
        if (point == null) {
            point = new Point2D.Double(pinfo.getDataArea().getMaxX() - 1, pinfo.getDataArea().getCenterY());
        }
        handleZoomable(point, ZOOM_IN_FACTOR);
    }

    public Set<IRangeListener> getRangeListeners() {
        return rangeListeners;
    }

    public boolean limitRange() {
        final NumberAxis domainAxis = chartPanel.getDomainAxis();
        final Range limitRange = getLimitRange(domainAxis.getRange());

        if (limitRange != null) {
            domainAxis.setRange(limitRange, true, false);
            return true;
        }
        return false;
    }

    public Range getLimitRange(final Range domainAxisRange) {
        Range range = domainAxisRange;
        final MutableBoolean rangeChanged = new MutableBoolean(false);
        if (!rangeListeners.isEmpty()) {
            final IRangeListener[] array = rangeListeners.asArray(IRangeListener.EMPTY_ARRAY);
            for (int i = 0; i < array.length; i++) {
                range = array[i].beforeLimitRange(range, rangeChanged);
            }
        }
        final double length = range.getLength();
        final int gap = chartPanel.getAllowedMaximumRangeGap(length);
        final List<? extends TimeRangedOHLCDataItem> data = chartPanel.getMasterDataset().getData();
        final double minLowerBound = getMinLowerBoundWithGap(data, gap);
        final double maxUpperBound = getMaxUpperBoundWithGap(data, gap);
        if (range.getLowerBound() < minLowerBound) {
            final double difference = minLowerBound - range.getLowerBound();
            final double max = Doubles.min(range.getUpperBound() + difference, maxUpperBound);
            if (minLowerBound < max) {
                range = new Range(minLowerBound, max);
                rangeChanged.setTrue();
            }
        }
        if (range.getUpperBound() > maxUpperBound) {
            final double difference = range.getUpperBound() - maxUpperBound;
            final double min = Doubles.max(minLowerBound, range.getLowerBound() - difference);
            if (min < maxUpperBound) {
                range = new Range(min, maxUpperBound);
                rangeChanged.setTrue();
            }
        }
        range = limitRangeZoom(range, rangeChanged, minLowerBound, maxUpperBound);
        if (!rangeListeners.isEmpty()) {
            final IRangeListener[] array = rangeListeners.asArray(IRangeListener.EMPTY_ARRAY);
            for (int i = 0; i < array.length; i++) {
                range = array[i].afterLimitRange(range, rangeChanged);
            }
        }
        if (rangeChanged.booleanValue()) {
            return range;
        }
        return null;
    }

    public double getMaxUpperBoundWithGap(final List<? extends TimeRangedOHLCDataItem> data, final int gap) {
        final double maxUpperBound = getMaxUpperBound(data);
        if (maxUpperBound >= data.size() - 1) {
            return maxUpperBound + gap;
        } else {
            return maxUpperBound;
        }
    }

    public double getMinLowerBoundWithGap(final List<? extends TimeRangedOHLCDataItem> data, final int gap) {
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

    public int getMaxUpperBound() {
        return getMaxUpperBound(chartPanel.getMasterDataset().getData());
    }

    public int getMinLowerBound() {
        return getMinLowerBound(chartPanel.getMasterDataset().getData());
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
                range = new Range(minLowerBound, minLowerBound + MIN_ZOOM_ITEM_COUNT);
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
                    range = new Range(minLowerBound, minLowerBound + MAX_ZOOM_ITEM_COUNT);
                }
            }
            rangeChanged.setTrue();
        }
        return range;
    }

    public void mousePressed(final MouseEvent e) {
        final Point2D point2D = this.chartPanel.getChartPanel().translateScreenToJava2D(e.getPoint());
        final AxisType axis = Axises.getAxisForMousePosition(chartPanel, point2D);
        if (axis != null && MouseEvent.BUTTON1 == e.getButton()) {
            axisDragInfo = Axises.createAxisDragInfo(chartPanel, point2D, axis);
        }

        if (axis != null && AxisType.RANGE_AXIS.equals(axis)) {
            final ValueAxis rangeAxis = axisDragInfo != null ? axisDragInfo.getValueAxis()
                    : Axises.getRangeAxis(chartPanel, point2D);
            maybeHandleRangeAxisReset(e, rangeAxis);
        }
    }

    public void mouseReleased(final MouseEvent e) {
        axisDragInfo = null;
    }

    public void mouseDragged(final MouseEvent e) {
        if (axisDragInfo != null) {
            final Point2D point2D = this.chartPanel.getChartPanel().translateScreenToJava2D(e.getPoint());
            final Range newAxisRange = calculateNewAxisRange(point2D);
            if (newAxisRange != null) {
                axisDragInfo.getValueAxis().setRange(newAxisRange);
                if (AxisType.RANGE_AXIS.equals(axisDragInfo.getAxis())) {
                    axisDragInfo.getValueAxis().setAutoRange(false);
                    ((XYPlot) axisDragInfo.getValueAxis().getPlot()).setRangePannable(true);
                }
            }
        }
    }

    public boolean mouseMoved(final MouseEvent e) {
        final Point2D point2D = this.chartPanel.getChartPanel().translateScreenToJava2D(e.getPoint());

        final AxisType axis = Axises.getAxisForMousePosition(chartPanel, point2D);
        if (axis != null) {
            prevCursor = chartPanel.getCursor();
            chartPanel.setCursor(AxisType.DOMAIN_AXIS.equals(axis) ? HORIZONTAL_RESIZE_CURSOR : VERTICAL_RESIZE_CURSOR);
            return true;
        } else if (prevCursor != null) {
            chartPanel.setCursor(prevCursor);
            prevCursor = null;
        }
        return false;
    }

    private Range calculateNewAxisRange(final Point2D point2D) {
        final double initialDragPoint = AxisType.DOMAIN_AXIS.equals(axisDragInfo.getAxis())
                ? axisDragInfo.getInitialDragPoint().getX()
                : axisDragInfo.getInitialDragPoint().getY();
        final double newDragPoint = AxisType.DOMAIN_AXIS.equals(axisDragInfo.getAxis()) ? point2D.getX()
                : point2D.getY();
        final double axisRangeChange = initialDragPoint - newDragPoint;

        final ValueAxis valueAxis = axisDragInfo.getValueAxis();
        final XYPlot xyPlot = (XYPlot) valueAxis.getPlot();
        final IPlotSourceDataset dataset = (IPlotSourceDataset) xyPlot.getDataset();
        final IndexedDateTimeOHLCDataset masterDataset = dataset.getMasterDataset();
        final Range range;
        if (AxisType.DOMAIN_AXIS.equals(axisDragInfo.getAxis())) {
            // When we zoom on the domain axis it can happen that we reload/expand the dataset.. in which case the initialRange will point at invalid dates. When we zoom on the range-axis this won't ever be needed.
            final int lowerBoundIndex = masterDataset.getDateTimeStartAsItemIndex(0,
                    axisDragInfo.getInitialAxisLowerBoundFDate());
            final int upperBoundIndex = masterDataset.getDateTimeStartAsItemIndex(0,
                    axisDragInfo.getInitialAxisUpperBoundFDate());
            range = new Range(masterDataset.getXValue(0, lowerBoundIndex), masterDataset.getXValue(0, upperBoundIndex));
        } else {
            range = axisDragInfo.getInitialAxisRange();
        }

        if (axisRangeChange == 0.0D) {
            return null;
        }

        //Check new mouse location in reference to the initialDragStartMouse Position and zoom the axis accordingly
        final double zoomFactor;
        final double axisLength = AxisType.DOMAIN_AXIS.equals(axisDragInfo.getAxis()) ? axisDragInfo.getPlotWidth()
                : axisDragInfo.getPlotHeight();
        if (axisRangeChange > 0.0D) {
            zoomFactor = 1D / (1D + Doubles.divide(Math.abs(axisRangeChange), axisLength / 2));
        } else {
            zoomFactor = 1D + Doubles.divide(Math.abs(axisRangeChange), axisLength / 2);
        }

        Range newRange = null;

        if (AxisType.RANGE_AXIS.equals(axisDragInfo.getAxis())) {
            final double halfLength = range.getLength() * zoomFactor / 2;
            final Range autoRange = Axises.calculateAutoRange(valueAxis);
            final double autoCentralValue = autoRange.getCentralValue();
            final double centralValue = range.getCentralValue();
            final double centralValueOffset = centralValue - autoCentralValue;
            final double adjustedCentralValueOffset = centralValueOffset * zoomFactor;

            newRange = new Range(autoCentralValue - halfLength + adjustedCentralValueOffset,
                    autoCentralValue + halfLength + adjustedCentralValueOffset);
        } else {
            //We work with the FDate as anchor because in case more Data gets loaded into the Dataset the double value will point at a different date so that the Zoom would make an unwanted jump in its range.
            final FDate domainAnchorFDate = axisDragInfo.getDomainAnchorFDate();
            final int itemIndex = masterDataset.getDateTimeStartAsItemIndex(0, domainAnchorFDate);
            final double domainAnchor = masterDataset.getXValue(0, itemIndex);
            final double left = domainAnchor - range.getLowerBound();
            final double right = range.getUpperBound() - domainAnchor;
            newRange = new Range(domainAnchor - left * zoomFactor, domainAnchor + right * zoomFactor);

            final Range limitRange = getLimitRange(newRange);
            if (limitRange != null) {
                try {
                    return new Range(Math.max(newRange.getLowerBound(), limitRange.getLowerBound()),
                            Math.min(newRange.getUpperBound(), limitRange.getUpperBound()));
                } catch (final IllegalArgumentException e) {
                    //For some reason it can happen that lowerBound > upperBound here and the Exception is thrown.
                    //Probably happens when getLimitRange appended/prepended more data.
                    return newRange;
                }

            }
        }
        return newRange;
    }

    /**
     * Range axis resets on Double-Left-Click or Single-Middle-Mouse-Button-Click (Scrollwheel).
     */
    private void maybeHandleRangeAxisReset(final MouseEvent e, final ValueAxis rangeAxis) {
        //Double-Click on the axis
        if (rangeAxis != null && ((MouseEvent.BUTTON1 == e.getButton() && e.getClickCount() == 2)
                || MouseEvent.BUTTON2 == e.getButton())) {
            final XYPlot xyPlot = (XYPlot) rangeAxis.getPlot();
            if (e.isControlDown()) {
                //reset every axis in the plot
                for (int i = 0; i < xyPlot.getRangeAxisCount(); i++) {
                    final ValueAxis rangeAxisInLoop = xyPlot.getRangeAxis(i);
                    if (rangeAxisInLoop != null) {
                        rangeAxisInLoop.setAutoRange(true);
                    }
                }
            } else {
                //reset only the axis we clicked on
                rangeAxis.setAutoRange(true);
            }
            // We make the xyplot y-pannable if at least one axis/indicator is on AutoRange = false.
            xyPlot.setRangePannable(!Axises.isEveryAxisAutoRange(xyPlot));
        }
    }

    /**
     * MaxZoomOut-Conditions: <br>
     * - MAX_ZOOM_ITEM_COUNT is reached <br>
     * - The full dataset is visible and a userGap on either side is > 0.5 <br>
     * - The full dataset is visible and userGapLeft == userGapRight <br>
     */
    public boolean isMaxZoomOut(final Range domainAxisRange) {
        if (domainAxisRange.getLength() >= MAX_ZOOM_ITEM_COUNT) {
            return true;
        }

        if (isFullDataRangeVisible(domainAxisRange)) {
            final double userGapRateLeft = calcCurrenctUserGapRateLeft();
            final double userGapRateRight = chartPanel.getUserGapRateRight();
            //We round this because there can always be a slight difference in a very late decimal place
            if (userGapRateLeft > 0.5D || userGapRateRight > 0.5D
                    || Doubles.round(userGapRateLeft, 3) == Doubles.round(userGapRateRight, 3)) {
                return true;
            }
        }

        return false;
    }

    private boolean isFullDataRangeVisible(final Range domainAxisRange) {
        return domainAxisRange.getLowerBound() <= getMinLowerBound()
                && domainAxisRange.getUpperBound() >= getMaxUpperBound();
    }

    /**
     * Returns the limit-DomainRange for zooming-out. When zooming out while we have a userGap (on either side) and the
     * whole dataset is visible: We only want to be able to zoom to a point where the UserGap on both sides are
     * identical.
     */
    private Range getFullDatasetVisibleLimitRange(final Range currentDomainAxisRange, final int minLowerBound,
            final int maxUpperBound) {
        final double lowerBound = currentDomainAxisRange.getLowerBound();

        final double gapLeft = lowerBound < minLowerBound ? minLowerBound - lowerBound : 0.0;
        final double userGapRateRight = chartPanel.getUserGapRateRight();
        final double gapRight = currentDomainAxisRange.getLength() * userGapRateRight;

        if (gapLeft == 0.0 && gapRight == 0.0) {
            return null;
        }

        if (gapLeft == gapRight) {
            return currentDomainAxisRange;
        }

        final double limitLowerBound;
        final double limitUpperBound;
        final double limitDomainAxisLength;
        final double newGap;

        if (gapLeft > gapRight) {
            limitDomainAxisLength = chartPanel.getMasterDataset().getData().size() + (2 * gapLeft);
            final double userGapRateLeft = calcUserGapRateLeft(minLowerBound, limitDomainAxisLength);
            newGap = limitDomainAxisLength * userGapRateLeft;
        } else {
            limitDomainAxisLength = chartPanel.getMasterDataset().getData().size() + (2 * gapRight);
            newGap = limitDomainAxisLength * userGapRateRight;
        }

        limitLowerBound = minLowerBound - newGap;
        limitUpperBound = maxUpperBound + newGap;

        return new Range(limitLowerBound, limitUpperBound);
    }

    private double calcCurrenctUserGapRateLeft() {
        return (getMinLowerBound() - chartPanel.getDomainAxis().getRange().getLowerBound())
                / chartPanel.getDomainAxis().getRange().getLength();
    }

    private double calcUserGapRateLeft(final double minLowerBound, final double domainAxisLength) {
        return (minLowerBound - chartPanel.getDomainAxis().getRange().getLowerBound()) / domainAxisLength;
    }

    public boolean isMouseDragZooming() {
        return axisDragInfo != null;
    }
}
