package de.invesdwin.context.client.swing.jfreechart.panel.helper;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.data.Range;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.plot.Axis;
import de.invesdwin.context.client.swing.jfreechart.plot.Axises;
import de.invesdwin.util.math.Doubles;

@NotThreadSafe
public class PlotPanHelper {

    private static final double DEFAULT_SCROLL_FACTOR = 0.05D;
    private static final double FASTER_SCROLL_FACTOR = DEFAULT_SCROLL_FACTOR * 3D;
    private double scrollFactor = DEFAULT_SCROLL_FACTOR;

    private final InteractiveChartPanel chartPanel;

    public PlotPanHelper(final InteractiveChartPanel chartPanel) {
        this.chartPanel = chartPanel;
    }

    public void panLeft() {
        if (chartPanel.isUpdating()) {
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

    public void panRight() {
        if (chartPanel.isUpdating()) {
            return;
        }
        final Range range = chartPanel.getDomainAxis().getRange();
        final double length = range.getLength();
        final double newUpperBound = Doubles.min(range.getUpperBound() + length * scrollFactor,
                chartPanel.getMasterDataset().getItemCount(0) + chartPanel.getAllowedRangeGap(length));
        final Range newRange = new Range(newUpperBound - length, newUpperBound);
        chartPanel.getDomainAxis().setRange(newRange);
        chartPanel.update();
    }

    /**
     * pans the x-axis right till the most recent datapoint is visible.
     */
    public void panLive(final MouseEvent e) {
        final Range range = chartPanel.getDomainAxis().getRange();
        final double length = range.getLength();
        final double newUpperBound = chartPanel.getMasterDataset().getItemCount(0)
                + chartPanel.getAllowedRangeGap(length);
        final Range newRange = new Range(newUpperBound - length, newUpperBound);
        chartPanel.getDomainAxis().setRange(newRange);
        //pan live button is removed, thus switch to crosshair
        chartPanel.getPlotNavigationHelper().mouseMoved(e);
        chartPanel.getPlotCrosshairHelper().mouseMoved(e);
        chartPanel.update(false);
    }

    public void maybeToggleVisibilityPanLiveIcon() {
        if (chartPanel.getMasterDataset().getItemCount(0) > chartPanel.getDomainAxis().getRange().getUpperBound()) {
            chartPanel.getPlotNavigationHelper().showPanLiveIcon();
        } else {
            chartPanel.getPlotNavigationHelper().hidePanLiveIcon();
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
        maybeHandleDomainAxisDoubleClick(e);
    }

    private void maybeHandleDomainAxisDoubleClick(final MouseEvent e) {
        if (e.getClickCount() == 2) {
            final Point2D point2D = chartPanel.getChartPanel().translateScreenToJava2D(e.getPoint());
            final Axis axis = Axises.getAxisForMousePosition(chartPanel, point2D);
            if (axis != null && Axis.DOMAIN_AXIS.equals(axis)) {
                panLive(e);
            }
        }
    }

}
