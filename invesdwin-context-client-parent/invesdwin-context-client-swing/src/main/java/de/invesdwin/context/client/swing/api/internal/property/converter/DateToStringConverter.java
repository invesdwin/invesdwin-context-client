package de.invesdwin.context.client.swing.api.internal.property.converter;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.concurrent.Immutable;

import org.jdesktop.beansbinding.Converter;

import de.invesdwin.norva.beanpath.spi.element.IPropertyBeanPathElement;
import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.time.fdate.FDate;

@Immutable
public class DateToStringConverter extends Converter<Object, Object> {

    private final java.text.SimpleDateFormat format;
    private final Class<?> type;

    public DateToStringConverter(final IPropertyBeanPathElement element) {
        final String formatStr = element.getFormatString();
        if (formatStr != null) {
            format = new java.text.SimpleDateFormat(formatStr);
        } else {
            format = new java.text.SimpleDateFormat(FDate.FORMAT_ISO_DATE_TIME_MS);
        }
        this.type = element.getModifier().getBeanClassAccessor().getType().getType();
    }

    @Override
    public Object convertForward(final Object value) {
        if (value == null) {
            return null;
        }
        final Date date;
        if (value instanceof Date) {
            date = (Date) value;
        } else if (value instanceof Calendar) {
            final Calendar cValue = (Calendar) value;
            date = cValue.getTime();
        } else if (value instanceof FDate) {
            final FDate cValue = (FDate) value;
            date = cValue.dateValue();
        } else {
            throw UnknownArgumentException.newInstance(Class.class, value.getClass());
        }
        return format.format(date);
    }

    @Override
    public Object convertReverse(final Object value) {
        if (value == null) {
            return null;
        }
        final String str = (String) value;
        try {
            final Date date = format.parse(str);
            if (type == Date.class) {
                return date;
            } else if (type == Calendar.class) {
                return new FDate(date).calendarValue();
            } else if (type == FDate.class) {
                return new FDate(date);
            } else {
                throw UnknownArgumentException.newInstance(Class.class, type);
            }
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
