package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.orders;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.ImageIcon;

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
import org.jfree.chart.util.XYCoordinateType;
import org.jfree.data.xy.XYDataset;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.LineStyleType;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.LineWidthType;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.PlotConfigurationHelper;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.PriceInitialSettings;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.icons.PlotIcons;
import de.invesdwin.context.client.swing.jfreechart.plot.XYPlots;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.XYNoteIconAnnotation;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.XYPriceLineAnnotation;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.INoteRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.IUpDownColorRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.Renderers;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.CustomProfitLossRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.ICustomRendererType;
import de.invesdwin.util.collections.iterable.ICloseableIterator;
import de.invesdwin.util.lang.string.Strings;
import de.invesdwin.util.math.Doubles;
import de.invesdwin.util.math.Integers;
import de.invesdwin.util.swing.HiDPI;
import de.invesdwin.util.swing.icon.AlphaImageIcon;
import de.invesdwin.util.swing.icon.ChangeColorImageFilter;

@NotThreadSafe
public class CustomOrderPlottingRenderer extends AbstractXYItemRenderer
        implements ICustomRendererType, IUpDownColorRenderer, INoteRenderer {

    public static final Font FONT = XYPriceLineAnnotation.FONT;
    public static final Color UP_COLOR = CustomProfitLossRenderer.UP_COLOR;
    public static final Color DOWN_COLOR = CustomProfitLossRenderer.DOWN_COLOR;
    public static final Color BOTH_COLOR = new Color(230, 145, 56);
    public static final Color TRIGGER_COLOR = new Color(128, 0, 128);
    public static final float NOTE_ALPHA = 0.2F;
    private static final ValueAxis ABSOLUTE_AXIS = XYPlots.DRAWING_ABSOLUTE_AXIS;
    private static final int NOTE_ICON_SIZE = HiDPI.scale(20);

    private static final LineStyleType LINE_STYLE_DEFAULT = LineStyleType.Solid;
    private static final LineStyleType LINE_STYLE_PENDING = LineStyleType.Dashed;

    private final OrderPlottingDataset dataset;
    private Color upColor;
    private Color downColor;
    private Color triggerColor;
    private ImageIcon noteUpIcon;
    private ImageIcon noteDownIcon;
    private ImageIcon noteBothIcon;

    private Map<Integer, XYNoteIconAnnotation> noteIndex_noteAnnotation = new TreeMap<>(
            Integers.COMPARATOR.asDescending().asNotNullSafe());

    public CustomOrderPlottingRenderer(final PlotConfigurationHelper plotConfigurationHelper,
            final OrderPlottingDataset dataset) {
        this(plotConfigurationHelper, dataset, null);
    }

    public CustomOrderPlottingRenderer(final PlotConfigurationHelper plotConfigurationHelper,
            final OrderPlottingDataset dataset, final LineWidthType lineWidth) {
        Renderers.disableAutoPopulate(this);

        this.dataset = dataset;
        final PriceInitialSettings config = plotConfigurationHelper.getPriceInitialSettings();
        setUpColor(UP_COLOR);
        setDownColor(DOWN_COLOR);
        setTriggerColor(TRIGGER_COLOR);
        setDefaultPaint(BOTH_COLOR);
        if (lineWidth == null) {
            setDefaultStroke(config.getSeriesStroke());
        } else {
            setDefaultStroke(lineWidth.getStroke(config.getSeriesStroke()));
        }
    }

    @Override
    public OrderPlottingDataset getDataset() {
        return dataset;
    }

    @Override
    public void setSeriesPaint(final int series, final Paint paint, final boolean notify) {
        throw new UnsupportedOperationException("use setDefaultPaint instead");
    }

    @Override
    public void setDefaultPaint(final Paint paint, final boolean notify) {
        super.setDefaultPaint(paint, notify);
        noteBothIcon = newNoteIcon((Color) paint);
    }

    private ImageIcon newNoteIcon(final Color color) {
        final ImageIcon icon = PlotIcons.MARKER.newIcon(NOTE_ICON_SIZE);
        final ImageIcon changeColor = ChangeColorImageFilter.apply(icon, color);
        return new AlphaImageIcon(changeColor, NOTE_ALPHA);
    }

    @Override
    public Paint getItemPaint(final int row, final int column) {
        if (dataset.isLastTradeProfit()) {
            return upColor;
        } else {
            return downColor;
        }
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
        return "Order Plotting";
    }

    @Override
    public void setUpColor(final Color upColor) {
        this.upColor = upColor;
        this.noteUpIcon = newNoteIcon(upColor);
        fireChangeEvent();
    }

    @Override
    public Color getUpColor() {
        return upColor;
    }

    @Override
    public void setDownColor(final Color downColor) {
        this.downColor = downColor;
        this.noteDownIcon = newNoteIcon(downColor);
        fireChangeEvent();
    }

    @Override
    public Color getDownColor() {
        return downColor;
    }

    public void setTriggerColor(final Color triggerColor) {
        this.triggerColor = triggerColor;
        fireChangeEvent();
    }

    @Override
    public boolean isSeriesRendererTypeConfigurable() {
        return false;
    }

    @Override
    public String getUpColorName() {
        return "Profit";
    }

    @Override
    public String getDownColorName() {
        return "Loss";
    }

    @Override
    public String getSeriesColorName() {
        return "Both";
    }

    @Override
    public void drawItem(final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea,
            final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis,
            final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState,
            final int pass) {
        final int lastItem = state.getLastItemIndex();
        if (item == lastItem) {
            final OrderPlottingDataset cDataset = (OrderPlottingDataset) dataset;
            final int firstItem = state.getFirstItemIndex();
            final int rendererIndex = getPlot().getIndexOf(this);
            final NumberAxis cRangeAxis = (NumberAxis) rangeAxis;
            final NumberFormat rangeAxisFormat = cRangeAxis.getNumberFormatOverride();
            final PlotOrientation orientation = plot.getOrientation();
            final RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
            final RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);
            final LineWidthType lineWidth = LineWidthType.valueOf(lookupSeriesStroke(series));
            final Map<Integer, List<OrderPlottingDataItem>> index_notes = drawLines(g2, dataArea, plot, domainAxis,
                    rangeAxis, lastItem, cDataset, firstItem, rendererIndex, rangeAxisFormat, domainEdge, rangeEdge,
                    lineWidth);
            drawNotes(g2, dataArea, info, plot, domainAxis, cDataset, rendererIndex, cRangeAxis, index_notes);
        }
    }

    private void drawNotes(final Graphics2D g2, final Rectangle2D dataArea, final PlotRenderingInfo info,
            final XYPlot plot, final ValueAxis domainAxis, final OrderPlottingDataset cDataset, final int rendererIndex,
            final NumberAxis cRangeAxis, final Map<Integer, List<OrderPlottingDataItem>> index_notes) {
        final Map<Integer, XYNoteIconAnnotation> newMap = new TreeMap<>(
                Integers.COMPARATOR.asDescending().asNotNullSafe());
        for (final Entry<Integer, List<OrderPlottingDataItem>> entry : index_notes.entrySet()) {
            final int noteIndex = entry.getKey();
            final List<OrderPlottingDataItem> notes = entry.getValue();
            OrderPlottingDataItem.NOTE_COMPARATOR.asAscending().asNotNullSafe().sort(notes);

            final StringBuilder note = new StringBuilder();
            int countProfit = 0;
            int countLoss = 0;
            for (int i = 0; i < notes.size(); i++) {
                final OrderPlottingDataItem noteItem = notes.get(i);
                final String noteStr = noteItem.getNote().get();
                if (noteItem.isProfit()) {
                    countProfit++;
                } else {
                    countLoss++;
                }
                if (i > 0) {
                    note.append("\n");
                }
                note.append(noteStr);
            }

            final ImageIcon icon;
            if (countProfit > 0 && countLoss == 0) {
                // green
                icon = noteUpIcon;
            } else if (countProfit == 0 && countLoss > 0) {
                // red
                icon = noteDownIcon;
            } else {
                // orange
                icon = noteBothIcon;
            }
            final double x = noteIndex;
            final double y = cDataset.getMasterDataset().getHighValue(0, noteIndex);
            XYNoteIconAnnotation noteAnnotation = noteIndex_noteAnnotation.get(noteIndex);
            if (noteAnnotation == null) {
                noteAnnotation = new XYNoteIconAnnotation(x, y, icon);
            } else {
                noteAnnotation.setX(x);
                noteAnnotation.setY(y);
                noteAnnotation.setIcon(icon);
            }
            noteAnnotation.setNote(note.toString());
            noteAnnotation.setCoordinateType(XYCoordinateType.DATA);
            noteAnnotation.draw(g2, plot, dataArea, domainAxis, cRangeAxis, rendererIndex, info);
            newMap.put(noteIndex, noteAnnotation);
        }
        noteIndex_noteAnnotation = newMap;
    }

    private Map<Integer, List<OrderPlottingDataItem>> drawLines(final Graphics2D g2, final Rectangle2D dataArea,
            final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final int lastItem,
            final OrderPlottingDataset cDataset, final int firstItem, final int rendererIndex,
            final NumberFormat rangeAxisFormat, final RectangleEdge domainEdge, final RectangleEdge rangeEdge,
            final LineWidthType lineWidth) {
        final Map<Integer, List<OrderPlottingDataItem>> index_notes = new LinkedHashMap<Integer, List<OrderPlottingDataItem>>();
        final ICloseableIterator<OrderPlottingDataItem> visibleItems = cDataset.getVisibleItems(firstItem, lastItem)
                .iterator();
        try {
            while (true) {
                final OrderPlottingDataItem next = visibleItems.next();
                drawLine(g2, dataArea, plot, domainAxis, rangeAxis, rendererIndex, rangeAxisFormat, domainEdge,
                        rangeEdge, lineWidth, next);
                final String note = next.getNote().get();
                if (Strings.isNotBlank(note)) {
                    final int noteIndex = next.getOpenTimeLoadedIndex();
                    List<OrderPlottingDataItem> notes = index_notes.get(noteIndex);
                    if (notes == null) {
                        notes = new ArrayList<OrderPlottingDataItem>();
                        index_notes.put(noteIndex, notes);
                    }
                    notes.add(next);
                }
            }
        } catch (final NoSuchElementException e) {
            //end reached
        }
        return index_notes;
    }

    private void drawLine(final Graphics2D g2, final Rectangle2D dataArea, final XYPlot plot,
            final ValueAxis domainAxis, final ValueAxis rangeAxis, final int rendererIndex,
            final NumberFormat rangeAxisFormat, final RectangleEdge domainEdge, final RectangleEdge rangeEdge,
            final LineWidthType lineWidth, final OrderPlottingDataItem item) {
        final double executionPrice = item.getExecutionPrice();
        if (Doubles.isNaN(executionPrice)) {
            return;
        }
        final double triggerPrice = item.getTriggerPrice();

        final Color color;
        if (item.isPending() && !Doubles.isNaN(triggerPrice)) {
            color = triggerColor;
        } else if (item.isProfit()) {
            color = upColor;
        } else {
            color = downColor;
        }

        final LineStyleType lineStyle;
        if (item.isPending()) {
            lineStyle = LINE_STYLE_PENDING;
        } else {
            lineStyle = LINE_STYLE_DEFAULT;
        }

        final Stroke stroke = lineStyle.getStroke(lineWidth);

        final boolean closed = item.isClosed();
        final double x1 = domainAxis.valueToJava2D(item.getOpenTimeLoadedIndex(), dataArea, domainEdge);
        final double x2;
        if (closed) {
            x2 = domainAxis.valueToJava2D(item.getCloseTimeLoadedIndex(), dataArea, domainEdge);
        } else {
            x2 = dataArea.getMaxX();
        }

        final double openPrice;
        final double closePrice;
        if (item.isPending() && !Doubles.isNaN(triggerPrice)) {
            /*
             * If there is a triggerPrice it has not been reached yet and therefore we only want to draw the
             * triggerPriceLine and not the execution-/close-PriceLine.
             */
            openPrice = triggerPrice;
            closePrice = triggerPrice;
        } else {
            openPrice = executionPrice;
            closePrice = item.getClosePrice();
        }
        final double y1 = rangeAxis.valueToJava2D(openPrice, dataArea, rangeEdge);
        final double y2 = rangeAxis.valueToJava2D(closePrice, dataArea, rangeEdge);
        final XYLineAnnotation lineAnnotation = new XYLineAnnotation(x1, y1, x2, y2, stroke, color);
        lineAnnotation.draw(g2, plot, dataArea, ABSOLUTE_AXIS, ABSOLUTE_AXIS, rendererIndex, null);

        if (!closed) {
            final String label = item.getLabel().get();
            if (Strings.isNotBlank(label)) {
                final XYTextAnnotation labelAnnotation = new XYTextAnnotation(label, x2 - 1D, y2 - 1D);
                labelAnnotation.setPaint(color);
                labelAnnotation.setFont(FONT);
                labelAnnotation.setTextAnchor(TextAnchor.BOTTOM_RIGHT);
                labelAnnotation.draw(g2, plot, dataArea, ABSOLUTE_AXIS, ABSOLUTE_AXIS, rendererIndex, null);
                final XYTextAnnotation priceAnnotation = new XYTextAnnotation(rangeAxisFormat.format(closePrice),
                        x2 - 1D, y2 + 1D);
                priceAnnotation.setPaint(color);
                priceAnnotation.setFont(FONT);
                priceAnnotation.setTextAnchor(TextAnchor.TOP_RIGHT);
                priceAnnotation.draw(g2, plot, dataArea, ABSOLUTE_AXIS, ABSOLUTE_AXIS, rendererIndex, null);
            }
        }
    }

    @Override
    public Collection<XYNoteIconAnnotation> getVisibleNoteIcons() {
        return noteIndex_noteAnnotation.values();
    }
}
