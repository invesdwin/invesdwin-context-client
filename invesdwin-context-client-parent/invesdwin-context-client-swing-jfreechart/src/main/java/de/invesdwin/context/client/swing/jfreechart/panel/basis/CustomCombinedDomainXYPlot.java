package de.invesdwin.context.client.swing.jfreechart.panel.basis;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;

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
        this.chartPanel = chartPanel;
        trashPlot = chartPanel.newPlot();
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

    public XYPlot getMainPlot() {
        return getSubplots().get(1);
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
    }

}
