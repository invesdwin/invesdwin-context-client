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
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations.item.LineAnnotationPlottingDataItem;
import de.invesdwin.util.collections.iterable.ICloseableIterator;
import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.lang.Strings;

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
        final double x1 = domainAxis.valueToJava2D(next.getStartTimeLoadedIndex(), dataArea, domainEdge);
        final double x2 = domainAxis.valueToJava2D(next.getEndTimeLoadedIndex(), dataArea, domainEdge);

        final double openPrice = next.getStartPrice();
        final double closePrice = next.getEndPrice();

        final double y1 = rangeAxis.valueToJava2D(openPrice, dataArea, rangeEdge);
        final double y2 = rangeAxis.valueToJava2D(closePrice, dataArea, rangeEdge);
        final XYLineAnnotation lineAnnotation = new XYLineAnnotation(x1, y1, x2, y2, stroke, color);
        lineAnnotation.draw(g2, plot, dataArea, ABSOLUTE_AXIS, ABSOLUTE_AXIS, rendererIndex, null);

        final String label = next.getLabel();
        if (Strings.isNotBlank(label)) {
            final XYTextAnnotation priceAnnotation = new XYTextAnnotation(label, x2 - 1D, y2 + 1D);
            priceAnnotation.setPaint(color);
            priceAnnotation.setFont(FONT);
            priceAnnotation.setTextAnchor(TextAnchor.TOP_RIGHT);
            priceAnnotation.draw(g2, plot, dataArea, ABSOLUTE_AXIS, ABSOLUTE_AXIS, rendererIndex, info);
        }
    }

}
