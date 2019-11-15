package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom;

import java.awt.Color;
import java.awt.Paint;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.PlotConfigurationHelper;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.PriceInitialSettings;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.IDelegatePriceLineXYItemRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.IPriceLineRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.XYPriceLineAnnotation;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.internal.ACustomEquityChangeRenderer;
import de.invesdwin.util.lang.Colors;
import de.invesdwin.util.math.decimal.scaled.Percent;
import de.invesdwin.util.math.decimal.scaled.PercentScale;

/**
 * Instead of drawing an outline, this one draws a line so that at start and end of series the line does not go to zero.
 * 
 * @author subes
 *
 */
@NotThreadSafe
public class CustomEquityChangeRenderer extends ACustomEquityChangeRenderer
        implements ICustomRendererType, IDelegatePriceLineXYItemRenderer {

    public static final Color UP_COLOR = CustomProfitLossRenderer.UP_COLOR;
    public static final Color DOWN_COLOR = CustomProfitLossRenderer.DOWN_COLOR;
    public static final Percent AREA_TRANSPARENCY = new Percent(90, PercentScale.PERCENT);
    public static final Percent LINE_TRANSPARENCY = new Percent(80, PercentScale.PERCENT);

    private final IPlotSourceDataset dataset;
    private final XYPriceLineAnnotation priceLineAnnotation;
    private Color upColor;
    private Color downColor;

    public CustomEquityChangeRenderer(final PlotConfigurationHelper plotConfigurationHelper,
            final IPlotSourceDataset dataset) {
        this.dataset = dataset;

        final PriceInitialSettings config = plotConfigurationHelper.getPriceInitialSettings();

        setSeriesPaint(0, Colors.setTransparency(UP_COLOR, LINE_TRANSPARENCY));
        setSeriesStroke(0, config.getSeriesStroke());

        this.upColor = Colors.setTransparency(UP_COLOR, AREA_TRANSPARENCY);
        this.downColor = Colors.setTransparency(DOWN_COLOR, AREA_TRANSPARENCY);
        this.priceLineAnnotation = new XYPriceLineAnnotation(dataset, this);
        addAnnotation(priceLineAnnotation);
    }

    @Override
    public IPlotSourceDataset getDataset() {
        return dataset;
    }

    @Override
    public IPriceLineRenderer getDelegatePriceLineRenderer() {
        return priceLineAnnotation;
    }

    @Override
    public Paint getItemPaint(final int row, final int column) {
        final double yValue;
        final int lastDatasetItem = dataset.getItemCount(row) - 1;
        if (column >= lastDatasetItem && column > 0) {
            final double lastY = dataset.getYValue(row, lastDatasetItem);
            if (Double.isNaN(lastY)) {
                yValue = dataset.getYValue(row, lastDatasetItem - 1);
            } else {
                yValue = lastY;
            }
        } else {
            yValue = dataset.getYValue(row, column);
        }
        if (Double.isNaN(yValue)) {
            return Colors.INVISIBLE_COLOR;
        } else if (yValue >= 0) {
            return upColor;
        } else {
            return downColor;
        }
    }

    @Override
    public boolean isLineStyleConfigurable() {
        return true;
    }

    @Override
    public boolean isLineWidthConfigurable() {
        return true;
    }

    @Override
    public boolean isSeriesColorConfigurable() {
        return true;
    }

    @Override
    public String getName() {
        return "ProfitLoss";
    }

    @Override
    public void setUpColor(final Color upColor) {
        this.upColor = upColor;
        fireChangeEvent();
    }

    @Override
    public Color getUpColor() {
        return upColor;
    }

    @Override
    public void setDownColor(final Color downColor) {
        this.downColor = downColor;
        fireChangeEvent();
    }

    @Override
    public Color getDownColor() {
        return downColor;
    }

    @Override
    public String getSeriesColorName() {
        return "EquityChange";
    }

    @Override
    public String getUpColorName() {
        return "Profit";
    }

    @Override
    public String getDownColorName() {
        return "Drawdown";
    }

}