package de.invesdwin.context.client.swing.jfreechart.plot.dataset;

import java.io.Closeable;

import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.expression.IExpressionSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.indicator.IIndicatorSeriesProvider;
import de.invesdwin.util.math.expression.IExpression;

public interface IPlotSourceDataset extends XYDataset, OHLCDataset, Closeable, IDrawIncompleteBar {

    XYPlot getPlot();

    void setPlot(XYPlot plot);

    @Override
    default void close() {
        setPlot(null);
    }

    Integer getPrecision();

    void setPrecision(Integer precision);

    String getInitialPlotPaneId();

    void setInitialPlotPaneId(String initialPlotPaneId);

    String getRangeAxisId();

    void setRangeAxisId(String rangeAxisId);

    boolean isLegendValueVisible(int series, int item);

    default String getSeriesId() {
        return (String) getSeriesKey(0);
    }

    /**
     * Use getSeriesId() instead
     */
    @SuppressWarnings("rawtypes")
    @Deprecated
    @Override
    Comparable getSeriesKey(int series);

    String getSeriesTitle();

    void setSeriesTitle(String seriesTitle);

    IIndicatorSeriesProvider getIndicatorSeriesProvider();

    void setIndicatorSeriesProvider(IIndicatorSeriesProvider indicatorSeriesProvider);

    void setIndicatorSeriesArguments(IExpression[] indicatorSeriesArguments);

    IExpression[] getIndicatorSeriesArguments();

    default boolean hasIndicatorSeriesArguments() {
        final IExpression[] args = getIndicatorSeriesArguments();
        return args != null && args.length > 0;
    }

    IExpressionSeriesProvider getExpressionSeriesProvider();

    void setExpressionSeriesProvider(IExpressionSeriesProvider expressionSeriesProvider);

    String getExpressionSeriesArguments();

    void setExpressionSeriesArguments(String expressionSeriesArguments);

    default boolean hasExpressionSeriesArguments() {
        return getExpressionSeriesArguments() != null;
    }

    IndexedDateTimeOHLCDataset getMasterDataset();

}
