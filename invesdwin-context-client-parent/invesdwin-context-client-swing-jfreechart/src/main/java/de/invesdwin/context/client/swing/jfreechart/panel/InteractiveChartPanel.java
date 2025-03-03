package de.invesdwin.context.client.swing.jfreechart.panel;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;

import com.google.common.util.concurrent.Runnables;

import de.invesdwin.context.client.swing.jfreechart.panel.basis.CustomChartPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.basis.CustomCombinedDomainXYPlot;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.PlotNavigationHelper;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.PlotPanHelper;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.PlotResizeHelper;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.PlotZoomHelper;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.PlotConfigurationHelper;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.indicator.MasterDatasetIndicatorSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.crosshair.PlotCoordinateHelper;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.crosshair.PlotCrosshairHelper;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.legend.PlotLegendHelper;
import de.invesdwin.context.client.swing.jfreechart.plot.CustomXYPlot;
import de.invesdwin.context.client.swing.jfreechart.plot.IndexedDateTimeNumberFormat;
import de.invesdwin.context.client.swing.jfreechart.plot.XYPlots;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.XYNoteIconAnnotation;
import de.invesdwin.context.client.swing.jfreechart.plot.axis.CustomDomainNumberAxis;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IndexedDateTimeOHLCDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.IChartPanelAwareDatasetList;
import de.invesdwin.context.jfreechart.FiniteTickUnitSource;
import de.invesdwin.context.jfreechart.dataset.TimeRangedOHLCDataItem;
import de.invesdwin.context.jfreechart.visitor.AJFreeChartVisitor;
import de.invesdwin.context.jfreechart.visitor.JFreeChartThemeSwing;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.concurrent.Executors;
import de.invesdwin.util.concurrent.WrappedExecutorService;
import de.invesdwin.util.concurrent.lock.ILock;
import de.invesdwin.util.concurrent.lock.Locks;
import de.invesdwin.util.lang.finalizer.AFinalizer;
import de.invesdwin.util.math.Doubles;
import de.invesdwin.util.math.Integers;
import de.invesdwin.util.swing.Components;
import de.invesdwin.util.swing.EventDispatchThreadUtil;
import de.invesdwin.util.swing.listener.KeyListenerSupport;
import de.invesdwin.util.swing.listener.MouseListenerSupport;
import de.invesdwin.util.swing.listener.MouseMotionListenerSupport;
import de.invesdwin.util.swing.listener.MouseWheelListenerSupport;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.util.time.date.FDates;
import de.invesdwin.util.time.date.FTimeUnit;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.util.time.range.TimeRange;

@NotThreadSafe
public class InteractiveChartPanel extends JPanel {

    public static final AJFreeChartVisitor DEFAULT_THEME = new JFreeChartThemeSwing();

    private static final Duration SCROLL_LOCK_DURATION = new Duration(250, FTimeUnit.MILLISECONDS);

    private final IndexedDateTimeOHLCDataset masterDataset;
    private final AJFreeChartVisitor theme;
    private final NumberAxis domainAxis;
    private final CustomCombinedDomainXYPlot combinedPlot;
    private final JFreeChart chart;
    private final CustomChartPanel chartPanel;
    private final IndexedDateTimeNumberFormat domainAxisFormat;
    private final PlotResizeHelper plotResizeHelper;
    private final PlotCrosshairHelper plotCrosshairHelper;
    private final PlotCoordinateHelper plotCoordinateHelper;
    private final PlotLegendHelper plotLegendHelper;
    private final PlotNavigationHelper plotNavigationHelper;
    private final PlotConfigurationHelper plotConfigurationHelper;
    private final PlotZoomHelper plotZoomHelper;
    private final PlotPanHelper plotPanHelper;
    private final MouseMotionListener mouseMotionListener;
    private FDate lastHorizontalScroll = FDates.MIN_DATE;
    private FDate lastVerticalScroll = FDates.MIN_DATE;
    private final AtomicInteger updatingCount = new AtomicInteger();
    private final ILock paintLock = Locks.newReentrantLock(InteractiveChartPanel.class.getSimpleName() + "_paintLock");

    private final InteractiveChartPanelFinalizer finalizer;

    private boolean initialized = false;
    private boolean dragging = false;

    private double userGapRateRight = 0D;

    public InteractiveChartPanel(final IndexedDateTimeOHLCDataset masterDataset) {
        this.masterDataset = masterDataset;
        this.theme = newTheme();
        Assertions.checkNotBlank(masterDataset.getRangeAxisId());
        Assertions.checkNotNull(masterDataset.getPrecision());

        this.finalizer = new InteractiveChartPanelFinalizer();
        this.finalizer.register(this);

        this.plotResizeHelper = new PlotResizeHelper(this);
        this.plotCoordinateHelper = new PlotCoordinateHelper(this);
        this.plotCrosshairHelper = new PlotCrosshairHelper(this);
        this.plotLegendHelper = new PlotLegendHelper(this);
        this.plotNavigationHelper = new PlotNavigationHelper(this);
        this.plotConfigurationHelper = new PlotConfigurationHelper(this);
        this.plotZoomHelper = new PlotZoomHelper(this);
        this.plotPanHelper = new PlotPanHelper(this);
        this.mouseMotionListener = new MouseMotionListenerImpl();

        domainAxis = new CustomDomainNumberAxis(this);
        domainAxis.setAutoRange(false);
        domainAxis.setLabelFont(XYPlots.DEFAULT_FONT);
        domainAxis.setTickLabelFont(XYPlots.DEFAULT_FONT);
        domainAxis.setTickLabelPaint(XYPlots.AXIS_LABEL_PAINT);
        domainAxis.setStandardTickUnits(FiniteTickUnitSource.maybeWrap(NumberAxis.createIntegerTickUnits()));
        domainAxisFormat = new IndexedDateTimeNumberFormat(masterDataset, domainAxis);
        domainAxis.setNumberFormatOverride(domainAxisFormat);

        combinedPlot = new CustomCombinedDomainXYPlot(this);
        combinedPlot.setDataset(masterDataset);
        combinedPlot.setDomainPannable(true);

        masterDataset.addChangeListener(new DatasetChangeListenerImpl());
        initMasterDatasetPlot();
        chart = new JFreeChart(null, null, combinedPlot, false);
        chartPanel = new CustomChartPanel(chart, true) {
            @Override
            protected boolean isPanAllowed() {
                return !isHighlighting();
            }

            @Override
            protected boolean isMousePanningAllowed() {
                return !isLoading();
            }

            @Override
            protected void onMousePanningReleased(final MouseEvent e) {
                getPlotCrosshairHelper().mouseMoved(e);
            }

            @Override
            protected boolean isPaintAllowed() {
                return updatingCount.get() == 0;
            }

            @Override
            public void paintComponent(final Graphics g) {
                paintLock.lock();
                try {
                    super.paintComponent(g);
                } finally {
                    paintLock.unlock();
                }
            }

            @Override
            protected Range maybeLimitDomainRange(final Range range) {
                final Range limitRange = plotZoomHelper.getLimitRange(range);
                if (limitRange != null) {
                    return limitRange;
                } else {
                    return range;
                }
            }

        };

        getTheme().process(chart);

        setLayout(new GridLayout());
        add(chartPanel);

        if (masterDataset.getData() instanceof IChartPanelAwareDatasetList) {
            final IChartPanelAwareDatasetList cData = (IChartPanelAwareDatasetList) masterDataset.getData();
            cData.setChartPanel(this);
        }
        finalizer.executorUpdateLimit.execute(new Runnable() {
            @Override
            public void run() {
                //prevent blocking component initialization
                resetRange(getInitialVisibleItemCount(), getUserGapRateRight());
            }
        });
    }

    /**
     * Override to change the theme.
     */
    protected AJFreeChartVisitor newTheme() {
        return DEFAULT_THEME;
    }

    public AJFreeChartVisitor getTheme() {
        return theme;
    }

    /**
     * When this flag is false, the master dataset series will not be added to the plot.
     */
    protected boolean isMasterDatasetPlottedInitially() {
        return true;
    }

    /**
     * WARNING: when this flag is true, the price series can be removed and a series provider will be registered for
     * adding it again. Though if that is the case, getMasterDataset().getPlot() can return null. This needs to be
     * properly handled.
     */
    protected boolean isMasterDatasetRemovable() {
        return false;
    }

    public void initialize() {
        chartPanel.initialize();
        chartPanel.addMouseWheelListener(new MouseWheelListenerImpl());
        chartPanel.addMouseMotionListener(mouseMotionListener);
        chartPanel.addKeyListener(new KeyListenerImpl());
        chartPanel.setFocusable(true); //key listener only works on focusable panels
        chartPanel.addMouseListener(new MouseListenerImpl());
        chartPanel.addMouseWheelListener(new MouseWheelListenerImpl());
        plotZoomHelper.init();
        plotPanHelper.panLive(null);
        initialized = true;
    }

    public PlotCoordinateHelper getPlotCoordinateHelper() {
        return plotCoordinateHelper;
    }

    public PlotCrosshairHelper getPlotCrosshairHelper() {
        return plotCrosshairHelper;
    }

    public PlotResizeHelper getPlotResizeHelper() {
        return plotResizeHelper;
    }

    public PlotLegendHelper getPlotLegendHelper() {
        return plotLegendHelper;
    }

    public PlotNavigationHelper getPlotNavigationHelper() {
        return plotNavigationHelper;
    }

    public PlotConfigurationHelper getPlotConfigurationHelper() {
        return plotConfigurationHelper;
    }

    public PlotZoomHelper getPlotZoomHelper() {
        return plotZoomHelper;
    }

    public PlotPanHelper getPlotPanHelper() {
        return plotPanHelper;
    }

    public int getDefaultTrailingRangeGapMinimum() {
        return chartPanel.getDefaultTrailingRangeGapMinimum();
    }

    public int getAllowedTrailingRangeGap(final double length) {
        return chartPanel.getAllowedTrailingRangeGap(length);
    }

    public double getDefaultTrailingRangeGapRate() {
        return chartPanel.getDefaultTrailingRangeGapRate();
    }

    public double getDefaultshowAllGapRate() {
        return chartPanel.getDefaultShowAllGapRate();
    }

    public int getAllowedMaximumRangeGap(final double range) {
        return chartPanel.getAllowedMaximumRangeGap(range);
    }

    public IndexedDateTimeOHLCDataset getMasterDataset() {
        return masterDataset;
    }

    public NumberAxis getDomainAxis() {
        return domainAxis;
    }

    public IndexedDateTimeNumberFormat getDomainAxisFormat() {
        return domainAxisFormat;
    }

    public CustomCombinedDomainXYPlot getCombinedPlot() {
        return combinedPlot;
    }

    public JFreeChart getChart() {
        return chart;
    }

    public CustomChartPanel getChartPanel() {
        return chartPanel;
    }

    public void resetRange(final int visibleItemCount, final double gapBefore) {
        resetRange(visibleItemCount, gapBefore, Runnables.doNothing());
    }

    public void resetRange(final int visibleItemCount, final double gapBefore, final Runnable followUp) {
        resetRangeRetry(visibleItemCount, gapBefore, followUp, true);
    }

    private void resetRangeRetry(final int visibleItemCount, final double gapBefore, final Runnable followUp,
            final boolean retryAllowed) {
        if (masterDataset.getItemCount(0) > 0) {
            final FDate firstItemDate = masterDataset.getData().get(0).getStartTime();
            final FDate lastItemDate = masterDataset.getData().get(masterDataset.getItemCount(0) - 1).getStartTime();
            beforeResetRange(visibleItemCount);
            doResetRange(visibleItemCount, gapBefore);
            update();
            final FDate newFirstItemDate = masterDataset.getData().get(0).getStartTime();
            final FDate newLastItemDate = masterDataset.getData().get(masterDataset.getItemCount(0) - 1).getStartTime();
            if (retryAllowed && (!newFirstItemDate.equals(firstItemDate) || !newLastItemDate.equals(lastItemDate))) {
                finalizer.executorUpdateLimit.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            EventDispatchThreadUtil.invokeAndWait(new Runnable() {
                                @Override
                                public void run() {
                                    //retry only once
                                    resetRangeRetry(visibleItemCount, gapBefore, followUp, false);
                                }
                            });
                        } catch (final InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            } else {
                followUp.run();
            }
        } else {
            beforeResetRange(visibleItemCount);
            doResetRange(visibleItemCount, gapBefore);
            update();
            followUp.run();
        }
    }

    protected void doResetRange(final int visibleItemCount, final double gapRateBefore) {
        //Keep at least the defaultTralingRangeGapMinimum
        final int defaultTrailingRangeGapMinimum = getDefaultTrailingRangeGapMinimum();
        int userGapAbsolute = (int) (visibleItemCount * gapRateBefore);
        userGapAbsolute = Math.max(defaultTrailingRangeGapMinimum, userGapAbsolute);
        final int lastItemIndex = masterDataset.getItemCount(0) - 1;
        final int upperBound = lastItemIndex + userGapAbsolute;
        final int lowerBound = upperBound - visibleItemCount;

        final Range range = new Range(lowerBound, upperBound);
        domainAxis.setRange(range);
    }

    protected void beforeResetRange(final int visibleItemCount) {
        if (masterDataset.getData() instanceof IChartPanelAwareDatasetList) {
            final IChartPanelAwareDatasetList cData = (IChartPanelAwareDatasetList) masterDataset.getData();
            cData.resetRange(visibleItemCount);
        }
    }

    public void reloadData() {
        if (masterDataset.getData() instanceof IChartPanelAwareDatasetList) {
            final IChartPanelAwareDatasetList cData = (IChartPanelAwareDatasetList) masterDataset.getData();
            cData.reloadData();
        }
    }

    public void reloadData(final TimeRange timeRange) {
        TimeRange.assertNotNull(timeRange);
        if (masterDataset.getData() instanceof IChartPanelAwareDatasetList) {
            final IChartPanelAwareDatasetList cData = (IChartPanelAwareDatasetList) masterDataset.getData();
            cData.reloadData(timeRange.getFrom(), timeRange.getTo(), new Runnable() {
                @Override
                public void run() {
                    setVisibleTimeRange(timeRange);
                }
            });
        } else {
            setVisibleTimeRange(timeRange);
        }
    }

    public void setVisibleTimeRangeOrReloadData(final TimeRange timeRange) {
        TimeRange.assertNotNull(timeRange);
        final TimeRange available = getAvailableTimeRange();
        if (available == null || available.getFrom().isAfterNotNullSafe(timeRange.getFrom())
                || available.getTo().isBeforeNotNullSafe(timeRange.getTo())) {
            reloadData(timeRange);
        } else {
            setVisibleTimeRange(timeRange);
        }
    }

    public void setVisibleTimeRange(final TimeRange timeRange) {
        TimeRange.assertNotNull(timeRange);
        final int fromIndex = masterDataset.getDateTimeEndAsItemIndex(0, timeRange.getFrom());
        final int toIndex = masterDataset.getDateTimeEndAsItemIndex(0, timeRange.getTo());
        final int lastItemIndex = masterDataset.getItemCount(0) - 1;
        final int lowerBound = fromIndex;
        final int upperBound = toIndex;
        final int minLowerBound = 0;
        final int maxUpperBound = lastItemIndex;
        final Range range = new Range(Doubles.max(minLowerBound, lowerBound), Doubles.min(maxUpperBound, upperBound));
        domainAxis.setRange(range);
    }

    public TimeRange getVisibleTimeRange() {
        final Range range = getDomainAxis().getRange();
        return getTimeRange(range);
    }

    public TimeRange getTimeRange(final Range range) {
        final IndexedDateTimeOHLCDataset masterDataset = getMasterDataset();
        final List<? extends TimeRangedOHLCDataItem> data = masterDataset.getData();
        if (data.isEmpty()) {
            return null;
        }
        final int firstIndex = Integers.max(0, (int) range.getLowerBound());
        final FDate from = data.get(firstIndex).getStartTime();
        final int lastIndex = Integers.min((int) range.getUpperBound(), data.size() - 1);
        final FDate to = data.get(lastIndex).getEndTime();
        return new TimeRange(from, to);
    }

    public TimeRange getAvailableTimeRange() {
        final IndexedDateTimeOHLCDataset masterDataset = getMasterDataset();
        final List<? extends TimeRangedOHLCDataItem> data = masterDataset.getData();
        if (data.isEmpty()) {
            return null;
        }
        final int firstIndex = 0;
        final FDate from = data.get(firstIndex).getStartTime();
        final int lastIndex = data.size() - 1;
        final FDate to = data.get(lastIndex).getEndTime();
        return new TimeRange(from, to);
    }

    public int getInitialVisibleItemCount() {
        return 200;
    }

    public int getLoadInitialDataMasterItemCount(final int visiableItemCount) {
        return getInitialVisibleItemCount();
    }

    public void update() {
        update(true);
    }

    public void update(final boolean disableCrosshair) {
        //have max 2 queue
        if (finalizer.executorUpdateLimit.getPendingCount() <= 1) {
            final Runnable task = new Runnable() {
                @Override
                public void run() {
                    incrementUpdatingCount();
                    try {
                        plotZoomHelper.limitRange(); //do this expensive task outside of EDT
                    } catch (final Throwable t) {
                        handleException("Ignoring, chart might have been closed", t);
                        return;
                    } finally {
                        decrementUpdatingCount();
                    }
                    try {
                        EventDispatchThreadUtil.invokeAndWait(new Runnable() {
                            @Override
                            public void run() {
                                incrementUpdatingCount();
                                try {
                                    //need to do this in EDT, or we get ArrayIndexOutOfBounds exception
                                    if (disableCrosshair) {
                                        plotCrosshairHelper.disableCrosshair(false);
                                    }
                                    configureRangeAxis();
                                    plotLegendHelper.update();
                                    plotCoordinateHelper.updatePinMarker();
                                } catch (final Throwable t) {
                                    handleException(t);
                                    return;
                                } finally {
                                    decrementUpdatingCount();
                                }
                                repaint();
                            }

                        });
                    } catch (final InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            finalizer.executorUpdateLimit.execute(task);
        }
    }

    @Override
    public void repaint() {
        Components.triggerMouseMovedWithWindowActive(InteractiveChartPanel.this, mouseMotionListener);
    }

    public boolean isUpdating() {
        return finalizer.executorUpdateLimit.getPendingCount() > 0 || updatingCount.get() > 0;
    }

    public boolean isLoading() {
        if (getMasterDataset().getData() instanceof IChartPanelAwareDatasetList) {
            final IChartPanelAwareDatasetList cData = (IChartPanelAwareDatasetList) getMasterDataset().getData();
            if (cData.isLoading()) {
                return true;
            }
        }

        return isUpdating();
    }

    public void incrementUpdatingCount() {
        paintLock.lock();
        try {
            updatingCount.incrementAndGet();
        } finally {
            paintLock.unlock();
        }
    }

    public void decrementUpdatingCount() {
        paintLock.lock();
        try {
            updatingCount.decrementAndGet();
        } finally {
            paintLock.unlock();
        }
    }

    public void configureRangeAxis() {
        final List<XYPlot> plots = combinedPlot.getSubplots();
        for (int i = 0; i < plots.size(); i++) {
            final XYPlot plot = plots.get(i);
            //explicitly don't apply theme here to not cause unnecessary object allocations
            XYPlots.configureRangeAxes(plot);
        }
    }

    private final class DatasetChangeListenerImpl implements DatasetChangeListener {
        @Override
        public void datasetChanged(final DatasetChangeEvent event) {
            plotCrosshairHelper.datasetChanged();
            plotCoordinateHelper.datasetChanged();
        }
    }

    private final class MouseListenerImpl extends MouseListenerSupport {

        @Override
        public void mouseExited(final MouseEvent e) {
            try {
                if (plotConfigurationHelper.isShowing()) {
                    return;
                }
                plotCoordinateHelper.mouseExited();
                InteractiveChartPanel.this.mouseExited();
            } catch (final Throwable t) {
                handleException(t);
            }
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            try {
                chartPanel.requestFocusInWindow();

                plotConfigurationHelper.mousePressed(e);
                if (plotConfigurationHelper.isShowing()) {
                    return;
                }

                plotResizeHelper.mousePressed(e);
                plotLegendHelper.mousePressed(e);
                plotNavigationHelper.mousePressed(e);
                plotZoomHelper.mousePressed(e);
                plotPanHelper.mousePressed(e);
                if (!isHighlighting()) {
                    plotCoordinateHelper.mousePressed(e, plotCrosshairHelper.getDomainCrosshairMarkerValueForPinning());
                }
                if (new Duration(lastVerticalScroll).isGreaterThan(SCROLL_LOCK_DURATION)) {
                    if (e.getButton() == 4) {
                        plotPanHelper.panLeft();
                        lastHorizontalScroll = new FDate();
                    } else if (e.getButton() == 5) {
                        plotPanHelper.panRight();
                        lastHorizontalScroll = new FDate();
                    }
                }
            } catch (final Throwable t) {
                handleException(t);
            }
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
            try {
                plotConfigurationHelper.mouseReleased(e);
                if (plotConfigurationHelper.isShowing()) {
                    return;
                }
                plotLegendHelper.mouseReleased(e);
                plotResizeHelper.mouseReleased(e);
                plotNavigationHelper.mouseReleased(e);
                plotZoomHelper.mouseReleased(e);
                plotPanHelper.mouseReleased(e);

                dragging = false;
            } catch (final Throwable t) {
                handleException(t);
            }
        }
    }

    private final class KeyListenerImpl extends KeyListenerSupport {
        @Override
        public void keyPressed(final KeyEvent e) {
            try {
                plotPanHelper.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_ADD) {
                    plotZoomHelper.zoomIn();
                } else if (e.getKeyCode() == KeyEvent.VK_MINUS || e.getKeyCode() == KeyEvent.VK_SUBTRACT) {
                    plotZoomHelper.zoomOut();
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_KP_RIGHT
                        || e.getKeyCode() == KeyEvent.VK_NUMPAD6) {
                    plotPanHelper.panRight();
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_KP_LEFT
                        || e.getKeyCode() == KeyEvent.VK_NUMPAD4) {
                    plotPanHelper.panLeft();
                }
            } catch (final Throwable t) {
                handleException(t);
            }
        }

        @Override
        public void keyReleased(final KeyEvent e) {
            try {
                plotPanHelper.keyReleased(e);
            } catch (final Throwable t) {
                handleException(t);
            }
        }

    }

    private final class MouseMotionListenerImpl extends MouseMotionListenerSupport {

        @Override
        public void mouseDragged(final MouseEvent e) {
            try {
                dragging = true;
                if (plotConfigurationHelper.isShowing() || isLoading()) {
                    return;
                }

                plotResizeHelper.mouseDragged(e);
                plotLegendHelper.mouseDragged(e);
                plotZoomHelper.mouseDragged(e);
                if (plotLegendHelper.isHighlighting()) {
                    plotNavigationHelper.mouseExited();
                } else {
                    plotNavigationHelper.mouseDragged(e);
                }

                updateUserGapRateRight();
                update();
            } catch (final Throwable t) {
                handleException(t);
            }
        }

        @Override
        public void mouseMoved(final MouseEvent e) {
            try {
                if (plotConfigurationHelper.isShowing() || isLoading()) {
                    //keep the crosshair as it is when making a right click screenshot
                    return;
                }
                if (plotLegendHelper.isHighlighting() || plotNavigationHelper.isHighlighting()) {
                    plotCrosshairHelper.disableCrosshair(true);
                    final XYNoteIconAnnotation note = plotNavigationHelper.getNoteShowingIconAnnotation();
                    if (note != null) {
                        plotCoordinateHelper.showNoteDetails(note);
                    } else {
                        plotCoordinateHelper.mouseExited();
                    }
                } else {
                    plotCrosshairHelper.mouseMoved(e);
                }
                plotLegendHelper.mouseMoved(e);
                plotResizeHelper.mouseMoved(e);
                plotNavigationHelper.mouseMoved(e);
            } catch (final Throwable t) {
                handleException(t);
            }
        }

    }

    private final class MouseWheelListenerImpl extends MouseWheelListenerSupport {
        @Override
        public void mouseWheelMoved(final MouseWheelEvent e) {
            try {
                if (new Duration(lastHorizontalScroll).isGreaterThan(SCROLL_LOCK_DURATION)) {
                    if (e.isShiftDown()) {
                        if (e.getWheelRotation() > 0) {
                            plotPanHelper.panLeft();
                        } else {
                            plotPanHelper.panRight();
                        }
                    } else if (e.isControlDown()) {
                        plotCoordinateHelper.mouseWheelMoved(e);
                    } else {
                        plotZoomHelper.mouseWheelMoved(e);
                    }
                    lastVerticalScroll = new FDate();
                }
                chartPanel.requestFocusInWindow();
            } catch (final Throwable t) {
                handleException(t);
            }
        }
    }

    @Override
    public void setCursor(final Cursor cursor) {
        if (!chartPanel.isPanning() && !isHighlighting()) {
            chartPanel.setCursor(cursor);
        }
    }

    @Override
    public Cursor getCursor() {
        return chartPanel.getCursor();
    }

    private boolean isHighlighting() {
        return plotLegendHelper.isHighlighting() || plotNavigationHelper.isHighlighting()
                || plotConfigurationHelper.isShowing();
    }

    public void mouseExited() {
        try {
            plotCrosshairHelper.disableCrosshair(true);
            plotLegendHelper.disableHighlighting();
            plotNavigationHelper.mouseExited();
        } catch (final Throwable t) {
            handleException(t);
        }
    }

    private void initMasterDatasetPlot() {
        if (isMasterDatasetPlottedInitially()) {
            final XYPlot masterDatasetPlot = new CustomXYPlot(combinedPlot, masterDataset, domainAxis,
                    XYPlots.newRangeAxis(getTheme(), 0, false, true),
                    plotConfigurationHelper.getPriceInitialSettings().getPriceRenderer());
            XYPlots.makeThreadSafe(masterDatasetPlot);
            masterDatasetPlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
            plotLegendHelper.addLegendAnnotation(masterDatasetPlot);
            masterDataset.setPlot(masterDatasetPlot);
            //give main plot twice the weight
            combinedPlot.add(masterDatasetPlot, CustomCombinedDomainXYPlot.MAIN_PLOT_WEIGHT);
            XYPlots.updateRangeAxes(getTheme(), masterDatasetPlot);
        } else {
            final XYPlot emptyPlot = combinedPlot.newPlot();
            combinedPlot.add(emptyPlot, CustomCombinedDomainXYPlot.MAIN_PLOT_WEIGHT);
        }

        final boolean masterDatasetRemovable = isMasterDatasetRemovable();
        plotLegendHelper.setDatasetRemovable(masterDataset, masterDatasetRemovable);
        if (masterDatasetRemovable) {
            plotConfigurationHelper.putIndicatorSeriesProvider(new MasterDatasetIndicatorSeriesProvider(this));
        }
    }

    @Override
    public void updateUI() {
        if (plotConfigurationHelper != null) {
            SwingUtilities.updateComponentTreeUI(plotConfigurationHelper.getPopupMenu());
        }
        super.updateUI();
    }

    @Override
    public synchronized void addKeyListener(final KeyListener l) {
        chartPanel.addKeyListener(l);
    }

    private static final class InteractiveChartPanelFinalizer extends AFinalizer {

        private WrappedExecutorService executorUpdateLimit;

        private InteractiveChartPanelFinalizer() {
            this.executorUpdateLimit = Executors
                    .newFixedThreadPool(InteractiveChartPanel.class.getSimpleName() + "_UPDATE_LIMIT", 1)
                    .setDynamicThreadName(false);
        }

        @Override
        protected void clean() {
            executorUpdateLimit.shutdownNow();
            executorUpdateLimit = null;
        }

        @Override
        protected boolean isCleaned() {
            return executorUpdateLimit != null;
        }

        @Override
        public boolean isThreadLocal() {
            return false;
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isDragging() {
        return dragging;
    }

    public double getUserGapRateRight() {
        return userGapRateRight;
    }

    public void setUserGapRateRight(final double userGapRateRight) {
        this.userGapRateRight = userGapRateRight;
    }

    public void updateUserGapRateRight() {
        final int maxUpperBound = plotZoomHelper.getMaxUpperBound();
        updateUserGapRateRight(maxUpperBound);
    }

    public void updateUserGapRateRight(final int maxUpperBound) {
        //Limit-User-Gap
        final double domainRangeLength = domainAxis.getRange().getLength();

        double newUserGapRateRight = calculateUserGapRateRight(maxUpperBound, domainRangeLength);

        if (newUserGapRateRight > chartPanel.getAllowedMaximumRangeGapRate()) {
            newUserGapRateRight = chartPanel.getAllowedMaximumRangeGapRate();
        }
        this.userGapRateRight = newUserGapRateRight;
    }

    public double calculateUserGapRateRight(final int maxUpperBound, final double domainRangeLength) {
        return maxUpperBound < domainAxis.getRange().getUpperBound()
                ? (domainAxis.getRange().getUpperBound() - maxUpperBound) / domainRangeLength
                : 0;
    }

    private void handleException(final Throwable t) {
        handleException("Ignoring", t);
    }

    protected void handleException(final String message, final Throwable t) {
        Err.process(new Exception(message, t));
    }
}
