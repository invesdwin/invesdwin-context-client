package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.util.time.date.FDate;

public interface IChartPanelAwareDatasetList {

    void setChartPanel(InteractiveChartPanel chartPanel);

    InteractiveChartPanel getChartPanel();

    int getMinLowerBound();

    int getMaxUpperBound();

    void resetRange();

    boolean isTrailingLoaded();

    void reloadData(FDate from, FDate to, Runnable reloadDataFinished);

    void reloadData();

    boolean isLoading();

}
