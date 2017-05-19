package de.invesdwin.common.client.swing.api.internal;

import javax.annotation.concurrent.Immutable;

import org.jdesktop.beansbinding.AbstractBindingListener;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.Binding.SyncFailure;

import de.invesdwin.context.log.Log;

/**
 * To make the debugging of synchronization problems easier for bindings-
 * 
 * @author subes
 * 
 */
@Immutable
public class ErrorBindingListener extends AbstractBindingListener {

    private final Log log = new Log(this);

    @SuppressWarnings("rawtypes")
    @Override
    public void syncFailed(final Binding binding, final SyncFailure failure) {
        log.warn("Binding synchronization failed [%s] on [%s].", failure, binding);
    }

}
