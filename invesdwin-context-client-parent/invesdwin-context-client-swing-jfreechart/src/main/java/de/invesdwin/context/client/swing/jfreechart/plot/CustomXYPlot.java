package de.invesdwin.context.client.swing.jfreechart.plot;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;

import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.AnnotationRenderingInfo;

@NotThreadSafe
public class CustomXYPlot extends XYPlot {

    private final AnnotationRenderingInfo annotationRenderingInfo;

    public CustomXYPlot(final XYDataset dataset, final ValueAxis domainAxis, final ValueAxis rangeAxis,
            final XYItemRenderer renderer) {
        super(dataset, domainAxis, rangeAxis, renderer);
        this.annotationRenderingInfo = new AnnotationRenderingInfo();
    }

    @Override
    public void draw(final Graphics2D g2, final Rectangle2D area, final Point2D anchor, final PlotState parentState,
            final PlotRenderingInfo info) {
        annotationRenderingInfo.beforePlotDraw();
        super.draw(g2, area, anchor, parentState, info);
    }

    public AnnotationRenderingInfo getAnnotationRenderingInfo() {
        return annotationRenderingInfo;
    }

}
