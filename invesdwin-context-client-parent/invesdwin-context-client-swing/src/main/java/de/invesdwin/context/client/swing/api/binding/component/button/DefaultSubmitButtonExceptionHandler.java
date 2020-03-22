package de.invesdwin.context.client.swing.api.binding.component.button;

import java.awt.Component;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.util.Views;
import de.invesdwin.norva.beanpath.spi.element.IBeanPathElement;
import de.invesdwin.util.lang.Strings;
import de.invesdwin.util.swing.Components;
import de.invesdwin.util.swing.Dialogs;

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
                final String title = GuiService.get()
                        .getResourceMap(DefaultSubmitButtonExceptionHandler.class)
                        .getString(DefaultSubmitButtonExceptionHandler.class.getSimpleName() + ".exception.title");
                final String message = Components.getDefaultToolTipFormatter().format(t.getMessage());
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
        Dialogs.showMessageDialog(Views.getRootComponentInDockable(component),
                Strings.prependIfMissingIgnoreCase(message, "<html>"), title, Dialogs.ERROR_MESSAGE);
    }

    public void logShowExceptionMessage(final Throwable t) {
        LOG.catching(org.slf4j.ext.XLogger.Level.WARN,
                new RuntimeException("Showing message dialog for button exception...", t));
    }

}
