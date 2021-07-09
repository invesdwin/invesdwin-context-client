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
import de.invesdwin.util.time.date.FTimeUnit;
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
        final long prevEndTime = (long) dataset.getXValueAsDateTimeEnd(0, item - 1);
        final long endTime = (long) dataset.getXValueAsDateTimeEnd(0, item);
        final String endTimeStr = formatTime(prevEndTime, endTime);
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
        final long prevStartTime = (long) dataset.getXValueAsDateTimeStart(0, item - 1);
        final long startTime = (long) dataset.getXValueAsDateTimeStart(0, item);
        final String startTimeStr = formatTime(prevStartTime, startTime);
        sb.append(startTimeStr);
        final long endTime = (long) dataset.getXValueAsDateTimeEnd(0, item);
        if (endTime != startTime) {
            final String endTimeStr = formatTime(startTime, endTime);
            if (!endTimeStr.equals(startTimeStr)) {
                sb.append(" -> ");
                sb.append(endTimeStr);
            }
        }
        return sb.toString();
    }

    private String formatTime(final long prevTime, final long time) {
        final FDate prevDate = new FDate(prevTime);
        final FDate date = new FDate(time);
        final Range range = domainAxis.getRange();
        final double millis = dataset.getXValueAsDateTimeStart(0, (int) range.getUpperBound())
                - dataset.getXValueAsDateTimeStart(0, (int) range.getLowerBound());
        final Duration duration = new Duration((long) millis, FTimeUnit.MILLISECONDS);
        final DateFormat format;
        if (duration.isLessThan(MILLISECOND_THRESHOLD) && FDates.isSameSecond(date, prevDate)) {
            format = millisecondFormat;
        } else if (duration.isLessThan(SECOND_THRESHOLD) && FDates.isSameMinute(date, prevDate)) {
            format = secondFormat;
        } else if (duration.isLessThan(MINUTE_THRESHOLD) && FDates.isSameDay(date, prevDate)) {
            format = minuteFormat;
        } else {
            format = dateFormat;
        }
        return format.format(date.dateValue());
    }

    @Override
    public Number parse(final String source, final ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }

}