package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;

public interface IChartPanelAwareDatasetList {

    void setChartPanel(InteractiveChartPanel chartPanel);

    int getMinLowerBound();

    int getMaxUpperBound();

    void resetRange();

}
