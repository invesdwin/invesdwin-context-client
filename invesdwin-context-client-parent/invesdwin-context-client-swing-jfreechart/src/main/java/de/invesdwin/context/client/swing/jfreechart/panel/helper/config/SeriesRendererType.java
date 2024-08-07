package de.invesdwin.context.client.swing.jfreechart.panel.helper.config;

import java.awt.Color;

import javax.annotation.concurrent.Immutable;

import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYItemRenderer;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.legend.HighlightedLegendInfo;
import de.invesdwin.context.client.swing.jfreechart.plot.XYPlots;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.DisabledXYItemRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.FastXYAreaRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.FastXYBarRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.FastXYLineRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.FastXYShapeRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.FastXYStepRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.IDatasetSourceXYItemRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.ICustomRendererType;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.shape.CircleSeriesShapeFactory;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.shape.CrossSeriesShapeFactory;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.shape.ISeriesShapeFactory;
import de.invesdwin.util.error.UnknownArgumentException;

@Immutable
public enum SeriesRendererType implements IRendererType {
    Line {
        @Override
        public IDatasetSourceXYItemRenderer newRenderer(final IPlotSourceDataset dataset,
                final LineStyleType lineStyleType, final LineWidthType lineWidthType, final Color color,
                final boolean priceLineVisible, final boolean priceLabelVisible) {
            final FastXYLineRenderer renderer = new FastXYLineRenderer(dataset);
            renderer.setDefaultPaint(color);
            renderer.setDefaultFillPaint(color);
            renderer.setDefaultStroke(lineStyleType.getStroke(lineWidthType));
            renderer.setPriceLineVisible(priceLineVisible);
            renderer.setPriceLabelVisible(priceLabelVisible);
            return renderer;
        }

        @Override
        public boolean isLineStyleConfigurable() {
            return true;
        }

        @Override
        public boolean isLineWidthConfigurable() {
            return true;
        }
    },
    Step {
        @Override
        public IDatasetSourceXYItemRenderer newRenderer(final IPlotSourceDataset dataset,
                final LineStyleType lineStyleType, final LineWidthType lineWidthType, final Color color,
                final boolean priceLineVisible, final boolean priceLabelVisible) {
            final FastXYStepRenderer renderer = new FastXYStepRenderer(dataset);
            renderer.setDefaultPaint(color);
            renderer.setDefaultFillPaint(color);
            renderer.setDefaultStroke(lineStyleType.getStroke(lineWidthType));
            renderer.setPriceLineVisible(priceLineVisible);
            renderer.setPriceLabelVisible(priceLabelVisible);
            return renderer;
        }

        @Override
        public boolean isLineStyleConfigurable() {
            return true;
        }

        @Override
        public boolean isLineWidthConfigurable() {
            return true;
        }
    },
    Area {
        @Override
        public IDatasetSourceXYItemRenderer newRenderer(final IPlotSourceDataset dataset,
                final LineStyleType lineStyleType, final LineWidthType lineWidthType, final Color color,
                final boolean priceLineVisible, final boolean priceLabelVisible) {
            final FastXYAreaRenderer renderer = new FastXYAreaRenderer(dataset);
            renderer.setDefaultPaint(color);
            renderer.setDefaultStroke(lineStyleType.getStroke(lineWidthType));
            renderer.setPriceLineVisible(priceLineVisible);
            renderer.setPriceLabelVisible(priceLabelVisible);
            return renderer;
        }

        @Override
        public boolean isLineStyleConfigurable() {
            return true;
        }

        @Override
        public boolean isLineWidthConfigurable() {
            return true;
        }
    },
    Column {
        @Override
        public IDatasetSourceXYItemRenderer newRenderer(final IPlotSourceDataset dataset,
                final LineStyleType lineStyleType, final LineWidthType lineWidthType, final Color color,
                final boolean priceLineVisible, final boolean priceLabelVisible) {
            final FastXYBarRenderer renderer = new FastXYBarRenderer(dataset);
            renderer.setBarPainter(new StandardXYBarPainter());
            renderer.setDefaultPaint(color);
            renderer.setDefaultFillPaint(color);
            renderer.setDefaultStroke(lineStyleType.getStroke(lineWidthType));
            renderer.setDrawBarOutline(false);
            renderer.setShadowVisible(false);
            renderer.setPriceLineVisible(priceLineVisible);
            renderer.setPriceLabelVisible(priceLabelVisible);
            return renderer;
        }

        @Override
        public boolean isLineStyleConfigurable() {
            return false;
        }

        @Override
        public boolean isLineWidthConfigurable() {
            return false;
        }
    },
    Histogram {
        @Override
        public IDatasetSourceXYItemRenderer newRenderer(final IPlotSourceDataset dataset,
                final LineStyleType lineStyleType, final LineWidthType lineWidthType, final Color color,
                final boolean priceLineVisible, final boolean priceLabelVisible) {
            final FastXYBarRenderer renderer = new FastXYBarRenderer(dataset, HISTOGRAM_MARGIN);
            renderer.setBarPainter(new StandardXYBarPainter());
            renderer.setDefaultPaint(color);
            renderer.setDefaultFillPaint(color);
            renderer.setDefaultStroke(lineStyleType.getStroke(lineWidthType));
            renderer.setDrawBarOutline(false);
            renderer.setShadowVisible(false);
            renderer.setPriceLineVisible(priceLineVisible);
            renderer.setPriceLabelVisible(priceLabelVisible);
            return renderer;
        }

        @Override
        public boolean isLineStyleConfigurable() {
            return false;
        }

        @Override
        public boolean isLineWidthConfigurable() {
            return false;
        }
    },
    Circle {
        @Override
        public IDatasetSourceXYItemRenderer newRenderer(final IPlotSourceDataset dataset,
                final LineStyleType lineStyleType, final LineWidthType lineWidthType, final Color color,
                final boolean priceLineVisible, final boolean priceLabelVisible) {
            final FastXYShapeRenderer renderer = new FastXYShapeRenderer(dataset, CircleSeriesShapeFactory.INSTANCE);
            renderer.setDefaultPaint(color);
            renderer.setDefaultFillPaint(color);
            renderer.setDefaultStroke(lineStyleType.getStroke(lineWidthType));
            renderer.setPriceLineVisible(priceLineVisible);
            renderer.setPriceLabelVisible(priceLabelVisible);
            return renderer;
        }

        @Override
        public boolean isLineStyleConfigurable() {
            return false;
        }

        @Override
        public boolean isLineWidthConfigurable() {
            return true;
        }
    },
    Cross {
        @Override
        public IDatasetSourceXYItemRenderer newRenderer(final IPlotSourceDataset dataset,
                final LineStyleType lineStyleType, final LineWidthType lineWidthType, final Color color,
                final boolean priceLineVisible, final boolean priceLabelVisible) {
            final FastXYShapeRenderer renderer = new FastXYShapeRenderer(dataset, CrossSeriesShapeFactory.INSTANCE);
            renderer.setDefaultPaint(color);
            renderer.setDefaultFillPaint(color);
            renderer.setDefaultStroke(lineStyleType.getStroke(lineWidthType));
            renderer.setPriceLineVisible(priceLineVisible);
            renderer.setPriceLabelVisible(priceLabelVisible);
            return renderer;
        }

        @Override
        public boolean isLineStyleConfigurable() {
            return false;
        }

        @Override
        public boolean isLineWidthConfigurable() {
            return true;
        }
    },
    Custom {
        @Override
        public boolean isLineStyleConfigurable() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isLineWidthConfigurable() {
            throw new UnsupportedOperationException();
        }

        @Override
        public IDatasetSourceXYItemRenderer newRenderer(final IPlotSourceDataset dataset,
                final LineStyleType lineStyleType, final LineWidthType lineWidthType, final Color color,
                final boolean priceLineVisible, final boolean priceLabelVisible) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isSeriesColorConfigurable() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDownColorConfigurable() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isUpColorConfigurable() {
            throw new UnsupportedOperationException();
        }
    };

    private static final double HISTOGRAM_MARGIN = 0.80D;

    public abstract IDatasetSourceXYItemRenderer newRenderer(IPlotSourceDataset dataset, LineStyleType lineStyleType,
            LineWidthType lineWidthType, Color color, boolean priceLineVisible, boolean priceLabelVisible);

    public static SeriesRendererType valueOf(final XYItemRenderer renderer) {
        final XYItemRenderer unwrapped = DisabledXYItemRenderer.maybeUnwrap(renderer);
        if (unwrapped instanceof ICustomRendererType) {
            return SeriesRendererType.Custom;
        } else if (unwrapped instanceof FastXYLineRenderer) {
            return SeriesRendererType.Line;
        } else if (unwrapped instanceof FastXYStepRenderer) {
            return SeriesRendererType.Step;
        } else if (unwrapped instanceof FastXYAreaRenderer) {
            return SeriesRendererType.Area;
        } else if (unwrapped instanceof FastXYBarRenderer) {
            final FastXYBarRenderer cRenderer = (FastXYBarRenderer) unwrapped;
            if (cRenderer.getMargin() == HISTOGRAM_MARGIN) {
                return SeriesRendererType.Histogram;
            } else {
                return SeriesRendererType.Column;
            }
        } else if (unwrapped instanceof FastXYShapeRenderer) {
            final FastXYShapeRenderer cUnwrapped = (FastXYShapeRenderer) unwrapped;
            final ISeriesShapeFactory seriesShapeFactory = cUnwrapped.getSeriesShapeFactory();
            if (seriesShapeFactory == CrossSeriesShapeFactory.INSTANCE) {
                return SeriesRendererType.Cross;
            } else if (seriesShapeFactory == CircleSeriesShapeFactory.INSTANCE) {
                return SeriesRendererType.Circle;
            } else {
                throw UnknownArgumentException.newInstance(Class.class, seriesShapeFactory.getClass());
            }
        }
        throw UnknownArgumentException.newInstance(Class.class, unwrapped.getClass());
    }

    @Override
    public boolean isUpColorConfigurable() {
        return false;
    }

    @Override
    public boolean isDownColorConfigurable() {
        return false;
    }

    @Override
    public boolean isSeriesColorConfigurable() {
        return true;
    }

    @Override
    public void reset(final HighlightedLegendInfo highlighted, final SeriesInitialSettings initialSettings) {
        final IPlotSourceDataset dataset = highlighted.getDataset();
        final IDatasetSourceXYItemRenderer newRenderer = newRenderer(dataset, initialSettings.getLineStyleType(),
                initialSettings.getLineWidthType(), initialSettings.getSeriesColor(),
                initialSettings.isPriceLineVisible(), initialSettings.isPriceLabelVisible());
        highlighted.setRenderer(newRenderer);
        dataset.setRangeAxisId(initialSettings.getRangeAxisId());
        XYPlots.updateRangeAxes(highlighted.getChartPanel().getTheme(), dataset.getPlot());
    }

    @Override
    public SeriesRendererType getSeriesRendererType() {
        return this;
    }

    @Override
    public boolean isSeriesRendererTypeConfigurable() {
        return true;
    }

    @Override
    public boolean isPriceLineConfigurable() {
        return true;
    }

}
