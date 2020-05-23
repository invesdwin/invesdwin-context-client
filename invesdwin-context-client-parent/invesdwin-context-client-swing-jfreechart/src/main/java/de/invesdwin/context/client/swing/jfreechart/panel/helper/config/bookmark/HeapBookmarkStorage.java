package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.bookmark;

import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.util.collections.list.ListSet;

@ThreadSafe
public class HeapBookmarkStorage implements IBookmarkStorage {

    private final ListSet<Bookmark> bookmarks = new ListSet<>();

    @Override
    public synchronized List<Bookmark> getValues() {
        return bookmarks;
    }

    @Override
    public synchronized void putValue(final Bookmark value) {
        bookmarks.remove(value);
        bookmarks.add(0, value);
    }

    @Override
    public synchronized void removeValue(final Bookmark value) {
        bookmarks.remove(value);
    }

    @Override
    public synchronized void clear() {
        bookmarks.clear();
    }

}
