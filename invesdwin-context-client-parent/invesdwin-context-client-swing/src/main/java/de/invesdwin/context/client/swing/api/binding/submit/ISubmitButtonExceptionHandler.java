package de.invesdwin.context.client.swing.api.binding.submit;

import java.awt.Component;
import java.io.Serializable;

import de.invesdwin.norva.beanpath.spi.element.IBeanPathElement;

public interface ISubmitButtonExceptionHandler extends Serializable {

    void handleSubmitButtonException(IBeanPathElement element, Component component, Throwable t);

}
