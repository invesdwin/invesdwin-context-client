package de.invesdwin.context.client.swing.jfreechart.plot;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.annotation.concurrent.Immutable;

import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.Range;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYDataset;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.basis.CustomCombinedDomainXYPlot;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.DisabledXYDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.util.collections.delegate.NullSafeDelegateMap;
import de.invesdwin.util.collections.fast.concurrent.SynchronizedFastIterableDelegateList;
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

    private static final UnsafeField<Boolean> XYPLOT_RANGE_CROSSHAIR_LOCKED_ON_DATA_FIELD;
    private static final UnsafeField<Boolean> XYPLOT_DOMAIN_CROSSHAIR_LOCKED_ON_DATA_FIELD;
    private static final UnsafeField<Map<Integer, ValueAxis>> XYPLOT_DOMAINAXES_FIELD;
    private static final UnsafeField<Map<Integer, AxisLocation>> XYPLOT_DOMAINAXISLOCATIONS_FIELD;
    private static final UnsafeField<Map<Integer, ValueAxis>> XYPLOT_RANGEAXES_FIELD;
    private static final UnsafeField<Map<Integer, AxisLocation>> XYPLOT_RANGEAXISLOCATIONS_FIELD;
    private static final UnsafeField<Map<Integer, XYDataset>> XYPLOT_DATASETS_FIELD;
    private static final UnsafeField<Map<Integer, XYItemRenderer>> XYPLOT_RENDERERS_FIELD;
    private static final UnsafeField<Map<Integer, List<Integer>>> XYPLOT_DATASETTODOMAINAXESMAP_FIELD;
    private static final UnsafeField<Map<Integer, List<Integer>>> XYPLOT_DATASETTORANGEAXESMAP_FIELD;
    @SuppressWarnings("rawtypes")
    private static final UnsafeField<Map> XYPLOT_FOREGROUNDDOMAINMARKERS_FIELD;
    @SuppressWarnings("rawtypes")
    private static final UnsafeField<Map> XYPLOT_BACKGROUNDDOMAINMARKERS_FIELD;
    @SuppressWarnings("rawtypes")
    private static final UnsafeField<Map> XYPLOT_FOREGROUNDRANGEMARKERS_FIELD;
    @SuppressWarnings("rawtypes")
    private static final UnsafeField<Map> XYPLOT_BACKGROUNDRANGEMARKERS_FIELD;
    private static final UnsafeField<List<XYAnnotation>> XYPLOT_ANNOTATIONS_FIELD;

    static {
        try {
            //          private final Map<Integer, ValueAxis> domainAxes;
            final Field xyPlotDomainAxesField = XYPlot.class.getDeclaredField("domainAxes");
            XYPLOT_DOMAINAXES_FIELD = new UnsafeField<>(xyPlotDomainAxesField);
            //          private final Map<Integer, AxisLocation> domainAxisLocations;
            final Field xyPlotDomainAxisLocationsField = XYPlot.class.getDeclaredField("domainAxisLocations");
            XYPLOT_DOMAINAXISLOCATIONS_FIELD = new UnsafeField<>(xyPlotDomainAxisLocationsField);
            //          private final Map<Integer, ValueAxis> rangeAxes;
            final Field xyPlotRangeAxesField = XYPlot.class.getDeclaredField("rangeAxes");
            XYPLOT_RANGEAXES_FIELD = new UnsafeField<>(xyPlotRangeAxesField);
            //          private final Map<Integer, AxisLocation> rangeAxisLocations;
            final Field xyPlotRangeAxisLocationsField = XYPlot.class.getDeclaredField("rangeAxisLocations");
            XYPLOT_RANGEAXISLOCATIONS_FIELD = new UnsafeField<>(xyPlotRangeAxisLocationsField);
            //          private final Map<Integer, XYDataset> datasets;
            final Field xyPlotDatasetsField = XYPlot.class.getDeclaredField("datasets");
            XYPLOT_DATASETS_FIELD = new UnsafeField<>(xyPlotDatasetsField);
            //          private final Map<Integer, XYItemRenderer> renderers;
            final Field xyPlotRenderersField = XYPlot.class.getDeclaredField("renderers");
            XYPLOT_RENDERERS_FIELD = new UnsafeField<>(xyPlotRenderersField);
            //          private final Map<Integer, List<Integer>> datasetToDomainAxesMap;
            final Field xyPlotDatasetToDomainAxesMapField = XYPlot.class.getDeclaredField("datasetToDomainAxesMap");
            XYPLOT_DATASETTODOMAINAXESMAP_FIELD = new UnsafeField<>(xyPlotDatasetToDomainAxesMapField);
            //          private final Map<Integer, List<Integer>> datasetToRangeAxesMap;
            final Field xyPlotDatasetToRangeAxesMapField = XYPlot.class.getDeclaredField("datasetToRangeAxesMap");
            XYPLOT_DATASETTORANGEAXESMAP_FIELD = new UnsafeField<>(xyPlotDatasetToRangeAxesMapField);
            //          private final Map foregroundDomainMarkers;
            final Field xyPlotForegroundDomainMarkersField = XYPlot.class.getDeclaredField("foregroundDomainMarkers");
            XYPLOT_FOREGROUNDDOMAINMARKERS_FIELD = new UnsafeField<>(xyPlotForegroundDomainMarkersField);
            //          private final Map backgroundDomainMarkers;
            final Field xyPlotBackgroundDomainMarkersField = XYPlot.class.getDeclaredField("backgroundDomainMarkers");
            XYPLOT_BACKGROUNDDOMAINMARKERS_FIELD = new UnsafeField<>(xyPlotBackgroundDomainMarkersField);
            //          private final Map foregroundRangeMarkers;
            final Field xyPlotForegroundRangeMarkersField = XYPlot.class.getDeclaredField("foregroundRangeMarkers");
            XYPLOT_FOREGROUNDRANGEMARKERS_FIELD = new UnsafeField<>(xyPlotForegroundRangeMarkersField);
            //          private final Map backgroundRangeMarkers;
            final Field xyPlotBackgroundRangeMarkersField = XYPlot.class.getDeclaredField("backgroundRangeMarkers");
            XYPLOT_BACKGROUNDRANGEMARKERS_FIELD = new UnsafeField<>(xyPlotBackgroundRangeMarkersField);
            //          private final List<XYAnnotation> annotations;
            final Field xyPlotAnnotationsField = XYPlot.class.getDeclaredField("annotations");
            XYPLOT_ANNOTATIONS_FIELD = new UnsafeField<>(xyPlotAnnotationsField);

            final Field xyPlotRangeCrosshairLockedOnDataField = XYPlot.class
                    .getDeclaredField("rangeCrosshairLockedOnData");
            XYPLOT_RANGE_CROSSHAIR_LOCKED_ON_DATA_FIELD = new UnsafeField<>(xyPlotRangeCrosshairLockedOnDataField);

            final Field xyPlotDomainCrosshairLockedOnDataField = XYPlot.class
                    .getDeclaredField("domainCrosshairLockedOnData");
            XYPLOT_DOMAIN_CROSSHAIR_LOCKED_ON_DATA_FIELD = new UnsafeField<>(xyPlotDomainCrosshairLockedOnDataField);
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
                if (rangeAxis != null && !rangeAxis.isAutoRange()) {
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

    public static void removeDomainMarker(final XYPlot xyPlot, final Marker marker) {
        xyPlot.removeDomainMarker(0, marker, Layer.FOREGROUND, false);
    }

    public static void removeRangeMarker(final XYPlot xyPlot, final Marker marker) {
        xyPlot.removeRangeMarker(0, marker, Layer.FOREGROUND, false);
    }

    public static void setRangeCrosshairLockedOnData(final XYPlot xyPlot, final boolean flag) {
        XYPLOT_RANGE_CROSSHAIR_LOCKED_ON_DATA_FIELD.put(xyPlot, flag);
    }

    public static void setDomainCrosshairLockedOnData(final XYPlot xyPlot, final boolean flag) {
        XYPLOT_DOMAIN_CROSSHAIR_LOCKED_ON_DATA_FIELD.put(xyPlot, flag);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void makeThreadSafe(final XYPlot xyPlot) {
        if (!(XYPLOT_DOMAINAXES_FIELD.get(xyPlot) instanceof HashMap)) {
            return;
        }
        //          private final Map<Integer, ValueAxis> domainAxes;
        //        this.domainAxes = new HashMap<Integer, ValueAxis>();
        final Map<Integer, ValueAxis> domainAxes = newConcurrentMap(XYPLOT_DOMAINAXES_FIELD.get(xyPlot));
        XYPLOT_DOMAINAXES_FIELD.put(xyPlot, domainAxes);
        //          private final Map<Integer, AxisLocation> domainAxisLocations;
        //        this.domainAxisLocations = new HashMap<Integer, AxisLocation>();
        final Map<Integer, AxisLocation> domainAxisLocations = newConcurrentMap(
                XYPLOT_DOMAINAXISLOCATIONS_FIELD.get(xyPlot));
        XYPLOT_DOMAINAXISLOCATIONS_FIELD.put(xyPlot, domainAxisLocations);
        //          private final Map<Integer, ValueAxis> rangeAxes;
        //        this.rangeAxes = new HashMap<Integer, ValueAxis>();
        final Map<Integer, ValueAxis> rangeAxes = newConcurrentMap(XYPLOT_RANGEAXES_FIELD.get(xyPlot));
        XYPLOT_RANGEAXES_FIELD.put(xyPlot, rangeAxes);
        //          private final Map<Integer, AxisLocation> rangeAxisLocations;
        //        this.rangeAxisLocations = new HashMap<Integer, AxisLocation>();
        final Map<Integer, AxisLocation> rangeAxisLocations = newConcurrentMap(
                XYPLOT_RANGEAXISLOCATIONS_FIELD.get(xyPlot));
        XYPLOT_RANGEAXISLOCATIONS_FIELD.put(xyPlot, rangeAxisLocations);
        //          private final Map<Integer, XYDataset> datasets;
        //        this.datasets = new HashMap<Integer, XYDataset>();
        final Map<Integer, XYDataset> datasets = newConcurrentMap(XYPLOT_DATASETS_FIELD.get(xyPlot));
        XYPLOT_DATASETS_FIELD.put(xyPlot, datasets);
        //          private final Map<Integer, XYItemRenderer> renderers;
        //        this.renderers = new HashMap<Integer, XYItemRenderer>();
        final Map<Integer, XYItemRenderer> renderers = newConcurrentMap(XYPLOT_RENDERERS_FIELD.get(xyPlot));
        XYPLOT_RENDERERS_FIELD.put(xyPlot, renderers);
        //          private final Map<Integer, List<Integer>> datasetToDomainAxesMap;
        //        this.datasetToDomainAxesMap = new TreeMap();
        final Map datasetToDomainAxesMap = newConcurrentNavigableMap(XYPLOT_DATASETTODOMAINAXESMAP_FIELD.get(xyPlot));
        XYPLOT_DATASETTODOMAINAXESMAP_FIELD.put(xyPlot, datasetToDomainAxesMap);
        //          private final Map<Integer, List<Integer>> datasetToRangeAxesMap;
        //        this.datasetToRangeAxesMap = new TreeMap();
        final Map datasetToRangeAxesMap = newConcurrentNavigableMap(XYPLOT_DATASETTORANGEAXESMAP_FIELD.get(xyPlot));
        XYPLOT_DATASETTORANGEAXESMAP_FIELD.put(xyPlot, datasetToRangeAxesMap);
        //          private final Map foregroundDomainMarkers;
        //        this.foregroundDomainMarkers = new HashMap();
        final Map foregroundDomainMarkers = newConcurrentMap(XYPLOT_FOREGROUNDDOMAINMARKERS_FIELD.get(xyPlot));
        XYPLOT_FOREGROUNDDOMAINMARKERS_FIELD.put(xyPlot, foregroundDomainMarkers);
        //          private final Map backgroundDomainMarkers;
        //        this.backgroundDomainMarkers = new HashMap();
        final Map backgroundDomainMarkers = newConcurrentMap(XYPLOT_BACKGROUNDDOMAINMARKERS_FIELD.get(xyPlot));
        XYPLOT_BACKGROUNDDOMAINMARKERS_FIELD.put(xyPlot, backgroundDomainMarkers);
        //          private final Map foregroundRangeMarkers;
        //        this.foregroundRangeMarkers = new HashMap();
        final Map foregroundRangeMarkers = newConcurrentMap(XYPLOT_FOREGROUNDRANGEMARKERS_FIELD.get(xyPlot));
        XYPLOT_FOREGROUNDRANGEMARKERS_FIELD.put(xyPlot, foregroundRangeMarkers);
        //          private final Map backgroundRangeMarkers;
        //        this.backgroundRangeMarkers = new HashMap();
        final Map backgroundRangeMarkers = newConcurrentMap(XYPLOT_BACKGROUNDRANGEMARKERS_FIELD.get(xyPlot));
        XYPLOT_BACKGROUNDRANGEMARKERS_FIELD.put(xyPlot, backgroundRangeMarkers);
        //        private static final UnsafeField<List<XYAnnotation>> XYPLOT_ANNOTATIONS_FIELD;
        //        this.annotations = new java.util.ArrayList();
        final List annotations = new SynchronizedFastIterableDelegateList(XYPLOT_ANNOTATIONS_FIELD.get(xyPlot));
        XYPLOT_ANNOTATIONS_FIELD.put(xyPlot, annotations);
    }

    private static <K, V> Map<K, V> newConcurrentMap(final Map<K, V> map) {
        final Map<K, V> newMap = new NullSafeDelegateMap<K, V>(new ConcurrentHashMap<K, V>(map.size()));
        newMap.putAll(map);
        return newMap;
    }

    private static <K, V> Map<K, V> newConcurrentNavigableMap(final Map<K, V> map) {
        return new ConcurrentSkipListMap<K, V>(map);
    }
}
