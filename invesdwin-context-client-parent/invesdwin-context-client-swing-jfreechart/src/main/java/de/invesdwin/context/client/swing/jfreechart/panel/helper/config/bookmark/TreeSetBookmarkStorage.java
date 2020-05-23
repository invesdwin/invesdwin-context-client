package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.bookmark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.util.collections.list.ListSet;
import de.invesdwin.util.time.fdate.FDate;

@ThreadSafe
public class TreeSetBookmarkStorage implements IBookmarkStorage {

    private final ListSet<Bookmark> bookmarks = new ListSet<>();

    @Override
    public Collection<Bookmark> getValues() {
        return bookmarks;
    }

    @Override
    public Collection<Bookmark> getRecentlyUsedValues(final int maxCount) {
        final List<Bookmark> result = new ArrayList<>();
        final Iterator<Bookmark> iterator = bookmarks.iterator();
        for (int i = 0; i < maxCount && iterator.hasNext(); i++) {
            final Bookmark next = iterator.next();
            result.add(next);
        }
        return result;
    }

    @Override
    public void addValue(final Bookmark value) {
        bookmarks.remove(value);
        bookmarks.add(0, new Bookmark(value, new FDate()));
    }

    @Override
    public void removeValue(final Bookmark value) {
        bookmarks.remove(value);
    }

}
