package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import javax.annotation.concurrent.Immutable;

@Immutable
public class SlaveLazyDatasetListenerSupport implements ISlaveLazyDatasetListener {

    @Override
    public void append(final int appendCount) {}

    @Override
    public void prepend(final int prependCount) {}

    @Override
    public void loadInitial() {}

    @Override
    public void removeStart(final int tooManyBefore) {}

    @Override
    public void removeEnd(final int tooManyAfter) {}

}
