package de.invesdwin.context.client.swing.api.binding.converter;

import java.text.ParseException;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.norva.beanpath.spi.element.IPropertyBeanPathElement;
import de.invesdwin.util.lang.string.Strings;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.util.time.date.format.FDateTimeFormatter;

@Immutable
public class DateToStringConverter implements IConverter<Object, String> {

    private final FDateTimeFormatter format;
    private final Class<?> type;

    public DateToStringConverter(final IPropertyBeanPathElement element) {
        final String formatStr = element.getFormatString();
        if (formatStr != null) {
            format = FDateTimeFormatter.forPattern(formatStr);
        } else {
            format = FDateTimeFormatter.forPattern(FDate.FORMAT_ISO_DATE_TIME_PS);
        }
        this.type = element.getModifier().getBeanClassAccessor().getType().getType();
    }

    @Override
    public String fromModelToComponent(final Object value) {
        if (value == null) {
            return null;
        }
        return format.print(value);
    }

    @Override
    public Object fromComponentToModel(final String value) throws ParseException {
        if (Strings.isBlank(value)) {
            return null;
        }
        return format.parse(type, value);
    }
}
