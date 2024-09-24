package de.invesdwin.context.client.swing.jfreechart.plot.axis;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.basis.CustomCombinedDomainXYPlot;
import de.invesdwin.context.client.swing.jfreechart.plot.CustomXYPlot;
import de.invesdwin.context.client.swing.jfreechart.plot.XYPlots;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.IPriceLineRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IDrawIncompleteBar;
import de.invesdwin.util.lang.color.Colors;
import de.invesdwin.util.math.Doubles;

@NotThreadSafe
public class CustomNumberAxis extends NumberAxis {

    public static final Font FONT = XYPlots.DEFAULT_FONT;
    /**
     * 3% minimum edge distance seems to work fine to avoid overlapping vertical tick labels
     */
    private static final double MIN_TICK_LABEL_VERTICAL_EDGE_DISTANCE_MULTIPLIER = 0.03;
    private static final int BACKGROUND_RECTANGLE_ADDED_HEIGHT = 3;
    private static final int BACKGROUND_RECTANGLE_OFFSET = 1;

    private final NumberFormat limitedNumberFormatOverride = new NumberFormat() {

        private boolean shouldFormatLabel(final double number) {
            final Range range = getRange();
            final double length = range.getLength();
            final double lowerBound = range.getLowerBound();
            final double upperBound = range.getUpperBound();

            final Plot plot = getPlot();
            if (plot instanceof CustomXYPlot) {
                final CustomXYPlot cPlot = (CustomXYPlot) plot;
                final CustomCombinedDomainXYPlot combinedPlot = cPlot.getCombinedPlot();
                if (combinedPlot == null) {
                    return false;
                }
                if (combinedPlot.getGap() > 0D) {
                    return true;
                }

                final double reducedLength = length * MIN_TICK_LABEL_VERTICAL_EDGE_DISTANCE_MULTIPLIER;
                final double reducedLowerBound = lowerBound + reducedLength;
                final double reducedUpperBound = upperBound - reducedLength;
                if (!combinedPlot.isSubplotAtTopEdge(cPlot) && number > reducedUpperBound) {
                    return false;
                }
                if (!combinedPlot.isSubplotAtBottomEdge(cPlot) && number < reducedLowerBound) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public StringBuffer format(final double number, final StringBuffer toAppendTo, final FieldPosition pos) {
            if (shouldFormatLabel(number)) {
                final NumberFormat formatter = superGetNumberFormatOverride();
                if (formatter != null) {
                    toAppendTo.append(formatter.format(number));
                } else {
                    toAppendTo.append(getTickUnit().valueToString(number));
                }
            }
            return toAppendTo;
        }

        @Override
        public StringBuffer format(final long number, final StringBuffer toAppendTo, final FieldPosition pos) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Number parse(final String source, final ParsePosition parsePosition) {
            throw new UnsupportedOperationException();
        }

    };

    private boolean limitNumberFormatOverride = false;

    @Override
    public NumberFormat getNumberFormatOverride() {
        if (limitNumberFormatOverride) {
            return limitedNumberFormatOverride;
        } else {
            return superGetNumberFormatOverride();
        }
    }

    private NumberFormat superGetNumberFormatOverride() {
        return super.getNumberFormatOverride();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List refreshTicksVertical(final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge) {
        limitNumberFormatOverride = true;
        try {
            return super.refreshTicksVertical(g2, dataArea, edge);
        } finally {
            limitNumberFormatOverride = false;
        }
    }

    @Override
    public AxisState draw(final Graphics2D g2, final double cursor, final Rectangle2D plotArea,
            final Rectangle2D dataArea, final RectangleEdge edge, final PlotRenderingInfo plotState) {
        final AxisState axisState = super.draw(g2, cursor, plotArea, dataArea, edge, plotState);
        drawPriceLineLabels(g2, cursor, dataArea, edge);
        return axisState;
    }

    /**
     * Draws Price LineLabels on the rangeAxises. Rectangle-Background in the color of the series (configurable).
     */
    protected void drawPriceLineLabels(final Graphics2D g2, final double cursor, final Rectangle2D dataArea,
            final RectangleEdge edge) {
        if (!isVisible()) {
            return;
        }

        final XYPlot plot = (XYPlot) getPlot();
        final InteractiveChartPanel chartPanel = XYPlots.getChartPanel(plot);
        final Color panelBackgroundColor = (Color) chartPanel.getChart().getBackgroundPaint();

        for (int i = 0; i < plot.getRendererCount(); i++) {
            final XYItemRenderer renderer = plot.getRenderer(i);
            /*
             * We check for isPriceLineVisible as well because the isPriceLabelVisible will be inconsistent here if we
             * unchecked the PriceLine-Checkbox while the PriceLabel-Checkbox was still checked.
             */
            if (renderer instanceof IPriceLineRenderer && ((IPriceLineRenderer) renderer).isPriceLineVisible()
                    && ((IPriceLineRenderer) renderer).isPriceLabelVisible()) {
                final ValueAxis rangeAxis = XYPlots.getRangeAxisForDatasetNullable(plot, i);
                if (this == rangeAxis) {
                    final XYDataset dataset = plot.getDataset(i);
                    final int lastItem = dataset.getItemCount(0) - 1;
                    final double lastPrice = IDrawIncompleteBar.getLastYValue(dataset);
                    if (Doubles.isNaN(lastPrice)) {
                        return;
                    }

                    //RectangleCoords
                    final NumberTick tick = new NumberTick(lastPrice, String.valueOf(lastPrice), TextAnchor.CENTER_LEFT,
                            TextAnchor.CENTER_LEFT, 0.0);
                    final float[] anchorPoint = calculateAnchorPoint(tick, cursor, dataArea, edge);

                    //Draw the background
                    final Color seriesColor = (Color) renderer.getItemPaint(0, lastItem);
                    final int height = g2.getFontMetrics().getHeight() + BACKGROUND_RECTANGLE_ADDED_HEIGHT;
                    final int width;
                    final Rectangle2D chartArea = chartPanel.getChartPanel().getChartRenderingInfo().getChartArea();
                    //Different TextAnchor depending on the edge necessary: see NumberAxis.refreshTicksVertical()
                    final TextAnchor textAnchor;
                    final int y = (int) anchorPoint[1] - (height / 2) + BACKGROUND_RECTANGLE_OFFSET;
                    //make overpaint 1 pixel smaller so that tick labels are cut off smoother
                    final int yOverpaint = y + BACKGROUND_RECTANGLE_OFFSET;
                    final int heightOverpaint = height - BACKGROUND_RECTANGLE_OFFSET - BACKGROUND_RECTANGLE_OFFSET;
                    if (RectangleEdge.LEFT.equals(edge)) {
                        width = (int) (cursor - chartArea.getX());
                        final int x = (int) chartArea.getX();
                        //Paint a Rectangle to overpaint the axis-tick-label
                        g2.setColor(panelBackgroundColor);
                        g2.fillRect(x, yOverpaint, width, heightOverpaint);
                        //Paint the background rectangle in the seriesColor
                        g2.setColor(seriesColor);
                        g2.fillRect(x, y, width, height);
                        textAnchor = TextAnchor.CENTER_RIGHT;
                    } else if (RectangleEdge.RIGHT.equals(edge)) {
                        final int x = (int) cursor;
                        width = (int) (chartArea.getWidth() - cursor);
                        //Paint a Rectangle to overpaint the axis-tick-label
                        g2.setColor(panelBackgroundColor);
                        g2.fillRect(x, yOverpaint, width, heightOverpaint);
                        //Paint the background rectangle in the seriesColor
                        g2.setColor(seriesColor);
                        g2.fillRect(x, y, width, height);
                        textAnchor = TextAnchor.CENTER_LEFT;
                    } else {
                        throw new UnsupportedOperationException("Axis-PriceLabelRendering not supported for: " + edge);
                    }

                    //Draw the text
                    g2.setColor(Colors.getContrastColor(seriesColor));
                    final NumberFormat rangeAxisFormat = getNumberFormatOverride();
                    final String formattedText = rangeAxisFormat.format(lastPrice);
                    TextUtils.drawAlignedString(formattedText, g2, anchorPoint[0], anchorPoint[1], textAnchor);
                }
            }
        }
    }
}
