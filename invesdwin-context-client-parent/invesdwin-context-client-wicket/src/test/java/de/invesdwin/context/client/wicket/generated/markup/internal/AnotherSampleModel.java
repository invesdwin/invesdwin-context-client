package de.invesdwin.context.client.wicket.generated.markup.internal;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotNull;

import de.invesdwin.context.client.wicket.generated.markup.internal.bugfix.Bugfix;
import de.invesdwin.context.client.wicket.generated.markup.internal.removefrom.BugfixRemoveFrom;
import de.invesdwin.context.client.wicket.generated.markup.internal.suffix.SuffixModelSuffix;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.norva.beanpath.annotation.Disabled;
import de.invesdwin.norva.beanpath.annotation.Eager;
import de.invesdwin.norva.beanpath.annotation.Forced;
import de.invesdwin.norva.beanpath.annotation.ModalCloser;
import de.invesdwin.nowicket.component.modal.panel.ModalMessage;
import de.invesdwin.nowicket.generated.guiservice.GuiService;
import de.invesdwin.nowicket.generated.markup.annotation.GeneratedMarkup;
import de.invesdwin.util.bean.AValueObject;
import de.invesdwin.util.time.fdate.FDate;

@GeneratedMarkup
@NotThreadSafe
public class AnotherSampleModel extends AValueObject {

    private String someProperty = "asdf";
    private String someComboBoxArray = "haha";

    private final List<SampleModel> rows;
    private SampleModel someTableArray;
    private Date someDate = new FDate().dateValue();
    private Calendar someCalendar = new FDate().calendarValue();
    private boolean somePrimitiveBoolean;
    private Boolean someBoolean;

    public AnotherSampleModel() {
        rows = new ArrayList<SampleModel>();
        for (int i = 0; i < 100; i++) {
            rows.add(new SampleModel(i));
        }
        GuiService.get().showStatusMessage("bla bla bla");
        GuiService.get().showStatusMessage("too too too");
    }

    @NotNull
    public String getSomeProperty() {
        return someProperty;
    }

    @Eager
    public void setSomeProperty(final String someProperty) {
        this.someProperty = someProperty;
    }

    @Disabled("I dont like dates")
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

    public String disableSomeCalendar() {
        if ("hello".equals(someProperty)) {
            return "hello typed in someProperty";
        } else {
            return null;
        }
    }

    public List<SampleModel> getSomeTableList() {
        return rows;
    }

    public SampleModel[] getSomeTableArrayChoice() {
        return rows.toArray(new SampleModel[0]);
    }

    public SampleModel getSomeTableArray() {
        return someTableArray;
    }

    public void setSomeTableArray(final SampleModel someTableArray) {
        this.someTableArray = someTableArray;
    }

    public String[] getSomeComboBoxArrayChoice() {
        return new String[] { "one", "two", "many" };
    }

    //    @Disabled("some other reason")
    public String getSomeComboBoxArray() {
        return someComboBoxArray;
    }

    public void setSomeComboBoxArray(final String someComboBoxArray) {
        this.someComboBoxArray = someComboBoxArray;
    }

    public Boolean getSomeBoolean() {
        return someBoolean;
    }

    @Eager
    public void setSomeBoolean(final Boolean someBoolean) {
        this.someBoolean = someBoolean;
    }

    public String validateSomeBoolean(final Boolean newValue) {
        if (newValue == Boolean.TRUE) {
            return "true is not sooooo good";
        }
        return null;
    }

    public boolean isSomePrimitiveBoolean() {
        return somePrimitiveBoolean;
    }

    public void setSomePrimitiveBoolean(final boolean somePrimitiveBoolean) {
        this.somePrimitiveBoolean = somePrimitiveBoolean;
    }

    public String getSomePrimitiveBooleanTitle() {
        return "some primitive boolean title";
    }

    public void someButton() {
        GuiService.get().showStatusMessage("some button status message");
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (final InterruptedException e) {
            throw Err.process(e);
        }
    }

    public void showModalPanel() {
        GuiService.get().showModalPanel(new ModalMessage("some title", "some modal message") {

            private int getMessageCalls = 0;
            private int titleCalls = 0;
            private String okTitle = "Open Nested Modal";
            private boolean closeThisTime = false;

            //            @ModalCloser(/* disabling modal closer by commenting it, overriden method is the only source for annotations */)
            @Override
            public void ok() {
                super.ok();
                if (closeThisTime) {
                    GuiService.get().hideModalPanel();
                } else {
                    GuiService.get().showModalPanel(new ModalMessage("some nested title", "some nested modal message") {
                        @Override
                        @ModalCloser
                        public void ok() {
                            super.ok();
                            okTitle = "Close this time";
                            closeThisTime = true;
                        }
                    });
                }
            }

            @Override
            public String getMessage() {
                return super.getMessage() + " " + getMessageCalls++;
            }

            @SuppressWarnings("unused")
            public String okTitle() {
                return okTitle;
            }

            @Override
            public String title() {
                return super.title() + " " + titleCalls++;
            }

        });
    }

    public String someButtonTooltip() {
        return "hahahahaha tooltip dynamic";
    }

    public String someButtonTitle() {
        return "some dynamic title";
    }

    public RedirectedModel redirectButton() {
        return new RedirectedModel(this);
    }

    public void openCustomModal() {
        GuiService.get().showModalPanel(new CustomModalModel());
    }

    @Forced
    public void forcedButton() {
        GuiService.get().showModalPanel(new ModalMessage("forced button worked!"));
    }

    public void showUnorderedListModal() {
        GuiService.get().showModalPanel(new UnorderedListModel(), new Dimension(800, 600));
    }

    @Forced
    public void reset() {
        this.someProperty = "asdf";
        this.someComboBoxArray = "haha";
        this.someTableArray = null;
        this.someDate = new FDate().dateValue();
        this.someCalendar = new FDate().calendarValue();
        this.somePrimitiveBoolean = false;
        this.someBoolean = null;
    }

    public void throwCheckedExceptionWithMessage() throws Exception {
        throw new Exception("asdf");
    }

    public void throwCheckedExceptionWithoutMessage() throws Exception {
        throw new Exception();
    }

    public void throwUncheckedExceptionWithMessage() {
        throw new RuntimeException("asdf");
    }

    public void throwUncheckedExceptionWithoutMessage() {
        throw new RuntimeException();
    }

    public Object title() {
        return "hello";
    }

    @Disabled("some static reason")
    public void someDisabledButton() {}

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof AnotherSampleModel;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public TabbedRootSampleModel redirectToTabbedSample() {
        return new TabbedRootSampleModel(this);
    }

    public void showSelectSampleModal() {
        GuiService.get().showModalPanel(new SelectSampleModel());
    }

    public Bugfix bugfix() {
        return new Bugfix();
    }

    public BugfixRemoveFrom bugfixRemoveFrom() {
        return new BugfixRemoveFrom();
    }

    public SuffixModelSuffix suffixModelSuffix() {
        return new SuffixModelSuffix();
    }

}
