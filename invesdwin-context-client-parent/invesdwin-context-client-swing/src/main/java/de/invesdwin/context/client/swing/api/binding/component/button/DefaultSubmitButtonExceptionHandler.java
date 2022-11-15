package de.invesdwin.context.client.swing.api.binding.component.button;

import java.awt.Component;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.util.Views;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.util.lang.string.Strings;
import de.invesdwin.util.swing.Components;
import de.invesdwin.util.swing.Dialogs;

@NotThreadSafe
public class DefaultSubmitButtonExceptionHandler implements ISubmitButtonExceptionHandler {

    private static final org.slf4j.ext.XLogger LOG = org.slf4j.ext.XLoggerFactory
            .getXLogger(DefaultSubmitButtonExceptionHandler.class);

    @Override
    public void handleSubmitButtonException(final Component component, final Throwable t) {
        if (shouldSwallowException(t)) {
            logSwallowExceptionMessage(t);
        } else {
            if (shouldShowExceptionMessage(t)) {
                logShowExceptionMessage(t);
                final String title = GuiService.get()
                        .getResourceMap(DefaultSubmitButtonExceptionHandler.class)
                        .getString(DefaultSubmitButtonExceptionHandler.class.getSimpleName() + ".exception.title");
                final String message = Components.getDefaultToolTipFormatter().format(t.getMessage());
                showExceptionMessage(component, t, title, message);
            } else {
                //at least log this unexpected exception
                propagateException(t);
            }
        }
    }

    protected void propagateException(final Throwable t) {
        throw logPropagateExceptionMessage(t);
    }

    protected boolean shouldShowExceptionMessage(final Throwable t) {
        return t instanceof Exception && !(t instanceof RuntimeException);
    }

    protected boolean shouldSwallowException(final Throwable t) {
        return t instanceof Exception && !(t instanceof RuntimeException) && Strings.isBlank(t.getMessage());
    }

    protected void showExceptionMessage(final Component component, final Throwable t, final String title,
            final String message) {
        Component root = Views.getRootComponentInDockable(component);
        if (root == null) {
            root = Dialogs.getRootFrame();
        }
        Dialogs.showMessageDialog(root, Strings.prependIfMissingIgnoreCase(message, "<html>"), title,
                Dialogs.ERROR_MESSAGE);
    }

    public void logShowExceptionMessage(final Throwable t) {
        LOG.catching(org.slf4j.ext.XLogger.Level.WARN,
                new RuntimeException("Showing message dialog for button exception...", t));
    }

    public RuntimeException logPropagateExceptionMessage(final Throwable t) {
        return Err.process(new RuntimeException("Propagated button exception...", t));
    }

    private void logSwallowExceptionMessage(final Throwable t) {
        LOG.catching(org.slf4j.ext.XLogger.Level.WARN, new RuntimeException("Button exception swallowed...", t));
    }

}
