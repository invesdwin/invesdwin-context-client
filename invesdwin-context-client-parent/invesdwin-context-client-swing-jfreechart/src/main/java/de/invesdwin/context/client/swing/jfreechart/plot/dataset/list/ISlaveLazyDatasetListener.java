package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

public interface ISlaveLazyDatasetListener {

    void append(int appendCount);

    void prepend(int prependCount);

    void loadInitial();

    void removeStart(int tooManyBefore);

    void removeEnd(int tooManyAfter);

}
