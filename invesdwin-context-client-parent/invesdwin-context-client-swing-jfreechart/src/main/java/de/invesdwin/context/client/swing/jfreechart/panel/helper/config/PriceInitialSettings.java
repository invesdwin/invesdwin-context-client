package de.invesdwin.context.client.swing.jfreechart.panel.helper.config;

import java.awt.Color;
import java.awt.Stroke;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;

import de.invesdwin.context.client.swing.jfreechart.plot.XYPlots;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IndexedDateTimeOHLCDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.FastCandlestickRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.FastOHLCRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.FastXYAreaRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.FastXYLineRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.FastXYStepRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.IDatasetSourceXYItemRenderer;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.lang.color.Colors;

@NotThreadSafe
public class PriceInitialSettings {

    public static final Color DEFAULT_DOWN_COLOR = Colors.fromHex("#EF5350");
    public static final Color DEFAULT_UP_COLOR = Colors.fromHex("#26A69A");
    public static final Color DEFAULT_SERIES_COLOR = Colors.fromHex("#3C78D8");
    public static final Stroke DEFAULT_SERIES_STROKE = LineStyleType.Solid.getStroke(LineWidthType._1);
    public static final boolean DEFAULT_PRICE_LINE_VISIBLE = true;
    public static final boolean DEFAULT_PRICE_LABEL_ENABLED = true;

    private final PlotConfigurationHelper plotConfigurationHelper;

    private final FastCandlestickRenderer candlestickRenderer;
    private final FastOHLCRenderer ohlcRenderer;
    private final FastXYAreaRenderer areaRenderer;
    private final FastXYLineRenderer lineRenderer;
    private final FastXYStepRenderer stepLineRenderer;

    /*
     * the renderers can diverge from these settings using the context menu configuration, though a reset will use these
     * values here again. using the setters will override the context menu configuration
     */
    private PriceRendererType priceRendererType = PriceRendererType.DEFAULT;
    private Color upColor;
    private Color downColor;
    private Color seriesColor;
    private Stroke seriesStroke;
    private boolean priceLineVisible;
    private boolean priceLabelVisible;
    private String rangeAxisId;

    public PriceInitialSettings(final PlotConfigurationHelper plotConfigurationHelper) {
        this.plotConfigurationHelper = plotConfigurationHelper;

        final IndexedDateTimeOHLCDataset dataset = plotConfigurationHelper.getChartPanel().getMasterDataset();
        this.candlestickRenderer = new FastCandlestickRenderer(dataset);
        this.ohlcRenderer = candlestickRenderer.getOhlcRenderer();
        this.areaRenderer = new FastXYAreaRenderer(dataset);
        this.lineRenderer = new FastXYLineRenderer(dataset);
        this.stepLineRenderer = new FastXYStepRenderer(dataset);
        this.rangeAxisId = dataset.getRangeAxisId();

        configureDefault();
    }

    public PriceInitialSettings(final PriceInitialSettings copyOf) {
        this.plotConfigurationHelper = copyOf.plotConfigurationHelper;
        this.candlestickRenderer = copyOf.candlestickRenderer;
        this.ohlcRenderer = copyOf.ohlcRenderer;
        this.areaRenderer = copyOf.areaRenderer;
        this.lineRenderer = copyOf.lineRenderer;
        this.stepLineRenderer = copyOf.stepLineRenderer;
        this.rangeAxisId = copyOf.getRangeAxisId();

        final XYItemRenderer priceRenderer = copyOf.getCurrentPriceRenderer();
        if (priceRenderer == null) {
            configureDefault();
        } else {
            priceRendererType = getPriceRendererType(priceRenderer);
            upColor = candlestickRenderer.getUpColor();
            downColor = candlestickRenderer.getDownColor();
            seriesColor = (Color) priceRenderer.getDefaultPaint();
            seriesStroke = priceRenderer.getDefaultStroke();
            priceLineVisible = candlestickRenderer.isPriceLineVisible();
            priceLabelVisible = candlestickRenderer.isPriceLabelVisible();
        }
    }

    private void configureDefault() {
        setUpColor(DEFAULT_UP_COLOR);
        setDownColor(DEFAULT_DOWN_COLOR);
        setSeriesColor(DEFAULT_SERIES_COLOR);
        setSeriesStroke(DEFAULT_SERIES_STROKE);
        setPriceLineVisible(DEFAULT_PRICE_LINE_VISIBLE);
        setPriceLabelVisible(DEFAULT_PRICE_LABEL_ENABLED);
    }

    public PriceRendererType getPriceRendererType() {
        return priceRendererType;
    }

    public void setPriceRendererType(final PriceRendererType priceRendererType) {
        final PriceRendererType usedPriceRendererType = priceRendererType.orDefault();
        if (usedPriceRendererType != this.priceRendererType) {
            updatePriceRendererType(usedPriceRendererType);
            plotConfigurationHelper.getChartPanel().update();
        }
        this.priceRendererType = usedPriceRendererType;
    }

    public void updatePriceRendererType(final PriceRendererType priceRendererType) {
        final IndexedDateTimeOHLCDataset masterDataset = plotConfigurationHelper.getChartPanel().getMasterDataset();
        final XYPlot masterDatasetPlot = masterDataset.getPlot();
        if (masterDatasetPlot != null) {
            final Integer masterDatasetIndex = XYPlots.getDatasetIndexForDataset(masterDatasetPlot, masterDataset,
                    true);
            final XYItemRenderer renderer = getCurrentPriceRenderer();
            final XYItemRenderer newRenderer = getPriceRenderer(priceRendererType);
            newRenderer.setDefaultPaint(renderer.getDefaultPaint());
            newRenderer.setDefaultStroke(renderer.getDefaultStroke());
            masterDatasetPlot.setRenderer(masterDatasetIndex, newRenderer);
        }
    }

    public IDatasetSourceXYItemRenderer getPriceRenderer() {
        return getPriceRenderer(priceRendererType);
    }

    public IDatasetSourceXYItemRenderer getPriceRenderer(final PriceRendererType priceRendererType) {
        switch (priceRendererType) {
        case None:
            return null;
        case Area:
            return areaRenderer;
        case Line:
            return lineRenderer;
        case Step:
            return stepLineRenderer;
        case OHLC:
            return ohlcRenderer;
        case Candlestick:
            return candlestickRenderer;
        default:
            throw UnknownArgumentException.newInstance(PriceRendererType.class, priceRendererType);
        }
    }

    public PriceRendererType getPriceRendererType(final XYItemRenderer renderer) {
        if (renderer == null) {
            return PriceRendererType.None;
        } else if (renderer == areaRenderer) {
            return PriceRendererType.Area;
        } else if (renderer == lineRenderer) {
            return PriceRendererType.Line;
        } else if (renderer == stepLineRenderer) {
            return PriceRendererType.Step;
        } else if (renderer == ohlcRenderer) {
            return PriceRendererType.OHLC;
        } else if (renderer == candlestickRenderer) {
            return PriceRendererType.Candlestick;
        } else {
            throw UnknownArgumentException.newInstance(XYItemRenderer.class, renderer);
        }
    }

    public Color getUpColor() {
        return upColor;
    }

    public void setUpColor(final Color upColor) {
        this.upColor = upColor;
        updateUpColor();
    }

    private void updateUpColor() {
        candlestickRenderer.setUpColor(upColor);
    }

    public Color getDownColor() {
        return downColor;
    }

    public void setDownColor(final Color downColor) {
        this.downColor = downColor;
        updateDownColor();
    }

    private void updateDownColor() {
        candlestickRenderer.setDownColor(downColor);
    }

    public Color getSeriesColor() {
        return seriesColor;
    }

    public void setSeriesColor(final Color seriesColor) {
        this.seriesColor = seriesColor;
        updateSeriesColor();
    }

    private void updateSeriesColor() {
        this.candlestickRenderer.setDefaultPaint(seriesColor);
        this.ohlcRenderer.setDefaultPaint(seriesColor);
        this.lineRenderer.setDefaultPaint(seriesColor);
        this.areaRenderer.setDefaultPaint(seriesColor);
        this.stepLineRenderer.setDefaultPaint(seriesColor);
    }

    public void setSeriesStroke(final LineStyleType lineStyleType, final LineWidthType lineWidthType) {
        this.seriesStroke = lineStyleType.getStroke(lineWidthType);
        updateSeriesStroke();
    }

    private void updateSeriesStroke() {
        this.candlestickRenderer.setDefaultStroke(seriesStroke);
        this.ohlcRenderer.setDefaultStroke(seriesStroke);
        this.lineRenderer.setDefaultStroke(seriesStroke);
        this.areaRenderer.setDefaultStroke(seriesStroke);
        this.stepLineRenderer.setDefaultStroke(seriesStroke);
    }

    public void setSeriesStroke(final Stroke seriesStroke) {
        Assertions.checkNotNull(LineStyleType.valueOf(seriesStroke));
        this.seriesStroke = seriesStroke;
    }

    public Stroke getSeriesStroke() {
        return seriesStroke;
    }

    public boolean isPriceLineVisible() {
        return priceLineVisible;
    }

    public void setPriceLineVisible(final boolean priceLineVisible) {
        this.priceLineVisible = priceLineVisible;
        updatePriceLineVisible();
    }

    private void updatePriceLineVisible() {
        this.candlestickRenderer.setPriceLineVisible(priceLineVisible);
        this.ohlcRenderer.setPriceLineVisible(priceLineVisible);
        this.lineRenderer.setPriceLineVisible(priceLineVisible);
        this.areaRenderer.setPriceLineVisible(priceLineVisible);
        this.stepLineRenderer.setPriceLineVisible(priceLineVisible);
    }

    public boolean isPriceLabelVisible() {
        return priceLabelVisible;
    }

    public void setPriceLabelVisible(final boolean priceLabelVisible) {
        this.priceLabelVisible = priceLabelVisible;
        updatePriceLabelVisible();
    }

    private void updatePriceLabelVisible() {
        candlestickRenderer.setPriceLabelVisible(priceLabelVisible);
        ohlcRenderer.setPriceLabelVisible(priceLabelVisible);
        areaRenderer.setPriceLabelVisible(priceLabelVisible);
        lineRenderer.setPriceLabelVisible(priceLabelVisible);
        stepLineRenderer.setPriceLabelVisible(priceLabelVisible);
    }

    public void reset() {
        updateUpColor();
        updateDownColor();
        updateSeriesColor();
        updateSeriesStroke();
        updatePriceLineVisible();
        updatePriceLabelVisible();
        updateRangeAxisId();
        final IndexedDateTimeOHLCDataset masterDataset = plotConfigurationHelper.getChartPanel().getMasterDataset();
        final XYPlot masterDatasetPlot = masterDataset.getPlot();
        if (masterDatasetPlot != null) {
            final Integer masterDatasetIndex = XYPlots.getDatasetIndexForDataset(masterDatasetPlot, masterDataset,
                    true);
            final IDatasetSourceXYItemRenderer renderer = getPriceRenderer(priceRendererType);
            masterDatasetPlot.setRenderer(masterDatasetIndex, renderer);
        }
    }

    public PriceRendererType getCurrentPriceRendererType() {
        return getPriceRendererType(getCurrentPriceRenderer());
    }

    public IDatasetSourceXYItemRenderer getCurrentPriceRenderer() {
        final IndexedDateTimeOHLCDataset masterDataset = plotConfigurationHelper.getChartPanel().getMasterDataset();
        final XYPlot masterDatasetPlot = masterDataset.getPlot();
        if (masterDatasetPlot == null) {
            return null;
        }
        final Integer masterDatasetIndex = XYPlots.getDatasetIndexForDataset(masterDatasetPlot, masterDataset, true);
        return (IDatasetSourceXYItemRenderer) masterDatasetPlot.getRenderer(masterDatasetIndex);
    }

    public String getRangeAxisId() {
        return rangeAxisId;
    }

    public void setRangeAxisId(final String rangeAxisId) {
        this.rangeAxisId = rangeAxisId;
        updateRangeAxisId();
    }

    private void updateRangeAxisId() {
        final IDatasetSourceXYItemRenderer renderer = getPriceRenderer(priceRendererType);
        final IPlotSourceDataset dataset = renderer.getDataset();
        dataset.setRangeAxisId(rangeAxisId);
        if (dataset.getPlot() != null) {
            XYPlots.updateRangeAxes(plotConfigurationHelper.getChartPanel().getTheme(), dataset.getPlot());
        }
    }

}
