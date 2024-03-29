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
import de.invesdwin.util.lang.color.Colors;
import de.invesdwin.util.math.decimal.scaled.Percent;
import de.invesdwin.util.math.decimal.scaled.PercentScale;

/**
 * Instead of drawing an outline, this one draws a line so that at start and end of series the line does not go to zero.
 * 
 * @author subes
 *
 */
@NotThreadSafe
public class CustomProfitLossRenderer extends ACustomEquityChangeRenderer
        implements ICustomRendererType, IDelegatePriceLineXYItemRenderer {

    public static final Percent TRANSPARENCY = new Percent(50, PercentScale.PERCENT);
    public static final Color DOWN_COLOR = Colors.fromHex("#CC0000");
    public static final Color UP_COLOR = Colors.fromHex("#38761D");

    private final IPlotSourceDataset dataset;
    private Color upColor;
    private Color downColor;
    private final XYPriceLineAnnotation priceLineAnnotation;

    public CustomProfitLossRenderer(final PlotConfigurationHelper plotConfigurationHelper,
            final IPlotSourceDataset dataset) {
        this.dataset = dataset;
        final PriceInitialSettings config = plotConfigurationHelper.getPriceInitialSettings();

        setDefaultPaint(Colors.setTransparency(UP_COLOR, TRANSPARENCY));
        setDefaultStroke(config.getSeriesStroke());

        this.upColor = Colors.setTransparency(UP_COLOR, TRANSPARENCY);
        this.downColor = Colors.setTransparency(DOWN_COLOR, TRANSPARENCY);

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
        return false;
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
    public String getUpColorName() {
        return "Profit";
    }

    @Override
    public String getDownColorName() {
        return "Loss";
    }

}