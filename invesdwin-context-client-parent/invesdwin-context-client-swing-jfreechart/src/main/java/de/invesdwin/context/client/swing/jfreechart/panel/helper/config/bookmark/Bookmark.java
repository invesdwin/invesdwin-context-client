package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.bookmark;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.util.lang.ADelegateComparator;
import de.invesdwin.util.time.fdate.FDate;
import de.invesdwin.util.time.range.TimeRange;

@NotThreadSafe
public class Bookmark extends TimeRange {

    public static final ADelegateComparator<Bookmark> COMPARATOR = new ADelegateComparator<Bookmark>() {
        @Override
        protected Comparable<?> getCompareCriteria(final Bookmark e) {
            return e.getLastUsed();
        }
    };

    private final FDate lastUsed;

    public Bookmark(final TimeRange timeRange, final FDate lastUsed) {
        super(timeRange.getFrom(), timeRange.getTo());
        this.lastUsed = lastUsed;
    }

    public FDate getLastUsed() {
        return lastUsed;
    }

}
