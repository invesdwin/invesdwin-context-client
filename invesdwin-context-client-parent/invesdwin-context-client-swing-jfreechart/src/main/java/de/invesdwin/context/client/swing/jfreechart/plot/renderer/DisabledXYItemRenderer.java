package de.invesdwin.context.client.swing.jfreechart.plot.renderer;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;

import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.ACustomXYItemRenderer;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.color.Colors;

@NotThreadSafe
public class DisabledXYItemRenderer extends ACustomXYItemRenderer implements IDatasetSourceXYItemRenderer {

    private final IDatasetSourceXYItemRenderer enabledRenderer;

    public DisabledXYItemRenderer(final IDatasetSourceXYItemRenderer enabledRenderer) {
        Renderers.disableAutoPopulate(this);

        Assertions.checkNotNull(enabledRenderer);
        if (enabledRenderer instanceof DisabledXYItemRenderer) {
            throw new IllegalArgumentException(
                    "enabledRenderer should not be an instance of " + DisabledXYItemRenderer.class.getSimpleName());
        }
        this.enabledRenderer = enabledRenderer;

    }

    public XYItemRenderer getEnabledRenderer() {
        return enabledRenderer;
    }

    @Override
    public void drawItem(final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea,
            final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis,
            final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState,
            final int pass) {
        //not drawing anything
    }

    public static XYItemRenderer maybeUnwrap(final XYItemRenderer renderer) {
        if (renderer instanceof DisabledXYItemRenderer) {
            final DisabledXYItemRenderer cRenderer = (DisabledXYItemRenderer) renderer;
            return cRenderer.getEnabledRenderer();
        } else {
            return renderer;
        }
    }

    @Override
    public void setSeriesStroke(final int series, final Stroke stroke, final boolean notify) {
        enabledRenderer.setSeriesStroke(series, stroke, notify);
    }

    @Override
    public void setSeriesStroke(final int series, final Stroke stroke) {
        enabledRenderer.setSeriesStroke(series, stroke);
    }

    @Override
    public Stroke getSeriesStroke(final int series) {
        return enabledRenderer.getSeriesStroke(series);
    }

    @Override
    public void setSeriesPaint(final int series, final Paint paint, final boolean notify) {
        enabledRenderer.setSeriesPaint(series, paint, notify);
    }

    @Override
    public void setSeriesPaint(final int series, final Paint paint) {
        enabledRenderer.setSeriesPaint(series, paint);
    }

    @Override
    public Paint getSeriesPaint(final int series) {
        return enabledRenderer.getSeriesPaint(series);
    }

    @Override
    public void setSeriesFillPaint(final int series, final Paint paint, final boolean notify) {
        enabledRenderer.setSeriesFillPaint(series, paint, notify);
    }

    @Override
    public void setSeriesFillPaint(final int series, final Paint paint) {
        enabledRenderer.setSeriesFillPaint(series, paint);
    }

    @Override
    public Paint getSeriesFillPaint(final int series) {
        return enabledRenderer.getSeriesFillPaint(series);
    }

    @Override
    public void setDefaultStroke(final Stroke stroke, final boolean notify) {
        enabledRenderer.setDefaultStroke(stroke, notify);
    }

    @Override
    public void setDefaultStroke(final Stroke stroke) {
        enabledRenderer.setDefaultStroke(stroke);
    }

    @Override
    public Stroke getDefaultStroke() {
        return enabledRenderer.getDefaultStroke();
    }

    @Override
    public void setDefaultPaint(final Paint paint, final boolean notify) {
        enabledRenderer.setDefaultPaint(paint, notify);
    }

    @Override
    public void setDefaultPaint(final Paint paint) {
        enabledRenderer.setDefaultPaint(paint);
    }

    @Override
    public Paint getDefaultPaint() {
        return enabledRenderer.getDefaultPaint();
    }

    @Override
    public void setDefaultFillPaint(final Paint paint, final boolean notify) {
        enabledRenderer.setDefaultFillPaint(paint, notify);
    }

    @Override
    public void setDefaultFillPaint(final Paint paint) {
        enabledRenderer.setDefaultFillPaint(paint);
    }

    @Override
    public Paint getDefaultFillPaint() {
        return enabledRenderer.getDefaultFillPaint();
    }

    @Override
    public Paint getItemPaint(final int row, final int column) {
        return Colors.INVISIBLE_COLOR;
    }

    @Override
    protected void updateCrosshairValues(final CrosshairState crosshairState, final double x, final double y,
            final int datasetIndex, final double transX, final double transY, final PlotOrientation orientation) {
        //noop
    }

    @Override
    protected void addEntity(final EntityCollection entities, final Shape hotspot, final XYDataset dataset,
            final int series, final int item, final double entityX, final double entityY) {
        //noop
    }

    @Override
    public IPlotSourceDataset getDataset() {
        return enabledRenderer.getDataset();
    }

}
