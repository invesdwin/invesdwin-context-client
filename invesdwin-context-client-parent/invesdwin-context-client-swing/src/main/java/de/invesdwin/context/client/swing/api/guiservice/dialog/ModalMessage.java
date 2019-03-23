package de.invesdwin.context.client.swing.api.guiservice.dialog;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.swing.api.AModel;
import de.invesdwin.norva.beanpath.annotation.ModalCloser;

@NotThreadSafe
public class ModalMessage extends AModel {

    private final String title;
    private String message;

    public ModalMessage(final String message) {
        this(null, message);
    }

    public ModalMessage(final String title, final String message) {
        this.title = title;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    @ModalCloser
    public void ok() {}

    @ModalCloser
    public void cancel() {}

    /**
     * override this method to make the cancel button visible
     */
    public boolean hideCancel() {
        return true;
    }

    public String title() {
        return title;
    }

}
