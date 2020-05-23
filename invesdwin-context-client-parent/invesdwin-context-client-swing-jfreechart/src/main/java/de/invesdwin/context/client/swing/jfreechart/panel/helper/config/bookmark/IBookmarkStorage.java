package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.bookmark;

import java.util.Collection;

public interface IBookmarkStorage {

    Collection<Bookmark> getValues();

    Collection<Bookmark> getRecentlyUsedValues(int maxCount);

    void addValue(Bookmark value);

    void removeValue(Bookmark value);

}
