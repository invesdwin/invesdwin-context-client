package de.invesdwin.context.client.swing.error;

import de.invesdwin.context.log.error.LoggedRuntimeException;

public interface IGuiExceptionHandlerHook {

    IGuiExceptionHandlerHook[] EMPTY_ARRAY = new IGuiExceptionHandlerHook[0];

    boolean shouldHideException(LoggedRuntimeException e);

}
