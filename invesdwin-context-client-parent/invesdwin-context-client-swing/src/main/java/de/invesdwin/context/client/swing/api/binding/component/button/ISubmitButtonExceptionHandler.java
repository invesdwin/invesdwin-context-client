package de.invesdwin.context.client.swing.api.binding.component.button;

import java.awt.Component;
import java.io.Serializable;

public interface ISubmitButtonExceptionHandler extends Serializable {

    void handleSubmitButtonException(Component component, Throwable t);

}
