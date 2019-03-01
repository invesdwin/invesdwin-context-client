package de.invesdwin.context.client.swing.error;

import de.invesdwin.context.log.error.LoggedRuntimeException;

public interface IGuiExceptionHandlerHook {

    boolean shouldHideException(LoggedRuntimeException e);

}
