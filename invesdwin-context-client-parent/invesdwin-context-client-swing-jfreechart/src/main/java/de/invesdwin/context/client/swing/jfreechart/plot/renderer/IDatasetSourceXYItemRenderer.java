package de.invesdwin.context.client.swing.jfreechart.plot.renderer;

import org.jfree.chart.renderer.xy.XYItemRenderer;

import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;

public interface IDatasetSourceXYItemRenderer extends XYItemRenderer {

    IPlotSourceDataset getDataset();

}
