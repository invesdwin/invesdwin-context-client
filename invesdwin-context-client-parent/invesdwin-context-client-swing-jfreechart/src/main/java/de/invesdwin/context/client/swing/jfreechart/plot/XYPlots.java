package de.invesdwin.context.client.swing.jfreechart.plot;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.Immutable;

import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.Range;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYDataset;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.basis.CustomCombinedDomainXYPlot;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.DisabledXYDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.util.lang.reflection.field.UnsafeField;
import de.invesdwin.util.math.Doubles;
import de.invesdwin.util.math.Integers;
import de.invesdwin.util.math.decimal.Decimal;
import de.invesdwin.util.math.decimal.scaled.Percent;
import de.invesdwin.util.math.decimal.scaled.PercentScale;
import de.invesdwin.util.swing.HiDPI;

@Immutable
public final class XYPlots {

    public static final NumberAxis DRAWING_ABSOLUTE_AXIS = new NumberAxis() {
        @Override
        public double valueToJava2D(final double value, final Rectangle2D area, final RectangleEdge edge) {
            return value;
        }
    };
    public static final Font DEFAULT_FONT = new Font("Verdana", Font.PLAIN, HiDPI.scale(9));

    private static final UnsafeField<List<XYAnnotation>> XYPLOT_ANNOTATIONS_FIELD;

    static {
        try {
            final Field xyPlotAnnotationsField = XYPlot.class.getDeclaredField("annotations");
            XYPLOT_ANNOTATIONS_FIELD = new UnsafeField<>(xyPlotAnnotationsField);
        } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private XYPlots() {}

    public static int getFreeDatasetIndex(final XYPlot plot) {
        for (int i = 0; i < plot.getDatasetCount(); i++) {
            if (plot.getDataset(i) == null) {
                return i;
            }
        }
        return plot.getDatasetCount();
    }

    /**
     * XYPlot.getAnnotations creates a new ArrayList (most likely to prevent concurrent modification exceptions)
     */
    public static List<XYAnnotation> getAnnotations(final XYPlot plot) {
        return XYPLOT_ANNOTATIONS_FIELD.get(plot);
    }

    public static void updateRangeAxes(final XYPlot plot) {
        final Map<String, RangeAxisData> rangeAxisId_data = new LinkedHashMap<>();
        int rangeAxisIndex = -1;
        for (int datasetIndex = 0; datasetIndex < plot.getDatasetCount(); datasetIndex++) {
            final IPlotSourceDataset dataset = (IPlotSourceDataset) plot.getDataset(datasetIndex);
            if (dataset != null) {
                final boolean visible = !(dataset instanceof DisabledXYDataset);
                String rangeAxisId = dataset.getRangeAxisId();
                if (!visible) {
                    rangeAxisId += "_invisible";
                }
                RangeAxisData data = rangeAxisId_data.get(rangeAxisId);
                if (data == null) {
                    rangeAxisIndex++;
                    final ValueAxis rangeAxis = plot.getRangeAxisForDataset(datasetIndex);
                    data = rangeAxis != null
                            ? new RangeAxisData(rangeAxisId, rangeAxisIndex, rangeAxis.isAutoRange(),
                                    rangeAxis.getRange())
                            : new RangeAxisData(rangeAxisId, rangeAxisIndex, visible, new Range(0, 1));
                    rangeAxisId_data.put(rangeAxisId, data);
                }
                data.getDatasetIndexes().add(datasetIndex);
                if (visible) {
                    data.setPrecision(Integers.max(data.getPrecision(), dataset.getPrecision()));
                    data.setVisible(true);
                }
            }
        }
        removeRangeAxes(plot);
        if (rangeAxisId_data.isEmpty()) {
            plot.setRangeAxis(newRangeAxis(0, false, false));
        } else {
            int countVisibleRangeAxes = 0;
            //first add the visible range axis, right=0 and left=1
            for (final RangeAxisData rangeAxisData : rangeAxisId_data.values()) {
                if (rangeAxisData.isVisible()) {
                    countVisibleRangeAxes++;
                    addRangeAxis(plot, countVisibleRangeAxes, rangeAxisData);
                }
            }
            //then the rest are the invisible ones
            for (final RangeAxisData rangeAxisData : rangeAxisId_data.values()) {
                if (!rangeAxisData.isVisible()) {
                    addRangeAxis(plot, countVisibleRangeAxes, rangeAxisData);
                }
            }
            configureRangeAxes(plot);
        }
    }

    private static void addRangeAxis(final XYPlot plot, final int countVisibleRangeAxes,
            final RangeAxisData rangeAxisData) {
        final boolean visible = rangeAxisData.isVisible() && countVisibleRangeAxes <= 2;
        final AxisLocation location;
        final int rangeAxisIndex = rangeAxisData.getRangeAxisIndex();
        if (countVisibleRangeAxes == 2) {
            location = AxisLocation.TOP_OR_LEFT;
        } else {
            location = AxisLocation.TOP_OR_RIGHT;
        }
        final NumberAxis rangeAxis = newRangeAxis(rangeAxisData.getPrecision(), visible, rangeAxisData);
        plot.setRangeAxis(rangeAxisIndex, rangeAxis);
        plot.setRangeAxisLocation(rangeAxisIndex, location);
        for (final int datasetIndex : rangeAxisData.getDatasetIndexes()) {
            plot.mapDatasetToDomainAxis(datasetIndex, 0);
            plot.mapDatasetToRangeAxis(datasetIndex, rangeAxisIndex);
        }
    }

    public static void configureRangeAxes(final XYPlot plot) {
        for (int i = 0; i < plot.getRangeAxisCount(); i++) {
            final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis(i);
            if (rangeAxis == null) {
                break;
            }
            rangeAxis.configure();
        }
    }

    public static void removeRangeAxes(final XYPlot plot) {
        final int rangeAxisCount = plot.getRangeAxisCount();
        for (int i = 0; i < rangeAxisCount; i++) {
            plot.setRangeAxis(i, null);
        }
    }

    public static NumberAxis newRangeAxis(final int precision, final boolean visible, final boolean autorange) {
        final NumberAxis rangeAxis = newRangeAxis(precision, visible);
        rangeAxis.setAutoRange(autorange);
        if (!autorange) {
            rangeAxis.setRange(0, 1);
        }
        return rangeAxis;
    }

    public static NumberAxis newRangeAxis(final int precision, final boolean visible,
            final RangeAxisData rangeAxisData) {
        final NumberAxis rangeAxis = newRangeAxis(precision, visible);
        if (!rangeAxisData.isAutoRange()) {
            rangeAxis.setRange(rangeAxisData.getRange());
        }
        return rangeAxis;
    }

    private static NumberAxis newRangeAxis(final int precision, final boolean visible) {
        final NumberAxis rangeAxis = new NumberAxis();
        rangeAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setNumberFormatOverride(Decimal
                .newDecimalFormatInstance(PercentScale.RATE.getFormat(Percent.ZERO_PERCENT, false, precision, false)));
        rangeAxis.setVisible(visible);
        rangeAxis.setLabelFont(DEFAULT_FONT);
        rangeAxis.setTickLabelFont(DEFAULT_FONT);
        return rangeAxis;
    }

    public static boolean hasDataset(final XYPlot plot) {
        for (int datasetIndex = 0; datasetIndex < plot.getDatasetCount(); datasetIndex++) {
            if (plot.getDataset() != null) {
                return true;
            }
        }
        return false;
    }

    public static void removeDataset(final XYPlot plot, final int datasetIndex) {
        final boolean notifyBefore = plot.isNotify();
        plot.setNotify(false);
        final int lastDatasetIndex = plot.getDatasetCount() - 1;
        for (int i = datasetIndex; i < lastDatasetIndex; i++) {
            plot.setDataset(i, plot.getDataset(i + 1));
            plot.setRenderer(i, plot.getRenderer(i + 1));
        }
        plot.setDataset(lastDatasetIndex, null);
        plot.setRenderer(lastDatasetIndex, null);
        plot.setNotify(notifyBefore);
    }

    @SuppressWarnings("resource")
    public static Integer getDatasetIndexForDataset(final XYPlot plot, final Dataset dataset,
            final boolean shouldThrow) {
        for (int datasetIndex = 0; datasetIndex <= plot.getDatasetCount(); datasetIndex++) {
            final XYDataset potentialDataset = plot.getDataset(datasetIndex);
            if (potentialDataset != null) {
                if (potentialDataset == dataset) {
                    return datasetIndex;
                }
                if (potentialDataset instanceof DisabledXYDataset) {
                    final DisabledXYDataset cPotentialDataset = (DisabledXYDataset) potentialDataset;
                    if (cPotentialDataset.getEnabledDataset() == dataset) {
                        return datasetIndex;
                    }
                }
            }
        }
        if (shouldThrow) {
            throw new IllegalStateException("No datasetIndex found for dataset");
        } else {
            return null;
        }
    }

    public static void updateMarker(final XYPlot plot, final int index, final ValueMarker marker, final Layer layer,
            final double value, final boolean notify) {
        if (marker == null) {
            return;
        }
        if (Doubles.isNaN(value)) {
            removeMarker(plot, index, marker, layer, notify);
        } else {
            addMarker(plot, index, marker, layer, value, notify);
        }
    }

    public static void addMarker(final XYPlot plot, final int index, final ValueMarker marker, final Layer layer,
            final double value, final boolean notify) {
        if (Doubles.isNaN(marker.getValue())) {
            marker.setValue(value);
            plot.addDomainMarker(index, marker, layer, notify);
        } else {
            marker.setValue(value);
        }
    }

    public static void removeMarker(final XYPlot plot, final int index, final ValueMarker marker, final Layer layer,
            final boolean notify) {
        if (plot.removeDomainMarker(index, marker, layer, notify)) {
            marker.setValue(Double.NaN);
        }
    }

    public static void resetAllRangePannables(final InteractiveChartPanel chartPanel) {
        chartPanel.getCombinedPlot().getSubplots().forEach(xyPlot -> xyPlot.setRangePannable(false));
    }

    /**
     * Checks if any dataset of the plot is assigned to the rangeAxisId.
     */
    public static boolean doesPlotContainRangeAxisId(final XYPlot plot, final String rangeAxisId) {
        for (int i = 0; i < plot.getDatasetCount(); i++) {
            final IPlotSourceDataset datasetToPlot = (IPlotSourceDataset) plot.getDataset(i);
            if (datasetToPlot != null && datasetToPlot.getRangeAxisId().equals(rangeAxisId)) {
                return true;
            }
        }
        return false;
    }

    public static XYPlot getSubplot(final InteractiveChartPanel chartPanel, final MouseEvent e) {
        final CustomCombinedDomainXYPlot combinedXyPlot = chartPanel.getCombinedPlot();
        final int subplotIndex = combinedXyPlot.getSubplotIndex(e.getX(), e.getY());
        if (subplotIndex == -1) {
            return null;
        }
        return combinedXyPlot.getSubplots().get(subplotIndex);
    }

    /**
     * Checks every subplot for the autoRange-property on every of it's axises and set's the rangePannable-property of
     * that subplot accordingly. If there is an axis with autoRange = false the rangePannable-property will be set to
     * true.
     */
    public static void setSuitableRangePannablesForSubplots(final InteractiveChartPanel chartPanel) {
        for (final XYPlot subplot : chartPanel.getCombinedPlot().getSubplots()) {
            boolean rangePannable = false;
            for (int i = 0; i < subplot.getRangeAxisCount(); i++) {
                final ValueAxis rangeAxis = subplot.getRangeAxis(i);
                if (!rangeAxis.isAutoRange()) {
                    rangePannable = true;
                    break;
                }
            }
            subplot.setRangePannable(rangePannable);
        }
    }

    /**
     * Disables all rangePannable-properties on every subplot except the exceptionPlot.
     */
    public static void disableRangePannables(final InteractiveChartPanel chartPanel, final XYPlot exceptionPlot) {
        for (final XYPlot subplot : chartPanel.getCombinedPlot().getSubplots()) {
            if (subplot != exceptionPlot) {
                subplot.setRangePannable(false);
            }
        }
    }
}
