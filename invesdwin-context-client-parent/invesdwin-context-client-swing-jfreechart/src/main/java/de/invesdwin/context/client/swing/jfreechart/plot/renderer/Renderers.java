package de.invesdwin.context.client.swing.jfreechart.plot.renderer;

import javax.annotation.concurrent.Immutable;

import org.jfree.chart.renderer.AbstractRenderer;

@Immutable
public final class Renderers {

    private Renderers() {
    }

    public static void disableAutoPopulate(final AbstractRenderer renderer) {
        renderer.setAutoPopulateSeriesFillPaint(false);
        renderer.setAutoPopulateSeriesOutlinePaint(false);
        renderer.setAutoPopulateSeriesOutlineStroke(false);
        renderer.setAutoPopulateSeriesPaint(false);
        renderer.setAutoPopulateSeriesShape(false);
        renderer.setAutoPopulateSeriesStroke(false);
    }

}
