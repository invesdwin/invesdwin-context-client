package de.invesdwin.context.client.swing.api.binding.submit;

import java.awt.Component;
import java.util.MissingResourceException;

import javax.annotation.concurrent.NotThreadSafe;

import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

import de.invesdwin.context.client.swing.api.guiservice.Dialogs;
import de.invesdwin.norva.beanpath.spi.element.IBeanPathElement;
import de.invesdwin.util.lang.Strings;

@NotThreadSafe
public class DefaultSubmitButtonExceptionHandler implements ISubmitButtonExceptionHandler {

    private static final org.slf4j.ext.XLogger LOG = org.slf4j.ext.XLoggerFactory
            .getXLogger(DefaultSubmitButtonExceptionHandler.class);

    @Override
    public void handleSubmitButtonException(final IBeanPathElement element, final Component component,
            final Throwable t) {
        if (shouldSwallowException(t)) {
            LOG.catching(org.slf4j.ext.XLogger.Level.WARN, new RuntimeException("Button exception swallowed...", t));
        } else {
            if (shouldShowExceptionMessage(t)) {
                logShowExceptionMessage(t);
                final ResourceMap resourceMap = Application.getInstance().getContext().getResourceMap(this.getClass());
                final String title = resourceMap.getString(
                        DefaultSubmitButtonExceptionHandler.class.getSimpleName() + ".exception.title", component);
                String message;
                try {
                    message = t.getMessage();
                } catch (final MissingResourceException e) {
                    message = t.getMessage();
                }
                showExceptionMessage(element, component, t, title, message);
            } else {
                propagateException(t);
            }
        }
    }

    protected void propagateException(final Throwable t) {
        throw new RuntimeException("Propagated button exception...", t);
    }

    protected boolean shouldShowExceptionMessage(final Throwable t) {
        return t instanceof Exception && !(t instanceof RuntimeException);
    }

    protected boolean shouldSwallowException(final Throwable t) {
        return t instanceof Exception && !(t instanceof RuntimeException) && Strings.isBlank(t.getMessage());
    }

    protected void showExceptionMessage(final IBeanPathElement element, final Component component, final Throwable t,
            final String title, final String message) {
        Dialogs.showMessageDialog(component, message, title, Dialogs.OK_OPTION);
    }

    public void logShowExceptionMessage(final Throwable t) {
        LOG.catching(org.slf4j.ext.XLogger.Level.WARN,
                new RuntimeException("Showing message dialog for button exception...", t));
    }

}
