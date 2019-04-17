package de.invesdwin.context.client.swing.jfreechart.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Stroke;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.OHLCDataItem;

import de.invesdwin.context.client.swing.jfreechart.panel.basis.CustomCombinedDomainXYPlot;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.LineStyleType;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.LineWidthType;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.SeriesRendererType;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.SeriesParameterType;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.expression.IExpressionSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.indicator.IIndicatorSeriesParameter;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.indicator.IIndicatorSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.plot.XYPlots;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IndexedDateTimeOHLCDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IndexedDateTimeXYSeries;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.PlotSourceXYSeriesCollection;
import de.invesdwin.context.jfreechart.dataset.XYDataItemOHLC;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.lang.UniqueNameGenerator;
import de.invesdwin.util.math.Integers;
import de.invesdwin.util.math.expression.ExpressionParser;
import de.invesdwin.util.math.expression.IExpression;
import de.invesdwin.util.math.expression.eval.BooleanExpression;
import de.invesdwin.util.math.expression.eval.ConstantExpression;
import de.invesdwin.util.math.expression.eval.EnumerationExpression;
import de.invesdwin.util.time.fdate.FDate;

@NotThreadSafe
public class CandlestickDemo extends JFrame {

    private static final String PRICE_PLOT_PANE_ID = "Price";
    private static final UniqueNameGenerator SERIES_ID_GENERATOR = new UniqueNameGenerator();

    public CandlestickDemo() {
        super("CandlestickDemo");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final InteractiveChartPanel chartPanel = new InteractiveChartPanel(getDataSet());
        chartPanel.setPreferredSize(new Dimension(1280, 800));
        add(chartPanel);
        chartPanel.getPlotConfigurationHelper().putIndicatorSeriesProvider(new CustomIndicatorSeriesProvider());
        chartPanel.getPlotConfigurationHelper().putIndicatorSeriesProvider(new ThrowExceptionIndicatorSeriesProvider());

        chartPanel.getPlotConfigurationHelper().setExpressionSeriesProvider(new CustomExpressionSeriesProvider());
        this.pack();
    }

    protected IndexedDateTimeOHLCDataset getDataSet() {

        //This is where we go get the data, replace with your own data source
        final List<OHLCDataItem> data = getData();

        //Create a dataset, an Open, High, Low, Close dataset
        final IndexedDateTimeOHLCDataset result = new IndexedDateTimeOHLCDataset("MSFT", data);
        result.setPrecision(2);
        result.setRangeAxisId(PRICE_PLOT_PANE_ID);

        return result;
    }

    //This method uses yahoo finance to get the OHLC data
    protected List<OHLCDataItem> getData() {
        final List<OHLCDataItem> dataItems = new ArrayList<OHLCDataItem>();
        try {
            /*
             * String strUrl=
             * "https://query1.finance.yahoo.com/v7/finance/download/MSFT?period1=1493801037&period2=1496479437&interval=1d&events=history&crumb=y/oR8szwo.9";
             */
            final File f = new File("src/test/java/" + CandlestickDemo.class.getPackage().getName().replace(".", "/")
                    + "/MSFTlong.csv");
            final BufferedReader in = new BufferedReader(new FileReader(f));

            final DateFormat df = new java.text.SimpleDateFormat("y-M-d");

            String inputLine;
            in.readLine();
            while ((inputLine = in.readLine()) != null) {
                final StringTokenizer st = new StringTokenizer(inputLine, ",");

                final Date date = df.parse(st.nextToken());
                final double open = Double.parseDouble(st.nextToken());
                final double high = Double.parseDouble(st.nextToken());
                final double low = Double.parseDouble(st.nextToken());
                final double close = Double.parseDouble(st.nextToken());
                final double adjClose = Double.parseDouble(st.nextToken());
                final double volume = Double.parseDouble(st.nextToken());

                final OHLCDataItem item = new OHLCDataItem(date, open, high, low, adjClose, volume);
                dataItems.add(item);
            }
            in.close();
        } catch (final Exception e) {
            throw Err.process(e);
        }
        return dataItems;
    }

    //CHECKSTYLE:OFF
    public static void main(final String[] args) {
        System.setProperty("jdk.gtk.version", "2");
        //CHECKSTYLE:ON
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
        new CandlestickDemo().setVisible(true);
    }

    private final class CustomExpressionSeriesProvider implements IExpressionSeriesProvider {
        @Override
        public IPlotSourceDataset newInstance(final InteractiveChartPanel chartPanel, final String expression) {
            final Stroke stroke = chartPanel.getPlotConfigurationHelper().getPriceInitialSettings().getSeriesStroke();
            final LineStyleType lineStyleType = LineStyleType.valueOf(stroke);
            final LineWidthType lineWidthType = LineWidthType.valueOf(stroke);
            final Color color = Color.GREEN;
            final boolean priceLineVisible = false;
            final boolean priceLabelVisible = false;

            final String seriesId = SERIES_ID_GENERATOR.get(getPlotPaneId());
            final PlotSourceXYSeriesCollection dataset = new PlotSourceXYSeriesCollection(seriesId);
            dataset.setSeriesTitle(expression);
            final XYPlot plot = chartPanel.getOhlcPlot();
            dataset.setPlot(plot);
            dataset.setPrecision(4);
            dataset.setRangeAxisId(getPlotPaneId());
            final IndexedDateTimeXYSeries series = newSeriesPrefilled(chartPanel, expression, seriesId);
            final int datasetIndex = plot.getDatasetCount();
            dataset.addSeries(series);
            final SeriesRendererType seriesRendererType = SeriesRendererType.Line;
            final XYItemRenderer renderer = seriesRendererType.newRenderer(dataset, lineStyleType, lineWidthType, color,
                    priceLineVisible, priceLabelVisible);
            plot.setDataset(datasetIndex, dataset);
            plot.setRenderer(datasetIndex, renderer);
            XYPlots.updateRangeAxes(plot);
            chartPanel.update();

            if (!chartPanel.getCombinedPlot().isSubplotVisible(plot)) {
                chartPanel.getCombinedPlot().add(plot, CustomCombinedDomainXYPlot.INITIAL_PLOT_WEIGHT);
            }

            return dataset;
        }

        @Override
        public void modifyDataset(final InteractiveChartPanel chartPanel, final IPlotSourceDataset dataset,
                final String expression) {
            final PlotSourceXYSeriesCollection cDataset = (PlotSourceXYSeriesCollection) dataset;
            final String seriesId = dataset.getSeriesId();
            final IndexedDateTimeXYSeries newSeriesPrefilled = newSeriesPrefilled(chartPanel, expression, seriesId);
            cDataset.setSeriesTitle(expression);
            cDataset.setNotify(false);
            cDataset.removeAllSeries();
            cDataset.addSeries(newSeriesPrefilled);
            cDataset.setNotify(true);
        }

        private IndexedDateTimeXYSeries newSeriesPrefilled(final InteractiveChartPanel chartPanel,
                final String expressionStr, final String seriesId) {
            final IndexedDateTimeXYSeries series = new IndexedDateTimeXYSeries(seriesId);

            final IExpression expression = parseExpression(expressionStr);
            final List<XYDataItemOHLC> list = series.getData();
            final List<OHLCDataItem> ohlc = chartPanel.getDataset().getData();
            for (int i = 0; i < ohlc.size(); i++) {
                final FDate time = new FDate(ohlc.get(i).getDate());
                final double value = expression.evaluateDouble(time);
                final XYDataItemOHLC item = new XYDataItemOHLC(
                        new OHLCDataItem(time.dateValue(), Double.NaN, Double.NaN, Double.NaN, value, Double.NaN));
                final int index = list.size();
                final double xValueAsDateTime = chartPanel.getDataset().getXValueAsDateTime(0, index);
                if (xValueAsDateTime != item.getXValue()) {
                    throw new IllegalStateException(
                            "Async at index [" + index + "]: ohlc[" + new FDate((long) xValueAsDateTime)
                                    + "] != series[" + new FDate((long) item.getXValue()) + "]");
                }
                list.add(item);
                series.updateBoundsForAddedItem(item);
            }
            return series;
        }

        @Override
        public IExpression parseExpression(final String expression) {
            return new ExpressionParser(expression).parse();
        }

        @Override
        public String getPlotPaneId() {
            return "Expression";
        }
    }

    private final class ThrowExceptionIndicatorSeriesProvider implements IIndicatorSeriesProvider {
        @Override
        public IPlotSourceDataset newInstance(final InteractiveChartPanel chartPanel, final IExpression[] args) {
            throw new RuntimeException("This should be displayed in a dialog.");
        }

        @Override
        public String getPlotPaneId() {
            return PRICE_PLOT_PANE_ID;
        }

        @Override
        public IIndicatorSeriesParameter[] getParameters() {
            return NO_PARAMETERS;
        }

        @Override
        public String getName() {
            return "throw exception";
        }

        @Override
        public String getExpressionName() {
            return "throwException";
        }

        @Override
        public String getDescription() {
            return "handles the exception";
        }

        @Override
        public void modifyDataset(final InteractiveChartPanel chartPanel, final IPlotSourceDataset dataset,
                final IExpression[] args) {
            throw new RuntimeException("This should never happen.");
        }
    }

    private final class CustomIndicatorSeriesProvider implements IIndicatorSeriesProvider {
        @Override
        public IPlotSourceDataset newInstance(final InteractiveChartPanel chartPanel, final IExpression[] args) {
            final Stroke stroke = chartPanel.getPlotConfigurationHelper().getPriceInitialSettings().getSeriesStroke();
            final LineStyleType lineStyleType = LineStyleType.valueOf(stroke);
            final LineWidthType lineWidthType = LineWidthType.valueOf(stroke);
            final Color color = Color.GREEN;
            final boolean priceLineVisible = false;
            final boolean priceLabelVisible = false;

            final PlotSourceXYSeriesCollection dataset = new PlotSourceXYSeriesCollection(getExpressionString(args));
            final XYPlot plot = chartPanel.getOhlcPlot();
            dataset.setPlot(plot);
            dataset.setPrecision(4);
            dataset.setRangeAxisId(getPlotPaneId());
            final String seriesId = SERIES_ID_GENERATOR.get(getPlotPaneId());
            final IndexedDateTimeXYSeries series = newSeriesPrefilled(chartPanel, args, seriesId);
            final int datasetIndex = plot.getDatasetCount();
            dataset.addSeries(series);
            final SeriesRendererType seriesRendererType = SeriesRendererType.Line;
            final XYItemRenderer renderer = seriesRendererType.newRenderer(dataset, lineStyleType, lineWidthType, color,
                    priceLineVisible, priceLabelVisible);
            plot.setDataset(datasetIndex, dataset);
            plot.setRenderer(datasetIndex, renderer);
            XYPlots.updateRangeAxes(plot);
            chartPanel.update();

            if (!chartPanel.getCombinedPlot().isSubplotVisible(plot)) {
                chartPanel.getCombinedPlot().add(plot, CustomCombinedDomainXYPlot.INITIAL_PLOT_WEIGHT);
            }

            return dataset;
        }

        private IndexedDateTimeXYSeries newSeriesPrefilled(final InteractiveChartPanel chartPanel,
                final IExpression[] args, final String seriesId) {
            Assertions.checkEquals(4, args.length);
            final boolean invertAddition = args[0].evaluateBoolean();
            final int lagBars = args[1].evaluateInteger();
            Assertions.assertThat(lagBars).isNotNegative();
            double addition = args[2].evaluateDouble();
            if (invertAddition) {
                addition = -addition;
            }
            final OhlcValueType ohlcValueType = OhlcValueType.parseString(args[3].toString());

            final IndexedDateTimeXYSeries series = new IndexedDateTimeXYSeries(getExpressionName());

            final List<XYDataItemOHLC> list = series.getData();
            final List<OHLCDataItem> ohlc = chartPanel.getDataset().getData();
            for (int i = 0; i < ohlc.size(); i++) {
                final FDate time = new FDate(ohlc.get(i).getDate());
                final int lagIndex = Integers.max(i - lagBars, 0);
                final OHLCDataItem ohlcItem = ohlc.get(lagIndex);
                final double value = ohlcValueType.getValue(ohlcItem) + addition;
                final XYDataItemOHLC item = new XYDataItemOHLC(
                        new OHLCDataItem(time.dateValue(), Double.NaN, Double.NaN, Double.NaN, value, Double.NaN));
                final int index = list.size();
                final double xValueAsDateTime = chartPanel.getDataset().getXValueAsDateTime(0, index);
                if (xValueAsDateTime != item.getXValue()) {
                    throw new IllegalStateException(
                            "Async at index [" + index + "]: ohlc[" + new FDate((long) xValueAsDateTime)
                                    + "] != series[" + new FDate((long) item.getXValue()) + "]");
                }
                list.add(item);
                series.updateBoundsForAddedItem(item);
            }
            return series;
        }

        @Override
        public void modifyDataset(final InteractiveChartPanel chartPanel, final IPlotSourceDataset dataset,
                final IExpression[] args) {
            final PlotSourceXYSeriesCollection cDataset = (PlotSourceXYSeriesCollection) dataset;
            final String seriesId = dataset.getSeriesId();
            final IndexedDateTimeXYSeries newSeriesPrefilled = newSeriesPrefilled(chartPanel, args, seriesId);
            cDataset.setNotify(false);
            cDataset.removeAllSeries();
            cDataset.addSeries(newSeriesPrefilled);
            cDataset.setNotify(true);
        }

        @Override
        public String getPlotPaneId() {
            return PRICE_PLOT_PANE_ID;
        }

        @Override
        public IIndicatorSeriesParameter[] getParameters() {
            return new IIndicatorSeriesParameter[] { new IIndicatorSeriesParameter() {

                @Override
                public SeriesParameterType getType() {
                    return SeriesParameterType.Boolean;
                }

                @Override
                public String getName() {
                    return "Invert Addition";
                }

                @Override
                public IExpression[] getEnumerationValues() {
                    return null;
                }

                @Override
                public String getDescription() {
                    return "boolean description";
                }

                @Override
                public IExpression getDefaultValue() {
                    return new BooleanExpression(false);
                }
            }, new IIndicatorSeriesParameter() {

                @Override
                public SeriesParameterType getType() {
                    return SeriesParameterType.Integer;
                }

                @Override
                public String getName() {
                    return "Lag Bars";
                }

                @Override
                public IExpression[] getEnumerationValues() {
                    return null;
                }

                @Override
                public String getDescription() {
                    return "integer description";
                }

                @Override
                public IExpression getDefaultValue() {
                    return new ConstantExpression(0);
                }
            }, new IIndicatorSeriesParameter() {

                @Override
                public SeriesParameterType getType() {
                    return SeriesParameterType.Double;
                }

                @Override
                public String getName() {
                    return "Addition";
                }

                @Override
                public IExpression[] getEnumerationValues() {
                    return null;
                }

                @Override
                public String getDescription() {
                    return "double description";
                }

                @Override
                public IExpression getDefaultValue() {
                    return new ConstantExpression(0);
                }
            }, new IIndicatorSeriesParameter() {

                @Override
                public SeriesParameterType getType() {
                    return SeriesParameterType.Enumeration;
                }

                @Override
                public String getName() {
                    return "OHLC Value";
                }

                @Override
                public IExpression[] getEnumerationValues() {
                    return EnumerationExpression.valueOf(OhlcValueType.values());
                }

                @Override
                public String getDescription() {
                    return "enum description";
                }

                @Override
                public IExpression getDefaultValue() {
                    return EnumerationExpression.valueOf(OhlcValueType.Close);
                }
            } };
        }

        @Override
        public String getName() {
            return "name";
        }

        @Override
        public String getExpressionName() {
            return "expression";
        }

        @Override
        public String getDescription() {
            return "description";
        }
    }

    private enum OhlcValueType {
        Open {
            @Override
            public double getValue(final OHLCDataItem item) {
                return item.getOpen().doubleValue();
            }
        },
        High {
            @Override
            public double getValue(final OHLCDataItem item) {
                return item.getHigh().doubleValue();
            }
        },
        Low {
            @Override
            public double getValue(final OHLCDataItem item) {
                return item.getLow().doubleValue();
            }
        },
        Close {
            @Override
            public double getValue(final OHLCDataItem item) {
                return item.getClose().doubleValue();
            }
        };

        public abstract double getValue(OHLCDataItem item);

        public static OhlcValueType parseString(final String str) {
            final String strClean = str.trim().toLowerCase();
            switch (strClean) {
            case "open":
                return OhlcValueType.Open;
            case "high":
                return OhlcValueType.High;
            case "low":
                return OhlcValueType.Low;
            case "close":
                return OhlcValueType.Close;
            default:
                throw UnknownArgumentException.newInstance(String.class, strClean);
            }
        }
    }
}