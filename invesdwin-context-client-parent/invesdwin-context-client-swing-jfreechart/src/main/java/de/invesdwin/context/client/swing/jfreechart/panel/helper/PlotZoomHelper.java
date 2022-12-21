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
import de.invesdwin.context.client.swing.jfreechart.plot.Axis;
import de.invesdwin.context.client.swing.jfreechart.plot.Axises;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.XYPriceLineAnnotation;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.IChartPanelAwareDatasetList;
import de.invesdwin.context.jfreechart.dataset.TimeRangedOHLCDataItem;
import de.invesdwin.util.collections.factory.ILockCollectionFactory;
import de.invesdwin.util.collections.fast.IFastIterableSet;
import de.invesdwin.util.lang.string.Strings;
import de.invesdwin.util.math.Doubles;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.date.FTimeUnit;
import de.invesdwin.util.time.duration.Duration;

@NotThreadSafe
public class PlotZoomHelper {

    public static final Cursor VERTICAL_RESIZE_CURSOR = new Cursor(Cursor.N_RESIZE_CURSOR);
    public static final Cursor HORIZONTAL_RESIZE_CURSOR = new Cursor(Cursor.W_RESIZE_CURSOR);

    public static final int MAX_ZOOM_ITEM_COUNT = 100_000;
    private static final int MIN_ZOOM_ITEM_COUNT = 10;
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
                final double domainAxisLength = plot.getDomainAxis().getRange().getLength();
                if (domainAxisLength >= MAX_ZOOM_ITEM_COUNT
                        || domainAxisLength >= chartPanel.getMasterDataset().getData().size()) {
                    zoomTitle.setText("Zoom: MAX");
                } else if (domainAxisLength <= MIN_ZOOM_ITEM_COUNT) {
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
        final XYPlot plot = this.chartPanel.getCombinedPlot().getMainPlot();
        plot.addAnnotation(zoomAnnotation);
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
            plot.zoomDomainAxes(zoomFactor, plotInfo, point, true);

            applyEdgeAnchor(rangeBefore, lengthBefore, point.getX(), plotInfo.getDataArea().getWidth());
            plot.setNotify(notifyState); // this generates the change event too
            chartPanel.update();
        } finally {
            chartPanel.decrementUpdatingCount();
        }
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
        final double minLowerBound = getMinLowerBound(data);
        final double maxUpperBound = getMaxUpperBound(data);
        final int gapAfter = chartPanel.getAllowedRangeGap(lengthAfter);
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
        final int length = (int) range.getLength();
        final int gap = chartPanel.getAllowedRangeGap(length);
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

    private double getMinLowerBoundWithGap(final List<? extends TimeRangedOHLCDataItem> data, final int gap) {
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

    public void mousePressed(final MouseEvent e) {
        final Point2D point2D = this.chartPanel.getChartPanel().translateScreenToJava2D(e.getPoint());
        final Axis axis = Axises.getAxisForMousePosition(chartPanel, point2D);
        if (axis != null && MouseEvent.BUTTON1 == e.getButton()) {
            axisDragInfo = Axises.createAxisDragInfo(chartPanel, point2D, axis);
        }

        if (axis != null && Axis.RANGE_AXIS.equals(axis)) {
            final ValueAxis rangeAxis = axisDragInfo != null ? axisDragInfo.getValueAxis()
                    : Axises.getRangeAxis(chartPanel, point2D);
            maybeHandleRangeAxisReset(e, rangeAxis, point2D);
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
                if (Axis.RANGE_AXIS.equals(axisDragInfo.getAxis())) {
                    axisDragInfo.getValueAxis().setAutoRange(false);
                    ((XYPlot) axisDragInfo.getValueAxis().getPlot()).setRangePannable(true);
                }
            }
        }
    }

    public boolean mouseMoved(final MouseEvent e) {
        final Point2D point2D = this.chartPanel.getChartPanel().translateScreenToJava2D(e.getPoint());

        final Axis axis = Axises.getAxisForMousePosition(chartPanel, point2D);
        if (axis != null) {
            prevCursor = chartPanel.getCursor();
            chartPanel.setCursor(Axis.DOMAIN_AXIS.equals(axis) ? HORIZONTAL_RESIZE_CURSOR : VERTICAL_RESIZE_CURSOR);
            return true;
        } else if (prevCursor != null) {
            chartPanel.setCursor(prevCursor);
            prevCursor = null;
        }
        return false;
    }

    private Range calculateNewAxisRange(final Point2D point2D) {
        final double initialDragPoint = Axis.DOMAIN_AXIS.equals(axisDragInfo.getAxis())
                ? axisDragInfo.getInitialDragPoint().getX()
                : axisDragInfo.getInitialDragPoint().getY();
        final double newDragPoint = Axis.DOMAIN_AXIS.equals(axisDragInfo.getAxis()) ? point2D.getX() : point2D.getY();

        final double axisRangeChange = initialDragPoint - newDragPoint;
        final Range range = axisDragInfo.getInitalAxisRange();
        final ValueAxis valueAxis = axisDragInfo.getValueAxis();
        if (axisRangeChange == 0.0D) {
            return null;
        }

        //Check new mouse location in reference to the initialDragStartMouse Position and zoom the axis accordingly
        final double zoomFactor;
        final double axisLength = Axis.DOMAIN_AXIS.equals(axisDragInfo.getAxis()) ? axisDragInfo.getPlotWidth()
                : axisDragInfo.getPlotHeight();
        if (axisRangeChange > 0.0D) {
            zoomFactor = 1D / (1D + Doubles.divide(Math.abs(axisRangeChange), axisLength / 2));
        } else {
            zoomFactor = 1D + Doubles.divide(Math.abs(axisRangeChange), axisLength / 2);
        }

        Range newRange = null;

        if (Axis.RANGE_AXIS.equals(axisDragInfo.getAxis())) {
            final double halfLength = range.getLength() * zoomFactor / 2;
            final Range autoRange = Axises.calculateAutoRange(valueAxis);
            final double autoCentralValue = autoRange.getCentralValue();
            final double centralValue = range.getCentralValue();
            final double centralValueOffset = centralValue - autoCentralValue;
            final double adjustedCentralValueOffset = centralValueOffset * zoomFactor;

            newRange = new Range(autoCentralValue - halfLength + adjustedCentralValueOffset,
                    autoCentralValue + halfLength + adjustedCentralValueOffset);
        } else {
            final double domainAnchor = axisDragInfo.getDomainAnchor();
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
    private void maybeHandleRangeAxisReset(final MouseEvent e, final ValueAxis rangeAxis, final Point2D point2D) {
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

    public boolean isMouseDragZooming() {
        return axisDragInfo != null;
    }
}
