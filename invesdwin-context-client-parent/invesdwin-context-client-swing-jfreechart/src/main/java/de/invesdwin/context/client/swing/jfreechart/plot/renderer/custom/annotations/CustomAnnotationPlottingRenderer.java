package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.NoSuchElementException;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.xy.XYDataset;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.PlotConfigurationHelper;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.PriceInitialSettings;
import de.invesdwin.context.client.swing.jfreechart.plot.XYPlots;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.XYPriceLineAnnotation;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.Renderers;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.ICustomRendererType;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.item.AAnnotationPlottingDataItem;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.item.LabelAnnotationPlottingDataItem;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.item.LineAnnotationPlottingDataItem;
import de.invesdwin.util.collections.iterable.ICloseableIterator;
import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.lang.Strings;
import de.invesdwin.util.math.Doubles;

@NotThreadSafe
public class CustomAnnotationPlottingRenderer extends AbstractXYItemRenderer implements ICustomRendererType {

    public static final Font FONT = XYPriceLineAnnotation.FONT;
    private static final ValueAxis ABSOLUTE_AXIS = XYPlots.DRAWING_ABSOLUTE_AXIS;

    private final AnnotationPlottingDataset dataset;

    public CustomAnnotationPlottingRenderer(final PlotConfigurationHelper plotConfigurationHelper,
            final AnnotationPlottingDataset dataset) {
        Renderers.disableAutoPopulate(this);

        this.dataset = dataset;
        final PriceInitialSettings config = plotConfigurationHelper.getPriceInitialSettings();
        setDefaultStroke(config.getSeriesStroke());
        setDefaultPaint(config.getSeriesColor());
    }

    @Override
    public AnnotationPlottingDataset getDataset() {
        return dataset;
    }

    @Override
    public void setSeriesPaint(final int series, final Paint paint, final boolean notify) {
        throw new UnsupportedOperationException("use setDefaultPaint instead");
    }

    @Override
    public boolean isLineStyleConfigurable() {
        return false;
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
        return dataset.getSeriesTitle();
    }

    @Override
    public boolean isSeriesRendererTypeConfigurable() {
        return false;
    }

    //CHECKSTYLE:OFF
    @Override
    public void drawItem(final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea,
            final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis,
            final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState,
            final int pass) {
        //CHECKSTYLE:ON
        final int lastItem = state.getLastItemIndex();
        if (item == lastItem) {
            final AnnotationPlottingDataset cDataset = (AnnotationPlottingDataset) dataset;
            final int firstItem = state.getFirstItemIndex();
            final int rendererIndex = getPlot().getIndexOf(this);
            final NumberAxis cRangeAxis = (NumberAxis) rangeAxis;
            final NumberFormat rangeAxisFormat = cRangeAxis.getNumberFormatOverride();
            final PlotOrientation orientation = plot.getOrientation();
            final RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
            final RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);
            drawLines(g2, dataArea, info, plot, domainAxis, rangeAxis, lastItem, cDataset, series, firstItem,
                    rendererIndex, rangeAxisFormat, domainEdge, rangeEdge);
        }
    }

    //CHECKSTYLE:OFF
    private void drawLines(final Graphics2D g2, final Rectangle2D dataArea, final PlotRenderingInfo info,
            final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final int lastItem,
            final AnnotationPlottingDataset cDataset, final int series, final int firstItem, final int rendererIndex,
            final NumberFormat rangeAxisFormat, final RectangleEdge domainEdge, final RectangleEdge rangeEdge) {
        //CHECKSTYLE:ON
        final Stroke stroke = lookupSeriesStroke(series);
        final Paint color = lookupSeriesPaint(series);

        final ICloseableIterator<AAnnotationPlottingDataItem> visibleItems = cDataset
                .getVisibleItems(firstItem, lastItem)
                .iterator();
        try {
            while (true) {
                final AAnnotationPlottingDataItem next = visibleItems.next();
                if (next instanceof LineAnnotationPlottingDataItem) {
                    final LineAnnotationPlottingDataItem cNext = (LineAnnotationPlottingDataItem) next;
                    drawLine(g2, dataArea, info, plot, domainAxis, rangeAxis, rendererIndex, rangeAxisFormat,
                            domainEdge, rangeEdge, stroke, color, cNext);
                } else if (next instanceof LabelAnnotationPlottingDataItem) {
                    final LabelAnnotationPlottingDataItem cNext = (LabelAnnotationPlottingDataItem) next;
                    drawLabel(g2, dataArea, info, plot, domainAxis, rangeAxis, rendererIndex, rangeAxisFormat,
                            domainEdge, rangeEdge, stroke, color, cNext);
                } else {
                    throw UnknownArgumentException.newInstance(Class.class, next.getClass());
                }
            }
        } catch (final NoSuchElementException e) {
            //end reached
        }
    }

    //CHECKSTYLE:OFF
    private void drawLine(final Graphics2D g2, final Rectangle2D dataArea, final PlotRenderingInfo info,
            final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final int rendererIndex,
            final NumberFormat rangeAxisFormat, final RectangleEdge domainEdge, final RectangleEdge rangeEdge,
            final Stroke stroke, final Paint color, final LineAnnotationPlottingDataItem next) {
        //CHECKSTYLE:ON
        final int startTimeLoadedIndex = next.getStartTimeLoadedIndex();
        final double x1 = domainAxis.valueToJava2D(startTimeLoadedIndex, dataArea, domainEdge);
        final int endTimeLoadedIndex = next.getEndTimeLoadedIndex();
        final double x2 = domainAxis.valueToJava2D(endTimeLoadedIndex, dataArea, domainEdge);

        final double openPrice = next.getStartPriceLoaded();
        final double closePrice = next.getEndPriceLoaded();

        final double y1 = rangeAxis.valueToJava2D(openPrice, dataArea, rangeEdge);
        final double y2 = rangeAxis.valueToJava2D(closePrice, dataArea, rangeEdge);
        final XYLineAnnotation lineAnnotation = new XYLineAnnotation(x1, y1, x2, y2, stroke, color);
        lineAnnotation.draw(g2, plot, dataArea, ABSOLUTE_AXIS, ABSOLUTE_AXIS, rendererIndex, null);

        final String label = next.getLabel();
        if (Strings.isNotBlank(label)) {
            final TextAnchor textAnchor = next.getLabelTextAnchor();
            final double labelX = getLineLabelX(next, x1, x2);
            final double labelY = getLineLabelY(next, y1, y2);
            final XYTextAnnotation priceAnnotation = new XYTextAnnotation(label, labelX, labelY);
            priceAnnotation.setPaint(color);
            priceAnnotation.setFont(FONT);
            priceAnnotation.setTextAnchor(textAnchor);
            priceAnnotation.draw(g2, plot, dataArea, ABSOLUTE_AXIS, ABSOLUTE_AXIS, rendererIndex, info);
        }
    }

    private double getLineLabelY(final LineAnnotationPlottingDataItem next, final double y1, final double y2) {
        switch (next.getLabelVerticalAlign()) {
        case Top:
            switch (next.getLabelHorizontalAlign()) {
            case Left:
                return y1 - 1D;
            case Center:
                return Doubles.max(y1, y2);
            case Right:
                return y2 - 1D;
            default:
                throw UnknownArgumentException.newInstance(LabelHorizontalAlignType.class,
                        next.getLabelHorizontalAlign());
            }
        case Center:
            return (y1 + y2) / 2;
        case Bottom:
            switch (next.getLabelHorizontalAlign()) {
            case Left:
                return y1 + 1D;
            case Center:
                return Doubles.min(y1, y2);
            case Right:
                return y2 + 1D;
            default:
                throw UnknownArgumentException.newInstance(LabelHorizontalAlignType.class,
                        next.getLabelHorizontalAlign());
            }
        default:
            throw UnknownArgumentException.newInstance(LabelVerticalAlignType.class, next.getLabelVerticalAlign());
        }
    }

    private double getLineLabelX(final LineAnnotationPlottingDataItem next, final double x1, final double x2) {
        switch (next.getLabelHorizontalAlign()) {
        case Left:
            return x1 + 1D;
        case Center:
            return (x1 + x2) / 2;
        case Right:
            return x2 - 1D;
        default:
            throw UnknownArgumentException.newInstance(LabelHorizontalAlignType.class, next.getLabelHorizontalAlign());
        }
    }

    //CHECKSTYLE:OFF
    private void drawLabel(final Graphics2D g2, final Rectangle2D dataArea, final PlotRenderingInfo info,
            final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final int rendererIndex,
            final NumberFormat rangeAxisFormat, final RectangleEdge domainEdge, final RectangleEdge rangeEdge,
            final Stroke stroke, final Paint color, final LabelAnnotationPlottingDataItem next) {
        //CHECKSTYLE:ON

        final double x = domainAxis.valueToJava2D(next.getStartTimeLoadedIndex(), dataArea, domainEdge);
        final double price = next.getPrice();
        final double y = rangeAxis.valueToJava2D(price, dataArea, rangeEdge);
        final String label = next.getLabel();
        final TextAnchor textAnchor = next.getLabelTextAnchor();

        final XYTextAnnotation priceAnnotation = new XYTextAnnotation(label, x, y);
        priceAnnotation.setPaint(color);
        priceAnnotation.setFont(FONT);
        priceAnnotation.setTextAnchor(textAnchor);
        priceAnnotation.draw(g2, plot, dataArea, ABSOLUTE_AXIS, ABSOLUTE_AXIS, rendererIndex, info);
    }

}
