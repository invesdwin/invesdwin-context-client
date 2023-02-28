package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

public interface ISlaveLazyDatasetListener {

    void appendItems(int appendCount);

    void maybeUpdateOnIdleAppendItems();

    void prependItems(int prependCount);

    void loadIinitialItems(boolean eager);

    void removeStartItems(int tooManyBefore);

    void removeEndItems(int tooManyAfter);

    void removeMiddleItems(int index, int count);

    void afterLoadItems(boolean async);

}
