package de.invesdwin.context.client.swing.jfreechart.panel.helper;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.plot.Axis;
import de.invesdwin.context.client.swing.jfreechart.plot.Axises;
import de.invesdwin.context.client.swing.jfreechart.plot.XYPlots;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.list.MasterLazyDatasetList;
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
        if (chartPanel.isLoading()) {
            return;
        }
        final Range range = chartPanel.getDomainAxis().getRange();
        final double length = range.getLength();
        final double newLowerBound = Doubles.max(range.getLowerBound() - length * scrollFactor,
                0 - chartPanel.getAllowedRangeGap(length));
        final Range newRange = new Range(newLowerBound, newLowerBound + length);
        chartPanel.getDomainAxis().setRange(newRange);
        chartPanel.update();
    }

    private double getPanLiveUpperBound() {
        return chartPanel.getPlotZoomHelper()
                .getMaxUpperBoundWithGap(chartPanel.getMasterDataset().getData(),
                        chartPanel.getAllowedRangeGap(chartPanel.getDomainAxis().getRange().getLength()));
    }

    public void panRight() {
        if (chartPanel.isLoading()) {
            return;
        }
        final Range range = chartPanel.getDomainAxis().getRange();
        final double length = range.getLength();
        final double newUpperBound = Doubles.min(range.getUpperBound() + length * scrollFactor, getPanLiveUpperBound());
        final Range newRange = new Range(newUpperBound - length, newUpperBound);
        chartPanel.getDomainAxis().setRange(newRange);
        chartPanel.update();
    }

    /**
     * pans the x-axis right till the most recent datapoint is visible.
     */
    public void panLive(final MouseEvent e) {
        final double length = chartPanel.getDomainAxis().getRange().getLength();
        final double newUpperBound = getPanLiveUpperBound();
        final Range newRange = new Range(newUpperBound - length, newUpperBound);
        chartPanel.getDomainAxis().setRange(newRange);
        //pan live button is removed, thus switch to crosshair
        chartPanel.getPlotNavigationHelper().mouseMoved(e);
        chartPanel.getPlotCrosshairHelper().mouseMoved(e);
        chartPanel.update(false);
    }

    public void maybeToggleVisibilityPanLiveIcon() {
        if (MasterLazyDatasetList.isTrailingRange(chartPanel.getDomainAxis().getRange(),
                chartPanel.getMasterDataset().getItemCount(0))) {
            chartPanel.getPlotNavigationHelper().hidePanLiveIcon();
        } else {
            chartPanel.getPlotNavigationHelper().showPanLiveIcon();
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
        panStartPlot = XYPlots.getSubplot(chartPanel, e);
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
            final Axis axis = Axises.getAxisForMousePosition(chartPanel, point2D);
            if (axis != null && Axis.DOMAIN_AXIS.equals(axis)) {
                chartPanel.resetRange(chartPanel.getInitialVisibleItemCount());
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
