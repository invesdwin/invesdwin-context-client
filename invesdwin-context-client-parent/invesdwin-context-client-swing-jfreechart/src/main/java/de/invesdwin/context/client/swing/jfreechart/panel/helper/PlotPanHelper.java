package de.invesdwin.context.client.swing.jfreechart.panel.helper;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.plot.XYPlots;
import de.invesdwin.context.client.swing.jfreechart.plot.axis.Axises;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.MasterLazyDatasetList;
import de.invesdwin.context.jfreechart.axis.AxisType;
import de.invesdwin.util.math.Doubles;

@NotThreadSafe
public class PlotPanHelper {

    private static final double DEFAULT_SCROLL_FACTOR = 0.05D;
    private static final double FASTER_SCROLL_FACTOR = DEFAULT_SCROLL_FACTOR * 3D;
    private double scrollFactor = DEFAULT_SCROLL_FACTOR;

    private final InteractiveChartPanel chartPanel;
    private XYPlot panStartPlot;

    public PlotPanHelper(final InteractiveChartPanel chartPanel) {
        this.chartPanel = chartPanel;
    }

    public void panLeft() {
        final Range range = chartPanel.getDomainAxis().getRange();
        final double length = range.getLength();
        final double newLowerBound = Doubles.max(range.getLowerBound() - length * scrollFactor,
                0 - chartPanel.getAllowedMaximumRangeGap(length));
        Range newRange = new Range(newLowerBound, newLowerBound + length);
        final Range limitRange = chartPanel.getPlotZoomHelper().getLimitRange(newRange);
        if (limitRange != null) {
            newRange = limitRange;
        }

        chartPanel.getDomainAxis().setRange(newRange);
        chartPanel.updateUserGapRateRight();
    }

    public double getPanLiveUpperBound() {
        return chartPanel.getPlotZoomHelper()
                .getMaxUpperBoundWithGap(chartPanel.getMasterDataset().getData(),
                        chartPanel.getAllowedTrailingRangeGap(chartPanel.getDomainAxis().getRange().getLength()));
    }

    public void panRight() {
        final Range range = chartPanel.getDomainAxis().getRange();
        final double length = range.getLength();
        final double newUpperBound = Doubles.min(range.getUpperBound() + length * scrollFactor,
                range.getUpperBound() + chartPanel.getAllowedMaximumRangeGap(length));

        Range newRange = new Range(newUpperBound - length, newUpperBound);
        final Range limitRange = chartPanel.getPlotZoomHelper().getLimitRange(newRange);
        if (limitRange != null) {
            newRange = limitRange;
        }

        chartPanel.getDomainAxis().setRange(newRange);
        chartPanel.updateUserGapRateRight();
    }

    /**
     * pans the x-axis right till the most recent datapoint is visible.
     */
    public void panLive(final MouseEvent e) {
        final int length = (int) chartPanel.getDomainAxis().getRange().getLength();
        final double defaultTrailingRangeGapRate = chartPanel.getChartPanel().getDefaultTrailingRangeGapRate();
        chartPanel.resetRange(length, defaultTrailingRangeGapRate);
        chartPanel.updateUserGapRateRight();

        //pan live button is removed, thus switch to crosshair
        if (e != null) {
            chartPanel.getPlotNavigationHelper().mouseMoved(e);
            chartPanel.getPlotCrosshairHelper().mouseMoved(e);
        }
    }

    /**
     * Return's if Nav-Icons have been changed/updated.
     */
    public boolean maybeToggleVisibilityPanLiveIcon() {
        if (MasterLazyDatasetList.isTrailingRange(chartPanel.getDomainAxis().getRange(),
                chartPanel.getMasterDataset().getItemCount(0))) {
            final double currUpperBound = chartPanel.getDomainAxis().getRange().getUpperBound();
            final double panLiveUpperBound = chartPanel.getPlotPanHelper().getPanLiveUpperBound();
            if (currUpperBound > panLiveUpperBound) {
                //We scrolled beyond the PanLive-UpperBound and want to show the Scroll-Backwards-PanLive-Icon
                return chartPanel.getPlotNavigationHelper().showPanLiveIcon(true);
            } else {
                //We actually scrolled somewhere before the trailing range and want to show the regular Pan-Live-Button
                return chartPanel.getPlotNavigationHelper().hidePanLiveIcon();
            }
        } else {
            //We are trailing and don't need to show the PanLive-Button
            return chartPanel.getPlotNavigationHelper().showPanLiveIcon(false);
        }
    }

    public void keyPressed(final KeyEvent e) {
        if (e.isControlDown()) {
            scrollFactor = FASTER_SCROLL_FACTOR;
        } else {
            scrollFactor = DEFAULT_SCROLL_FACTOR;
        }

    }

    public void keyReleased(final KeyEvent e) {
        scrollFactor = DEFAULT_SCROLL_FACTOR;
    }

    public void mousePressed(final MouseEvent e) {
        if (chartPanel.getPlotLegendHelper().isHighlighting()) {
            // If we are hovering over a LegendItem we don't want to pan on the plot.
            return;
        }
        panStartPlot = chartPanel.getCombinedPlot().getSubplot(e);
        XYPlots.disableRangePannables(chartPanel, panStartPlot);
        maybeHandleDomainAxisReset(e);
    }

    public void mouseReleased(final MouseEvent e) {
        panStartPlot = null;
        XYPlots.setSuitableRangePannablesForSubplots(chartPanel);
    }

    /**
     * Domain axis resets on Double-Left-Click or Single-Middle-Mouse-Button-Click (Scrollwheel).
     */
    private void maybeHandleDomainAxisReset(final MouseEvent e) {
        if ((MouseEvent.BUTTON1 == e.getButton() && e.getClickCount() == 2) || MouseEvent.BUTTON2 == e.getButton()) {
            final Point2D point2D = chartPanel.getChartPanel().translateScreenToJava2D(e.getPoint());
            final AxisType axis = Axises.getAxisForMousePosition(chartPanel, point2D);
            if (axis != null && AxisType.DOMAIN_AXIS.equals(axis)) {
                chartPanel.resetRange(chartPanel.getInitialVisibleItemCount(), chartPanel.getUserGapRateRight());
            }
        }
    }

    public XYPlot getPanStartPlot() {
        return panStartPlot;
    }

    public boolean isPanning() {
        return panStartPlot != null;
    }
}
