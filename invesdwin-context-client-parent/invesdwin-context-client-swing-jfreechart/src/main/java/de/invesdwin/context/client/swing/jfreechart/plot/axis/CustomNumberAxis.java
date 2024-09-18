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
import de.invesdwin.util.lang.color.Colors;

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
        drawPriceLineLabels(g2, cursor, dataArea, edge, plotState);
        return axisState;
    }

    protected void drawPriceLineLabels(final Graphics2D g2, final double cursor, final Rectangle2D dataArea,
            final RectangleEdge edge, final PlotRenderingInfo plotState) {
        final XYPlot plot = (XYPlot) getPlot();
        final InteractiveChartPanel chartPanel = XYPlots.getChartPanel(plot);
        final Color panelBackgroundColor = (Color) chartPanel.getChart().getBackgroundPaint();

        for (int i = 0; i < plot.getRendererCount(); i++) {
            final XYItemRenderer renderer = plot.getRenderer(i);
            if (renderer instanceof IPriceLineRenderer && ((IPriceLineRenderer) renderer).isPriceLabelVisible()) {
                final ValueAxis rangeAxis = XYPlots.getRangeAxisForDatasetNullable(plot, i);
                if (this == rangeAxis) {
                    final XYDataset dataset = plot.getDataset(i);
                    final int lastItem = dataset.getItemCount(0) - 1;
                    final double lastPrice = (double) dataset.getY(0, lastItem);

                    //RectangleCoords
                    final NumberTick tick = new NumberTick(lastPrice, String.valueOf(lastPrice), TextAnchor.CENTER_LEFT,
                            TextAnchor.CENTER_LEFT, 0.0);
                    final float[] anchorPoint = calculateAnchorPoint(tick, cursor, dataArea, edge);

                    //Draw the background
                    final Color seriesColor = (Color) renderer.getItemPaint(0, lastItem);
                    final int height = g2.getFontMetrics().getHeight() + BACKGROUND_RECTANGLE_ADDED_HEIGHT;
                    final int width;
                    //Different TextAnchor depending on the edge necessary: see NumberAxis.refreshTicksVertical()
                    final TextAnchor textAnchor;
                    final int y = (int) anchorPoint[1] - (height / 2) + BACKGROUND_RECTANGLE_OFFSET;
                    //make overpaint 1 pixel smaller so that tick labels are cut off smoother
                    final int yOverpaint = y + BACKGROUND_RECTANGLE_OFFSET;
                    final int heightOverpaint = height - BACKGROUND_RECTANGLE_OFFSET - BACKGROUND_RECTANGLE_OFFSET;
                    if (RectangleEdge.LEFT.equals(edge)) {
                        width = (int) (cursor - plotState.getOwner().getChartArea().getX());
                        final int x = (int) plotState.getOwner().getChartArea().getX();
                        //Paint a Rectangle to overpaint the axis-tick-label
                        g2.setColor(panelBackgroundColor);
                        g2.fillRect(x, yOverpaint, width, heightOverpaint);
                        //Paint the background rectangle in the seriesColor
                        g2.setColor(seriesColor);
                        g2.fillRect(x, y, width, height);
                        textAnchor = TextAnchor.CENTER_RIGHT;
                    } else if (RectangleEdge.RIGHT.equals(edge)) {
                        final int x = (int) cursor;
                        width = (int) (plotState.getOwner().getChartArea().getWidth() - cursor);
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

    //    //CHECKSTYLE:OFF
    //    @Override
    //    protected AxisState drawTickMarksAndLabels(final Graphics2D g2, final double cursor, final Rectangle2D plotArea,
    //            final Rectangle2D dataArea, final RectangleEdge edge) {
    //        //CHECKSTYLE:ON
    //
    //        final AxisState state = new AxisState(cursor);
    //        if (isAxisLineVisible()) {
    //            drawAxisLine(g2, cursor, dataArea, edge);
    //        }
    //        final List ticks = refreshTicks(g2, state, dataArea, edge);
    //        state.setTicks(ticks);
    //        g2.setFont(getTickLabelFont());
    //        final Object saved = g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
    //        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
    //        final Iterator iterator = ticks.iterator();
    //        while (iterator.hasNext()) {
    //            final ValueTick tick = (ValueTick) iterator.next();
    //            if (isTickLabelsVisible()) {
    //                if (tick instanceof LogTick) {
    //                    final LogTick lt = (LogTick) tick;
    //                    if (lt.getAttributedLabel() == null) {
    //                        continue;
    //                    }
    //                    final float[] anchorPoint = calculateAnchorPoint(tick, cursor, dataArea, edge);
    //                    final Color tickLabelPaint = getTickLabelPaint(g2, dataArea, edge, tick);
    //                    g2.setPaint(tickLabelPaint);
    //                    AttrStringUtils.drawRotatedString(lt.getAttributedLabel(), g2, anchorPoint[0], anchorPoint[1],
    //                            tick.getTextAnchor(), tick.getAngle(), tick.getRotationAnchor());
    //                } else {
    //                    if (tick.getText() == null) {
    //                        continue;
    //                    }
    //                    final float[] anchorPoint = calculateAnchorPoint(tick, cursor, dataArea, edge);
    //                    final Color tickLabelPaint = getTickLabelPaint(g2, dataArea, edge, tick);
    //                    g2.setPaint(tickLabelPaint);
    //                    TextUtils.drawRotatedString(tick.getText(), g2, anchorPoint[0], anchorPoint[1],
    //                            tick.getTextAnchor(), tick.getAngle(), tick.getRotationAnchor());
    //                }
    //            }
    //
    //            if ((isTickMarksVisible() && tick.getTickType().equals(TickType.MAJOR))
    //                    || (isMinorTickMarksVisible() && tick.getTickType().equals(TickType.MINOR))) {
    //
    //                final double ol = (tick.getTickType().equals(TickType.MINOR)) ? getMinorTickMarkOutsideLength()
    //                        : getTickMarkOutsideLength();
    //
    //                final double il = (tick.getTickType().equals(TickType.MINOR)) ? getMinorTickMarkInsideLength()
    //                        : getTickMarkInsideLength();
    //
    //                final float xx = (float) valueToJava2D(tick.getValue(), dataArea, edge);
    //                Line2D mark = null;
    //                g2.setStroke(getTickMarkStroke());
    //                g2.setPaint(getTickMarkPaint());
    //                if (edge == RectangleEdge.LEFT) {
    //                    mark = new Line2D.Double(cursor - ol, xx, cursor + il, xx);
    //                } else if (edge == RectangleEdge.RIGHT) {
    //                    mark = new Line2D.Double(cursor + ol, xx, cursor - il, xx);
    //                } else if (edge == RectangleEdge.TOP) {
    //                    mark = new Line2D.Double(xx, cursor - ol, xx, cursor + il);
    //                } else if (edge == RectangleEdge.BOTTOM) {
    //                    mark = new Line2D.Double(xx, cursor + ol, xx, cursor - il);
    //                }
    //                g2.draw(mark);
    //            }
    //        }
    //        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, saved);
    //
    //        // need to work out the space used by the tick labels...
    //        // so we can update the cursor...
    //        double used = 0.0;
    //        if (isTickLabelsVisible()) {
    //            if (edge == RectangleEdge.LEFT) {
    //                used += findMaximumTickLabelWidth(ticks, g2, plotArea, isVerticalTickLabels());
    //                state.cursorLeft(used);
    //            } else if (edge == RectangleEdge.RIGHT) {
    //                used = findMaximumTickLabelWidth(ticks, g2, plotArea, isVerticalTickLabels());
    //                state.cursorRight(used);
    //            } else if (edge == RectangleEdge.TOP) {
    //                used = findMaximumTickLabelHeight(ticks, g2, plotArea, isVerticalTickLabels());
    //                state.cursorUp(used);
    //            } else if (edge == RectangleEdge.BOTTOM) {
    //                used = findMaximumTickLabelHeight(ticks, g2, plotArea, isVerticalTickLabels());
    //                state.cursorDown(used);
    //            }
    //        }
    //
    //        return state;
    //    }
    //
    //    private Color getTickLabelPaint(final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge,
    //            final ValueTick tick) {
    //        final Color tickLabelPaint;
    //        if (isOverlappingWithPriceLineLabel(g2, dataArea, edge, tick)) {
    //            tickLabelPaint = Colors.setTransparency((Color) getTickLabelPaint(), Percent.SEVENTYFIVE_PERCENT);
    //        } else {
    //            tickLabelPaint = (Color) getTickLabelPaint();
    //        }
    //        return tickLabelPaint;
    //    }
    //
    //    private boolean isOverlappingWithPriceLineLabel(final Graphics2D g2, final Rectangle2D dataArea,
    //            final RectangleEdge edge, final ValueTick axisTick) {
    //        final XYPlot plot = (XYPlot) getPlot();
    //        for (int i = 0; i < plot.getRendererCount(); i++) {
    //            final XYItemRenderer renderer = plot.getRenderer(i);
    //            if (renderer instanceof IPriceLineRenderer && ((IPriceLineRenderer) renderer).isPriceLabelVisible()) {
    //                final ValueAxis rangeAxis = XYPlots.getRangeAxisForDatasetNullable(plot, i);
    //                if (this == rangeAxis) {
    //                    final XYDataset dataset = plot.getDataset(i);
    //                    final int lastItem = dataset.getItemCount(0) - 1;
    //                    final double lastPrice = (double) dataset.getY(0, lastItem);
    //                    final int height = g2.getFontMetrics().getHeight();
    //                    final int halfHeight = height / 2 + BACKGROUND_OVERLAP_TOLERANCE;
    //                    final double lastPrice2D = valueToJava2D(lastPrice, dataArea, edge);
    //                    final double tickValue2D = valueToJava2D(axisTick.getValue(), dataArea, edge);
    //
    //                    if (Doubles.isBetween(lastPrice2D, tickValue2D - halfHeight, tickValue2D + halfHeight)) {
    //                        return true;
    //                    }
    //                }
    //            }
    //        }
    //        return false;
    //    }

}
