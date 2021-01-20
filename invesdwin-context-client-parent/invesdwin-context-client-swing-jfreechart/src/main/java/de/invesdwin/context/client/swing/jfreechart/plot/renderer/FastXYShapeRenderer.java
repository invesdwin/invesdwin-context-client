package de.invesdwin.context.client.swing.jfreechart.plot.renderer;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.IDelegatePriceLineXYItemRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.IPriceLineRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.priceline.XYPriceLineAnnotation;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.shape.CustomXYShapeRenderer;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.shape.ISeriesShapeFactory;

@NotThreadSafe
public class FastXYShapeRenderer extends CustomXYShapeRenderer implements IDelegatePriceLineXYItemRenderer {

    private final IPlotSourceDataset dataset;
    private final XYPriceLineAnnotation priceLineAnnotation;

    public FastXYShapeRenderer(final IPlotSourceDataset dataset, final ISeriesShapeFactory seriesShapeFactory) {
        super(seriesShapeFactory);
        this.dataset = dataset;
        this.priceLineAnnotation = new XYPriceLineAnnotation(dataset, this);
        addAnnotation(priceLineAnnotation);
    }

    @Override
    public IPlotSourceDataset getDataset() {
        return dataset;
    }

    @Override
    public IPriceLineRenderer getDelegatePriceLineRenderer() {
        return priceLineAnnotation;
    }

}
