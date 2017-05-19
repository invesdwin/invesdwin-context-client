package de.invesdwin.context.client.swing.api;

import javax.annotation.concurrent.NotThreadSafe;

import org.jdesktop.application.Application;

import de.invesdwin.context.log.error.Err;

/**
 * Ensures proper ErrorHandling.
 * 
 * @author subes
 */
@NotThreadSafe
public abstract class ATask<T, V> extends org.jdesktop.application.Task<T, V> {

    public ATask(final Application application) {
        super(application);
    }

    @Override
    protected void failed(final Throwable cause) {
        Err.process(cause);
    }

}
