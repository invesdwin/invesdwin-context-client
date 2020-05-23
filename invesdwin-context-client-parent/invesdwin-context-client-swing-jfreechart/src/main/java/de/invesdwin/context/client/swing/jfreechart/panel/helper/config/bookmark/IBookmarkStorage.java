package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.bookmark;

import java.util.List;

public interface IBookmarkStorage {

    List<Bookmark> getValues();

    void addValue(Bookmark value);

    void removeValue(Bookmark value);

}
