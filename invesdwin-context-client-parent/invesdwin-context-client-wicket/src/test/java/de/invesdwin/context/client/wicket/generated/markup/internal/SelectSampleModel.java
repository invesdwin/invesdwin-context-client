package de.invesdwin.context.client.wicket.generated.markup.internal;

import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import de.invesdwin.norva.beanpath.annotation.Disabled;
import de.invesdwin.nowicket.generated.binding.annotation.ModalCloser;
import de.invesdwin.nowicket.generated.markup.annotation.GeneratedMarkup;
import de.invesdwin.util.bean.AValueObject;

@GeneratedMarkup
@NotThreadSafe
public class SelectSampleModel extends AValueObject {

    private TimeUnit dropDownDisabled;

    private TimeUnit dropDownEnabled;

    private TimeUnit listDisabled;

    private TimeUnit listEnabled;

    @Disabled("some drop down reason")
    @NotNull
    public TimeUnit getDropDownDisabled() {
        return dropDownDisabled;
    }

    public void setDropDownDisabled(final TimeUnit dropDownDisabled) {
        this.dropDownDisabled = dropDownDisabled;
    }

    @NotNull
    public TimeUnit getDropDownEnabled() {
        return dropDownEnabled;
    }

    public void setDropDownEnabled(final TimeUnit dropDownEnabled) {
        this.dropDownEnabled = dropDownEnabled;
    }

    @Disabled("some list reason")
    @NotNull
    public TimeUnit getListDisabled() {
        return listDisabled;
    }

    public void setListDisabled(final TimeUnit listDisabled) {
        this.listDisabled = listDisabled;
    }

    @NotNull
    public TimeUnit getListEnabled() {
        return listEnabled;
    }

    public void setListEnabled(final TimeUnit listEnabled) {
        this.listEnabled = listEnabled;
    }

    public void submit() {

    }

    @ModalCloser
    public void close() {

    }

}
