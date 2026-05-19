package de.invesdwin.context.client.swing.jfreechart.plot;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.Range;

import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IIndexedDateTimeXYDataset;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.util.time.date.FDates;
import de.invesdwin.util.time.duration.Duration;

@NotThreadSafe
public class IndexedDateTimeNumberFormat extends NumberFormat {
    private static final String DATE_TIME_SEPARATOR = " ";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final int THRESHOLD_MULTIPLIER = 10;
    private static final Duration MILLISECOND_THRESHOLD = Duration.ONE_MINUTE.multiply(THRESHOLD_MULTIPLIER);
    private static final Duration SECOND_THRESHOLD = Duration.ONE_HOUR.multiply(THRESHOLD_MULTIPLIER);
    private static final Duration MINUTE_THRESHOLD = Duration.ONE_DAY.multiply(THRESHOLD_MULTIPLIER);
    private final DateFormat dateFormat = new java.text.SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
    private final DateFormat minuteFormat = new java.text.SimpleDateFormat(DATE_FORMAT + DATE_TIME_SEPARATOR + "HH:mm",
            Locale.ENGLISH);
    private final DateFormat secondFormat = new java.text.SimpleDateFormat(
            DATE_FORMAT + DATE_TIME_SEPARATOR + FDate.FORMAT_ISO_TIME, Locale.ENGLISH);
    private final DateFormat millisecondFormat = new java.text.SimpleDateFormat(
            DATE_FORMAT + DATE_TIME_SEPARATOR + FDate.FORMAT_ISO_TIME_MS, Locale.ENGLISH);
    private final IIndexedDateTimeXYDataset dataset;
    private final NumberAxis domainAxis;

    public IndexedDateTimeNumberFormat(final IIndexedDateTimeXYDataset dataset, final NumberAxis domainAxis) {
        this.dataset = dataset;
        this.domainAxis = domainAxis;
    }

    @Override
    public StringBuffer format(final double number, final StringBuffer toAppendTo, final FieldPosition pos) {
        final int item = (int) number;
        final String str = formatItem(item);
        toAppendTo.append(str);
        return toAppendTo;
    }

    private String formatItem(final int item) {
        final FDate prevEndTime = dataset.getXValueAsDateTimeEnd(0, item - 1);
        final FDate endTime = dataset.getXValueAsDateTimeEnd(0, item);
        final boolean lastItem = item == dataset.getItemCount(0) - 1;
        final String endTimeStr = formatTime(prevEndTime, endTime, lastItem);
        return endTimeStr;
    }

    @Override
    public StringBuffer format(final long number, final StringBuffer toAppendTo, final FieldPosition pos) {
        final int item = (int) number;
        final String str = formatItem(item);
        toAppendTo.append(str);
        return toAppendTo;
    }

    public String formatFromTo(final int item) {
        final StringBuilder sb = new StringBuilder();
        final FDate prevStartTime = dataset.getXValueAsDateTimeStart(0, item - 1);
        final FDate startTime = dataset.getXValueAsDateTimeStart(0, item);
        final boolean lastItem = item == dataset.getItemCount(0) - 1;
        final String startTimeStr = formatTime(prevStartTime, startTime, lastItem);
        sb.append(startTimeStr);
        final FDate endTime = dataset.getXValueAsDateTimeEnd(0, item);
        if (!endTime.equalsNotNullSafe(startTime)) {
            final String endTimeStr = formatTime(startTime, endTime, lastItem);
            if (!endTimeStr.equals(startTimeStr)) {
                sb.append(" -> ");
                sb.append(endTimeStr);
            }
        }
        return sb.toString();
    }

    private String formatTime(final FDate prevTime, final FDate time, final boolean lastItem) {
        final Range range = domainAxis.getRange();
        final FDate from = dataset.getXValueAsDateTimeStart(0, (int) range.getLowerBound());
        final FDate to = dataset.getXValueAsDateTimeStart(0, (int) range.getUpperBound());
        final Duration duration = new Duration(from, to);
        final DateFormat format;
        if (lastItem) {
            format = getFallbackTimeFormat(time);
        } else {
            if (duration.isLessThan(MILLISECOND_THRESHOLD) && FDates.isSameSecond(time, prevTime)) {
                format = millisecondFormat;
            } else if (duration.isLessThan(SECOND_THRESHOLD) && FDates.isSameMinute(time, prevTime)) {
                format = secondFormat;
            } else if (duration.isLessThan(MINUTE_THRESHOLD) && FDates.isSameJulianDay(time, prevTime)) {
                format = minuteFormat;
            } else if (time.isWithoutTime()) {
                format = dateFormat;
            } else {
                format = getFallbackTimeFormat(time);
            }
        }
        return format.format(time.dateValue());
    }

    private DateFormat getFallbackTimeFormat(final FDate date) {
        if (date.getMillisecond() != 0) {
            return millisecondFormat;
        } else if (date.getSecond() != 0) {
            return secondFormat;
        } else if (date.getMinute() != 0) {
            return minuteFormat;
        } else if (date.getHour() == 0) {
            return dateFormat;
        } else {
            return minuteFormat;
        }
    }

    @Override
    public Number parse(final String source, final ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }

}