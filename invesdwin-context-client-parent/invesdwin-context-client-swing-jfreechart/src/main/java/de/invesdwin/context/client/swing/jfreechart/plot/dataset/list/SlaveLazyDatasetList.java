package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.jfreechart.dataset.XYDataItemOHLC;

@NotThreadSafe
public class SlaveLazyDatasetList extends ALazyDatasetList<XYDataItemOHLC> {

    private final ISlaveLazyDatasetProvider provider;

    public SlaveLazyDatasetList(final InteractiveChartPanel chartPanel, final ISlaveLazyDatasetProvider provider) {
        this.provider = provider;
        final MasterLazyDatasetList master = (MasterLazyDatasetList) chartPanel.getDataset().getData();
        master.registerSlave(this);
    }

    public ISlaveLazyDatasetProvider getProvider() {
        return provider;
    }

    public List<XYDataItemOHLC> getData() {
        return data;
    }

    public void setData(final List<XYDataItemOHLC> data) {
        this.data = data;
    }

}
