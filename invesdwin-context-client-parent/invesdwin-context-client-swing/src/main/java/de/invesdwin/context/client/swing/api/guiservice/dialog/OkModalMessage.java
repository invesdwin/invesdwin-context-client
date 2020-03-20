package de.invesdwin.context.client.swing.api.guiservice.dialog;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.swing.api.annotation.DefaultCloseOperation;
import de.invesdwin.context.client.swing.api.view.AModel;
import de.invesdwin.norva.beanpath.annotation.ModalCloser;

@NotThreadSafe
public class OkModalMessage extends AModel {

    private final String title;
    private String message;

    public OkModalMessage(final String message) {
        this(null, message);
    }

    public OkModalMessage(final String title, final String message) {
        this.title = title;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    @DefaultCloseOperation
    @ModalCloser
    public void ok() {
    }

    public String title() {
        return title;
    }

}
