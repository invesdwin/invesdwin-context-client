package de.invesdwin.context.client.swing.jfreechart.plot.dataset.list;

import de.invesdwin.util.collections.loadingcache.historical.query.error.ResetCacheException;

public interface ISlaveLazyDatasetListener {

    void appendItems(int appendCount) throws ResetCacheException;

    void prependItems(int prependCount) throws ResetCacheException;

    void loadIinitialItems(boolean eager) throws ResetCacheException;

    void removeStartItems(int tooManyBefore) throws ResetCacheException;

    void removeEndItems(int tooManyAfter) throws ResetCacheException;

    void removeMiddleItems(int index, int count) throws ResetCacheException;

    void afterLoadItems(boolean async);

}
