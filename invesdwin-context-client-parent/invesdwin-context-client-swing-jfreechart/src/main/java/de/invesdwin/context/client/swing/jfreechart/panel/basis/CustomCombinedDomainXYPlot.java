package de.invesdwin.context.client.swing.jfreechart.panel.basis;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.plot.XYPlots;
import de.invesdwin.util.swing.EventDispatchThreadUtil;

@NotThreadSafe
public class CustomCombinedDomainXYPlot extends CombinedDomainXYPlot {

    public static final Color DEFAULT_BACKGROUND_COLOR = (Color) DEFAULT_BACKGROUND_PAINT;
    public static final int INVISIBLE_PLOT_WEIGHT = 0;
    public static final int INITIAL_PLOT_WEIGHT = 1000;
    public static final int MAIN_PLOT_WEIGHT = INITIAL_PLOT_WEIGHT * 2;
    public static final int EMPTY_PLOT_WEIGHT = INITIAL_PLOT_WEIGHT / 5;
    private final InteractiveChartPanel chartPanel;
    private final XYPlot trashPlot;

    public CustomCombinedDomainXYPlot(final InteractiveChartPanel chartPanel) {
        super(chartPanel.getDomainAxis());
        XYPlots.makeThreadSafe(this);
        this.chartPanel = chartPanel;
        trashPlot = chartPanel.newPlot(this);
        trashPlot.getRangeAxis().setVisible(false);
        trashPlot.setDomainGridlinesVisible(false);
        trashPlot.setRangeGridlinesVisible(false);
        chartPanel.getPlotLegendHelper().addLegendAnnotation(trashPlot);
        add(trashPlot, INVISIBLE_PLOT_WEIGHT);
    }

    public XYPlot getTrashPlot() {
        return trashPlot;
    }

    public int getSubplotIndex(final int mouseX, final int mouseY) {
        return getSubplotIndex(chartPanel.getChartPanel().translateScreenToJava2D(new Point(mouseX, mouseY)));
    }

    public boolean isSubplotAtBottomEdge(final XYPlot plot) {
        final List<XYPlot> plots = getSubplots();
        for (int i = plots.size() - 1; i >= 0; i--) {
            final XYPlot potentialPlot = plots.get(i);
            if (potentialPlot.getWeight() == INVISIBLE_PLOT_WEIGHT) {
                continue;
            }
            if (potentialPlot == plot) {
                return true;
            } else {
                break;
            }
        }
        return false;
    }

    public boolean isSubplotAtTopEdge(final XYPlot plot) {
        final List<XYPlot> plots = getSubplots();
        for (int i = 0; i < plots.size(); i++) {
            final XYPlot potentialPlot = plots.get(i);
            if (potentialPlot.getWeight() == INVISIBLE_PLOT_WEIGHT) {
                continue;
            }
            if (potentialPlot == plot) {
                return true;
            } else {
                break;
            }
        }
        return false;
    }

    public int getSubplotIndex(final Point2D java2DPoint) {
        final ChartRenderingInfo chartInfo = chartPanel.getChartPanel().getChartRenderingInfo();
        // see if the point is in one of the subplots; this is the
        // intersection of the range and domain crosshairs
        return chartInfo.getPlotInfo().getSubplotIndex(java2DPoint);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<XYPlot> getSubplots() {
        return super.getSubplots();
    }

    public boolean isSubplotVisible(final XYPlot plot) {
        final List<XYPlot> plots = getSubplots();
        for (int i = 0; i < plots.size(); i++) {
            final XYPlot potentialPlot = plots.get(i);
            if (potentialPlot == plot) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
    @Override
    public void add(final XYPlot subplot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(final XYPlot subplot, final int weight) {
        if (chartPanel.isInitialized()) {
            EventDispatchThreadUtil.assertEventDispatchThread();
        }
        if (weight == 0) {
            super.add(subplot, 1);
            subplot.setWeight(0);
        } else {
            super.add(subplot, weight);
        }
    }

    @Override
    public void remove(final XYPlot subplot) {
        if (chartPanel.isInitialized()) {
            EventDispatchThreadUtil.assertEventDispatchThread();
        }
        super.remove(subplot);
        chartPanel.getPlotCoordinateHelper().removePinMarker(subplot);
        chartPanel.update();
    }

    /**
     * We override this so that when we start Panning (via Mouse-Drag) on one plot and we drag the mouse off the plot
     * (maybe even into another plot), we still keep panning. The default behaviour of the CombinedDomainXYPlot is that
     * you can only pan on the range-axis when you are moving the mouse in the specific plot-area.
     */
    @Override
    public void panRangeAxes(final double panRange, final PlotRenderingInfo info, final Point2D source) {
        final XYPlot panStartPlot = chartPanel.getPlotPanHelper().getPanStartPlot();
        if (panStartPlot != null) {
            if (!panStartPlot.isRangePannable()) {
                return;
            }
            for (int i = 0; i < panStartPlot.getRangeAxisCount(); i++) {
                final ValueAxis rangeAxis = panStartPlot.getRangeAxis(i);
                if (rangeAxis != null) {
                    rangeAxis.pan(panRange);
                }
            }
        } else {
            //Not sure if this is needed but it wont break things.
            super.panRangeAxes(panRange, info, source);
        }
    }

}
