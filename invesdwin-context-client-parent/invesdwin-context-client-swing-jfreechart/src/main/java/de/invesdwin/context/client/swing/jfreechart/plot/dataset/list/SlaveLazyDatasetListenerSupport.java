package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import javax.annotation.concurrent.Immutable;

@Immutable
public class SlaveLazyDatasetListenerSupport implements ISlaveLazyDatasetListener {

    @Override
    public void removeStartItems(final int tooManyBefore) {}

    @Override
    public void removeMiddleItems(final int index, final int count) {}

    @Override
    public void removeEndItems(final int tooManyAfter) {}

    @Override
    public void appendItems(final int appendCount) {}

    @Override
    public void prependItems(final int prependCount) {}

    @Override
    public void loadIinitialItems(final boolean eager) {}

    @Override
    public void afterLoadSlaveItems() {}

}
