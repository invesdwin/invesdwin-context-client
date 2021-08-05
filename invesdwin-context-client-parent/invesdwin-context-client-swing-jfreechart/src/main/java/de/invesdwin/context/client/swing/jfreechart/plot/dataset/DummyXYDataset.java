package de.invesdwin.context.client.swing.jfreechart.plot.dataset;

import javax.annotation.concurrent.Immutable;

import org.jfree.chart.plot.XYPlot;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.expression.IExpressionSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.indicator.IIndicatorSeriesProvider;
import de.invesdwin.util.math.expression.IExpression;

@SuppressWarnings("rawtypes")
@Immutable
public class DummyXYDataset implements IPlotSourceDataset {

    public static final DummyXYDataset INSTANCE = new DummyXYDataset();

    protected DummyXYDataset() {
    }

    @Override
    public DomainOrder getDomainOrder() {
        return null;
    }

    @Override
    public int getItemCount(final int series) {
        return 0;
    }

    @Override
    public Number getX(final int series, final int item) {
        return null;
    }

    @Override
    public double getXValue(final int series, final int item) {
        return Double.NaN;
    }

    @Override
    public Number getY(final int series, final int item) {
        return null;
    }

    @Override
    public double getYValue(final int series, final int item) {
        return Double.NaN;
    }

    @Override
    public int getSeriesCount() {
        return 0;
    }

    @Override
    public int indexOf(final Comparable seriesKey) {
        return -1;
    }

    @Override
    public void addChangeListener(final DatasetChangeListener listener) {
    }

    @Override
    public void removeChangeListener(final DatasetChangeListener listener) {
    }

    @Override
    public DatasetGroup getGroup() {
        return null;
    }

    @Override
    public void setGroup(final DatasetGroup group) {
    }

    @Override
    public Number getHigh(final int series, final int item) {
        return null;
    }

    @Override
    public double getHighValue(final int series, final int item) {
        return Double.NaN;
    }

    @Override
    public Number getLow(final int series, final int item) {
        return null;
    }

    @Override
    public double getLowValue(final int series, final int item) {
        return Double.NaN;
    }

    @Override
    public Number getOpen(final int series, final int item) {
        return null;
    }

    @Override
    public double getOpenValue(final int series, final int item) {
        return Double.NaN;
    }

    @Override
    public Number getClose(final int series, final int item) {
        return null;
    }

    @Override
    public double getCloseValue(final int series, final int item) {
        return Double.NaN;
    }

    @Override
    public Number getVolume(final int series, final int item) {
        return null;
    }

    @Override
    public double getVolumeValue(final int series, final int item) {
        return Double.NaN;
    }

    @Override
    public XYPlot getPlot() {
        return null;
    }

    @Override
    public void setPlot(final XYPlot plot) {
    }

    @Override
    public Integer getPrecision() {
        return null;
    }

    @Override
    public void setPrecision(final Integer precision) {
    }

    @Override
    public String getInitialPlotPaneId() {
        return null;
    }

    @Override
    public void setInitialPlotPaneId(final String initialPlotPaneId) {
    }

    @Override
    public String getRangeAxisId() {
        return null;
    }

    @Override
    public void setRangeAxisId(final String rangeAxisId) {
    }

    @Override
    public boolean isLegendValueVisible(final int series, final int item) {
        return false;
    }

    @Override
    public Comparable getSeriesKey(final int series) {
        return null;
    }

    @Override
    public String getSeriesTitle() {
        return null;
    }

    @Override
    public void setSeriesTitle(final String seriesTitle) {
    }

    @Override
    public IIndicatorSeriesProvider getIndicatorSeriesProvider() {
        return null;
    }

    @Override
    public void setIndicatorSeriesProvider(final IIndicatorSeriesProvider indicatorSeriesProvider) {
    }

    @Override
    public void setIndicatorSeriesArguments(final IExpression[] indicatorSeriesArguments) {
    }

    @Override
    public IExpression[] getIndicatorSeriesArguments() {
        return null;
    }

    @Override
    public IExpressionSeriesProvider getExpressionSeriesProvider() {
        return null;
    }

    @Override
    public void setExpressionSeriesProvider(final IExpressionSeriesProvider expressionSeriesProvider) {
    }

    @Override
    public String getExpressionSeriesArguments() {
        return null;
    }

    @Override
    public void setExpressionSeriesArguments(final String expressionSeriesArguments) {
    }

    @Override
    public IndexedDateTimeOHLCDataset getMasterDataset() {
        return null;
    }

}
