package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.bookmark;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.util.lang.comparator.ACriteriaComparator;
import de.invesdwin.util.lang.comparator.IComparator;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.util.time.range.TimeRange;

@NotThreadSafe
public class Bookmark extends TimeRange {

    public static final IComparator<Bookmark> COMPARATOR = new ACriteriaComparator<Bookmark>() {
        @Override
        public Comparable<?> getCompareCriteriaNotNullSafe(final Bookmark e) {
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
