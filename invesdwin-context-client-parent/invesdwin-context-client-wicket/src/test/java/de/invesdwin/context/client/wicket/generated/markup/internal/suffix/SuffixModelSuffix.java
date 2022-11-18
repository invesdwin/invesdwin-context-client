package de.invesdwin.context.client.wicket.generated.markup.internal.suffix;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.wicket.generated.markup.internal.CustomModalModel;
import de.invesdwin.norva.beanpath.annotation.Hidden;
import de.invesdwin.nowicket.generated.guiservice.GuiService;
import de.invesdwin.nowicket.generated.markup.annotation.GeneratedMarkup;
import de.invesdwin.util.bean.AValueObject;
import de.invesdwin.util.time.date.FDate;
import jakarta.validation.constraints.NotNull;

@GeneratedMarkup(modelClassNameSuffix = SuffixModelSuffix.MODEL_NAME_SUFFIX)
@NotThreadSafe
public class SuffixModelSuffix extends AValueObject {

    public static final String MODEL_NAME_SUFFIX = "Suffix";

    private TimeUnit someEnum = TimeUnit.DAYS;
    private String one;
    private String two;
    private String three;
    private Date someDate = new FDate().dateValue();
    private Calendar someCalendar = new FDate().calendarValue();

    public SuffixModelSuffix() {
        one = "single";
    }

    public SuffixModelSuffix(final int i) {
        one = i + "-1";
        two = i + "-2";
        three = i + "-3";
    }

    @NotNull
    public String getOne() {
        return one;
    }

    public void setOne(final String one) {
        this.one = one;
    }

    public String getTwo() {
        return two;
    }

    public void setTwo(final String two) {
        this.two = two;
    }

    public String getThree() {
        return three;
    }

    public void setThree(final String three) {
        this.three = three;
    }

    public TimeUnit getSomeEnum() {
        return someEnum;
    }

    public void setSomeEnum(final TimeUnit someEnum) {
        this.someEnum = someEnum;
    }

    public Date getSomeDate() {
        return someDate;
    }

    public void setSomeDate(final Date someDate) {
        this.someDate = someDate;
    }

    public Calendar getSomeCalendar() {
        return someCalendar;
    }

    public void setSomeCalendar(final Calendar someCalendar) {
        this.someCalendar = someCalendar;
    }

    public void someButton() {
        GuiService.get().showModalPanel(new CustomModalModel());
    }

    public void someButtonToo() {}

    public String disableSomeButtonToo() {
        if ("1-1".equals(one)) {
            return "I dont like this row";
        }
        return null;
    }

    @Hidden
    public String title() {
        return "hahahaha";
    }

}
