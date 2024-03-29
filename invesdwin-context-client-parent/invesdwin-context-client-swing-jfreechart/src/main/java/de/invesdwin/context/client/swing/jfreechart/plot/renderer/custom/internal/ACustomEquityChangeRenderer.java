package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.internal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.HashUtils;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.RendererState;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.ui.GradientPaintTransformer;
import org.jfree.chart.ui.StandardGradientPaintTransformer;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.PriceInitialSettings;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.PlotSourceXYSeriesCollection;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.IUpDownColorRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.Renderers;
import de.invesdwin.context.jfreechart.dataset.ListXYSeriesOHLC;
import de.invesdwin.context.jfreechart.dataset.MutableXYDataItemOHLC;
import de.invesdwin.context.jfreechart.dataset.TimeRangedOHLCDataItem;
import de.invesdwin.util.lang.color.Colors;
import de.invesdwin.util.math.Doubles;

/**
 * High is Profit, Low is Drawdown and Close is EquityChange.
 * 
 */
@NotThreadSafe
public abstract class ACustomEquityChangeRenderer extends AbstractXYItemRenderer
        implements XYItemRenderer, PublicCloneable, IUpDownColorRenderer {

    private static final class XYAreaRendererState extends XYItemRendererState {

        private final XYAreaRendererStateData profit;
        private final XYAreaRendererStateData loss;
        private final Line2D line;

        private XYAreaRendererState(final PlotRenderingInfo info) {
            super(info);
            this.profit = new XYAreaRendererStateData();
            this.loss = new XYAreaRendererStateData();
            this.line = new Line2D.Double();
        }

    }

    private static final class XYAreaRendererStateData {
        private GeneralPath area;

        private XYAreaRendererStateData() {
            this.area = new GeneralPath();
        }
    }

    /** A flag indicating whether or not lines are drawn between XY points. */
    private final boolean plotLines;

    /** A flag indicating whether or not Area are drawn at each XY point. */
    private final boolean plotArea;

    /**
     * The shape used to represent an area in each legend item (this should never be {@code null}).
     */
    private transient Shape legendArea;

    /**
     * A transformer that is applied to the paint used to fill under the area *if* it is an instance of GradientPaint.
     *
     * @since 1.0.14
     */
    private GradientPaintTransformer gradientTransformer;

    public ACustomEquityChangeRenderer() {
        Renderers.disableAutoPopulate(this);

        this.plotArea = true;
        this.plotLines = true;

        final GeneralPath area = new GeneralPath();
        area.moveTo(0.0f, -4.0f);
        area.lineTo(3.0f, -2.0f);
        area.lineTo(4.0f, 4.0f);
        area.lineTo(-4.0f, 4.0f);
        area.lineTo(-3.0f, -2.0f);
        area.closePath();
        this.legendArea = area;
        this.gradientTransformer = new StandardGradientPaintTransformer();

        setDefaultStroke(PriceInitialSettings.DEFAULT_SERIES_STROKE);
    }

    /**
     * Returns true if lines are being plotted by the renderer.
     *
     * @return {@code true} if lines are being plotted by the renderer.
     */
    public boolean getPlotLines() {
        return this.plotLines;
    }

    /**
     * Returns true if Area is being plotted by the renderer.
     *
     * @return {@code true} if Area is being plotted by the renderer.
     */
    public boolean getPlotArea() {
        return this.plotArea;
    }

    /**
     * Returns the shape used to represent an area in the legend.
     *
     * @return The legend area (never {@code null}).
     */
    public Shape getLegendArea() {
        return this.legendArea;
    }

    /**
     * Sets the shape used as an area in each legend item and sends a {@link RendererChangeEvent} to all registered
     * listeners.
     *
     * @param area
     *            the area ({@code null} not permitted).
     */
    public void setLegendArea(final Shape area) {
        Args.nullNotPermitted(area, "area");
        this.legendArea = area;
        fireChangeEvent();
    }

    /**
     * Returns the gradient paint transformer.
     *
     * @return The gradient paint transformer (never {@code null}).
     *
     * @since 1.0.14
     */
    public GradientPaintTransformer getGradientTransformer() {
        return this.gradientTransformer;
    }

    /**
     * Sets the gradient paint transformer and sends a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param transformer
     *            the transformer ({@code null} not permitted).
     *
     * @since 1.0.14
     */
    public void setGradientTransformer(final GradientPaintTransformer transformer) {
        Args.nullNotPermitted(transformer, "transformer");
        this.gradientTransformer = transformer;
        fireChangeEvent();
    }

    /**
     * Initialises the renderer and returns a state object that should be passed to all subsequent calls to the
     * drawItem() method.
     *
     * @param g2
     *            the graphics device.
     * @param dataArea
     *            the area inside the axes.
     * @param plot
     *            the plot.
     * @param data
     *            the data.
     * @param info
     *            an optional info collection object to return data back to the caller.
     *
     * @return A state object for use by the renderer.
     */
    @Override
    public XYItemRendererState initialise(final Graphics2D g2, final Rectangle2D dataArea, final XYPlot plot,
            final XYDataset data, final PlotRenderingInfo info) {
        final XYAreaRendererState state = new XYAreaRendererState(info);
        return state;
    }

    /**
     * Returns a default legend item for the specified series. Subclasses should override this method to generate
     * customised items.
     *
     * @param datasetIndex
     *            the dataset index (zero-based).
     * @param series
     *            the series index (zero-based).
     *
     * @return A legend item for the series.
     */
    @Override
    public LegendItem getLegendItem(final int datasetIndex, final int series) {
        LegendItem result = null;
        final XYPlot xyplot = getPlot();
        if (xyplot != null) {
            final XYDataset dataset = xyplot.getDataset(datasetIndex);
            if (dataset != null) {
                final XYSeriesLabelGenerator lg = getLegendItemLabelGenerator();
                final String label = lg.generateLabel(dataset, series);
                final String description = label;
                String toolTipText = null;
                if (getLegendItemToolTipGenerator() != null) {
                    toolTipText = getLegendItemToolTipGenerator().generateLabel(dataset, series);
                }
                String urlText = null;
                if (getLegendItemURLGenerator() != null) {
                    urlText = getLegendItemURLGenerator().generateLabel(dataset, series);
                }
                final Paint paint = lookupSeriesPaint(series);
                result = new LegendItem(label, description, toolTipText, urlText, this.legendArea, paint);
                result.setLabelFont(lookupLegendTextFont(series));
                final Paint labelPaint = lookupLegendTextPaint(series);
                if (labelPaint != null) {
                    result.setLabelPaint(labelPaint);
                }
                result.setDataset(dataset);
                result.setDatasetIndex(datasetIndex);
                result.setSeriesKey(dataset.getSeriesKey(series));
                result.setSeriesIndex(series);
            }
        }
        return result;
    }

    /**
     * Draws the visual representation of a single data item.
     *
     * @param g2
     *            the graphics device.
     * @param state
     *            the renderer state.
     * @param dataArea
     *            the area within which the data is being drawn.
     * @param info
     *            collects information about the drawing.
     * @param plot
     *            the plot (can be used to obtain standard color information etc).
     * @param domainAxis
     *            the domain axis.
     * @param rangeAxis
     *            the range axis.
     * @param dataset
     *            the dataset.
     * @param series
     *            the series index (zero-based).
     * @param item1
     *            the item index (zero-based).
     * @param crosshairState
     *            crosshair information for the plot ({@code null} permitted).
     * @param pass
     *            the pass index.
     */
    @Override
    public void drawItem(final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea,
            final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis,
            final XYDataset dataset, final int series, final int item1, final CrosshairState crosshairState,
            final int pass) {
        final int lastDatasetItem = dataset.getItemCount(series) - 1;
        if (!getItemVisible(series, item1) || item1 > lastDatasetItem) {
            return;
        }
        final XYAreaRendererState areaState = (XYAreaRendererState) state;

        // get the data point...
        final PlotSourceXYSeriesCollection cDataset = (PlotSourceXYSeriesCollection) dataset;
        final ListXYSeriesOHLC cSeries = cDataset.getSeries(series);
        final List<MutableXYDataItemOHLC> data = cSeries.getData();
        final int item0 = Math.max(item1 - 1, 0);
        final TimeRangedOHLCDataItem cItem0 = data.get(item0).getOHLC();
        final TimeRangedOHLCDataItem cItem1 = data.get(item1).getOHLC();

        final double x1 = dataset.getXValue(series, item1);
        final double x0 = dataset.getXValue(series, item0);
        final int itemClose;
        final double xClose;
        final Paint lineColor;
        if (Double.isNaN(cItem1.getClose())) {
            lineColor = Colors.INVISIBLE_COLOR;
            //workaround for in-progress-bar
            if (item1 == lastDatasetItem) {
                xClose = x0;
                itemClose = item0;
            } else {
                xClose = x1;
                itemClose = item1;
            }
        } else {
            lineColor = lookupSeriesPaint(series);
            xClose = x1;
            itemClose = item1;
        }

        drawLine(g2, dataArea, plot, domainAxis, rangeAxis, series, item1, areaState, x0, convert(cItem0.getClose()),
                x1, convert(cItem1.getClose()), lineColor);
        drawArea(dataArea, plot, domainAxis, rangeAxis, series, itemClose, areaState.profit, xClose,
                convert(cItem1.getHigh()), dataset, state, state.getFirstItemIndex());
        drawArea(dataArea, plot, domainAxis, rangeAxis, series, itemClose, areaState.loss, xClose,
                convert(cItem1.getLow()), dataset, state, state.getFirstItemIndex());

        // Check if the item is the last item for the series.
        // and number of items > 0.  We can't draw an area for a single point.
        if (getPlotArea() && item1 > 0 && (item1 == lastDatasetItem || item1 == state.getLastItemIndex())) {
            //this should never be invisible color
            closeArea(g2, dataArea, plot, domainAxis, rangeAxis, getUpColor(), xClose, areaState.profit);
            closeArea(g2, dataArea, plot, domainAxis, rangeAxis, getDownColor(), xClose, areaState.loss);
        }

    }

    private double convert(final Number value) {
        return Doubles.nanToZero(value.doubleValue());
    }

    private void closeArea(final Graphics2D g2, final Rectangle2D dataArea, final XYPlot plot,
            final ValueAxis domainAxis, final ValueAxis rangeAxis, final Color upColor, final double x1,
            final XYAreaRendererStateData areaStateData) {
        final double transX1 = domainAxis.valueToJava2D(x1, dataArea, plot.getDomainAxisEdge());
        final double transZero = rangeAxis.valueToJava2D(0.0, dataArea, plot.getRangeAxisEdge());

        final PlotOrientation orientation = plot.getOrientation();
        if (orientation == PlotOrientation.VERTICAL) {
            // Add the last point (x,0)
            lineTo(areaStateData.area, transX1, transZero);
            areaStateData.area.closePath();
        } else if (orientation == PlotOrientation.HORIZONTAL) {
            // Add the last point (x,0)
            lineTo(areaStateData.area, transZero, transX1);
            areaStateData.area.closePath();
        }

        g2.setPaint(upColor);
        g2.fill(areaStateData.area);
    }

    private void drawArea(final Rectangle2D dataArea, final XYPlot plot, final ValueAxis domainAxis,
            final ValueAxis rangeAxis, final int series, final int item, final XYAreaRendererStateData areaStateData,
            final double x1, final double y1, final XYDataset dataset, final RendererState state, final int firstItem) {
        final double transX1 = domainAxis.valueToJava2D(x1, dataArea, plot.getDomainAxisEdge());
        final double transY1 = rangeAxis.valueToJava2D(y1, dataArea, plot.getRangeAxisEdge());

        if (item == firstItem) { // create a new area polygon for the series
            areaStateData.area = new GeneralPath();
            // the first point is (x, 0)
            final double zero = rangeAxis.valueToJava2D(0.0, dataArea, plot.getRangeAxisEdge());
            if (plot.getOrientation().isVertical()) {
                moveTo(areaStateData.area, transX1, zero);
            } else if (plot.getOrientation().isHorizontal()) {
                moveTo(areaStateData.area, zero, transX1);
            }
        }

        // Add each point to Area (x, y)
        if (plot.getOrientation().isVertical()) {
            lineTo(areaStateData.area, transX1, transY1);
        } else if (plot.getOrientation().isHorizontal()) {
            lineTo(areaStateData.area, transY1, transX1);
        }
    }

    private void drawLine(final Graphics2D g2, final Rectangle2D dataArea, final XYPlot plot,
            final ValueAxis domainAxis, final ValueAxis rangeAxis, final int series, final int item,
            final XYAreaRendererState areaState, final double x0, final double y0, final double x1, final double y1,
            final Paint paint) {
        final double transX1 = domainAxis.valueToJava2D(x1, dataArea, plot.getDomainAxisEdge());
        final double transY1 = rangeAxis.valueToJava2D(y1, dataArea, plot.getRangeAxisEdge());

        // get the previous point and the next point so we can calculate a
        // "hot spot" for the area (used by the chart entity)...
        final double transX0 = domainAxis.valueToJava2D(x0, dataArea, plot.getDomainAxisEdge());
        final double transY0 = rangeAxis.valueToJava2D(y0, dataArea, plot.getRangeAxisEdge());

        final Stroke stroke = getItemStroke(series, item);
        g2.setPaint(paint);
        g2.setStroke(stroke);

        if (getPlotLines()) {
            if (item > 0) {
                if (plot.getOrientation() == PlotOrientation.VERTICAL) {
                    areaState.line.setLine(transX0, transY0, transX1, transY1);
                } else if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
                    areaState.line.setLine(transY0, transX0, transY1, transX1);
                }
                g2.draw(areaState.line);
            }
        }

    }

    /**
     * Returns a clone of the renderer.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException
     *             if the renderer cannot be cloned.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        final ACustomEquityChangeRenderer clone = (ACustomEquityChangeRenderer) super.clone();
        clone.legendArea = ShapeUtils.clone(this.legendArea);
        return clone;
    }

    /**
     * Tests this renderer for equality with an arbitrary object.
     *
     * @param obj
     *            the object ({@code null} permitted).
     *
     * @return A boolean.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ACustomEquityChangeRenderer)) {
            return false;
        }
        final ACustomEquityChangeRenderer that = (ACustomEquityChangeRenderer) obj;
        if (this.plotArea != that.plotArea) {
            return false;
        }
        if (this.plotLines != that.plotLines) {
            return false;
        }
        if (!this.gradientTransformer.equals(that.gradientTransformer)) {
            return false;
        }
        if (!ShapeUtils.equal(this.legendArea, that.legendArea)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a hash code for this instance.
     *
     * @return A hash code.
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = HashUtils.hashCode(result, this.plotArea);
        result = HashUtils.hashCode(result, this.plotLines);
        return result;
    }

    /**
     * Provides serialization support.
     *
     * @param stream
     *            the input stream.
     *
     * @throws IOException
     *             if there is an I/O error.
     * @throws ClassNotFoundException
     *             if there is a classpath problem.
     */
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.legendArea = SerialUtils.readShape(stream);
    }

    /**
     * Provides serialization support.
     *
     * @param stream
     *            the output stream.
     *
     * @throws IOException
     *             if there is an I/O error.
     */
    private void writeObject(final ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writeShape(this.legendArea, stream);
    }

    @Override
    public Range findRangeBounds(final XYDataset dataset) {
        //include interval per default
        return super.findRangeBounds(dataset, true);
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

}
