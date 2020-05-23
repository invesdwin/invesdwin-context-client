package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.bookmark;

import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.util.collections.list.ListSet;

@ThreadSafe
public class TreeSetBookmarkStorage implements IBookmarkStorage {

    private final ListSet<Bookmark> bookmarks = new ListSet<>();

    @Override
    public List<Bookmark> getValues() {
        return bookmarks;
    }

    @Override
    public void putValue(final Bookmark value) {
        bookmarks.remove(value);
        bookmarks.add(0, value);
    }

    @Override
    public void removeValue(final Bookmark value) {
        bookmarks.remove(value);
    }

    @Override
    public void clear() {
        bookmarks.clear();
    }

}
