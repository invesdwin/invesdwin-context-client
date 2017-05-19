package de.invesdwin.context.client.wicket.generated.markup.internal;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.nowicket.generated.binding.annotation.ModalCloser;
import de.invesdwin.nowicket.generated.markup.annotation.GeneratedMarkup;
import de.invesdwin.util.bean.AValueObject;

@NotThreadSafe
@GeneratedMarkup
public class CustomModalModel extends AValueObject {

    private String someInput;

    public String getSomeInput() {
        return someInput;
    }

    public void setSomeInput(final String someInput) {
        this.someInput = someInput;
    }

    @ModalCloser
    public void close() {}

}
