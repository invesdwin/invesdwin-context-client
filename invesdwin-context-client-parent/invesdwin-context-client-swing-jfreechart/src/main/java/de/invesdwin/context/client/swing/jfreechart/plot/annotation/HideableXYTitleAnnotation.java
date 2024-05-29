package de.invesdwin.context.client.swing.jfreechart.plot.annotation;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.Title;
import org.jfree.chart.ui.RectangleAnchor;

@NotThreadSafe
public class HideableXYTitleAnnotation extends XYTitleAnnotation {

    private boolean hidden = false;

    public HideableXYTitleAnnotation(final double x, final double y, final Title title) {
        super(x, y, title);
    }

    public HideableXYTitleAnnotation(final double x, final double y, final Title title, final RectangleAnchor anchor) {
        super(x, y, title, anchor);
    }

    @Override
    public void draw(final Graphics2D g2, final XYPlot plot, final Rectangle2D dataArea, final ValueAxis domainAxis,
            final ValueAxis rangeAxis, final int rendererIndex, final PlotRenderingInfo info) {
        if (!hidden) {
            super.draw(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info);
        }
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }
}
