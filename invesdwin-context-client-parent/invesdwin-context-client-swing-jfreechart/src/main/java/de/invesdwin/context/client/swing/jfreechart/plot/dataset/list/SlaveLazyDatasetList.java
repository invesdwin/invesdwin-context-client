package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.jfreechart.dataset.XYDataItemOHLC;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.time.fdate.FDate;

@NotThreadSafe
public class SlaveLazyDatasetList extends ALazyDatasetList<XYDataItemOHLC> implements ISlaveLazyDatasetListener {

    private final ISlaveLazyDatasetProvider provider;
    private final MasterLazyDatasetList master;

    public SlaveLazyDatasetList(final InteractiveChartPanel chartPanel, final ISlaveLazyDatasetProvider provider) {
        this.provider = provider;
        this.master = (MasterLazyDatasetList) chartPanel.getDataset().getData();
        master.registerSlaveDatasetListener(this);
    }

    @Override
    public void append(final int appendCount) {
        final int masterSizeAfter = master.size();
        final int masterSizeBefore = masterSizeAfter - appendCount;
        final int fromIndex = masterSizeBefore;
        final int toIndex = masterSizeAfter - 1;
        while (data.size() > masterSizeBefore) {
            data.remove(data.size() - 1);
        }
        int countAdded = 0;
        for (int i = fromIndex; i <= toIndex; i++) {
            final FDate key = FDate.valueOf(master.get(i).getDate());
            final XYDataItemOHLC next = provider.getValue(key);
            data.add(next);
            countAdded++;
        }
        Assertions.checkEquals(countAdded, appendCount);
        Assertions.checkEquals(masterSizeAfter, data.size());
    }

    @Override
    public void prepend(final int prependCount) {
        final List<XYDataItemOHLC> prependItems = new ArrayList<>(prependCount);
        for (int i = 0; i < prependCount; i++) {
            final FDate key = FDate.valueOf(master.get(i).getDate());
            final XYDataItemOHLC value = provider.getValue(key);
            prependItems.add(value);
        }
        data.addAll(0, prependItems);
        Assertions.checkEquals(master.size(), data.size());
    }

    @Override
    public void loadInitial() {
        data = new ArrayList<>(data.size());
        for (int i = 0; i < master.size(); i++) {
            final FDate key = FDate.valueOf(master.get(i).getDate());
            final XYDataItemOHLC value = provider.getValue(key);
            data.add(value);
        }
    }

    @Override
    public void removeStart(final int tooManyBefore) {
        for (int i = 0; i < tooManyBefore; i++) {
            data.remove(0);
        }
        Assertions.checkEquals(master.size(), data.size());
    }

    @Override
    public void removeEnd(final int tooManyAfter) {
        for (int i = 0; i < tooManyAfter; i++) {
            data.remove(data.size() - 1);
        }
        Assertions.checkEquals(master.size(), data.size());
    }

}
