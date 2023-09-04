package de.invesdwin.context.client.swing.error;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;
import javax.swing.JDialog;
import javax.swing.UIManager;

import org.jdesktop.application.ResourceMap;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;

import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.frame.app.DelegateRichApplication;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.context.log.error.LoggedRuntimeException;
import de.invesdwin.context.log.error.hook.ErrHookManager;
import de.invesdwin.context.log.error.hook.IErrHook;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.Collections;
import de.invesdwin.util.concurrent.future.ImmutableFuture;
import de.invesdwin.util.error.Throwables;
import de.invesdwin.util.lang.string.description.HtmlToPlainText;
import de.invesdwin.util.shutdown.ShutdownHookManager;
import de.invesdwin.util.swing.Components;
import de.invesdwin.util.swing.Dialogs;
import de.invesdwin.util.swing.EventDispatchThreadUtil;
import de.invesdwin.util.swing.listener.WindowListenerSupport;

@ThreadSafe
public final class GuiExceptionHandler implements IErrHook {

    public static final GuiExceptionHandler INSTANCE = new GuiExceptionHandler();
    private static final int MAX_PREVIEW_MESSAGE_LENGTH = 1500;
    private static final int MAX_EXCEPTIONS_SHOWING = 1;
    private static final AtomicInteger ALREADY_SHOWING_COUNT = new AtomicInteger(0);
    private final Set<IGuiExceptionHandlerHook> hooks = Collections
            .synchronizedSet(new LinkedHashSet<IGuiExceptionHandlerHook>());
    private volatile Throwable shutdownAfterShowing;

    private GuiExceptionHandler() {
        ErrHookManager.register(this);
        UIManager.getDefaults().put(JXErrorPane.uiClassID, DetailedErrorPaneUI.class.getName());
    }

    public static void requestShutdownAfterShowing(final Throwable shutdownAfterShowing) {
        INSTANCE.shutdownAfterShowing = shutdownAfterShowing;
    }

    public static void registerHook(final IGuiExceptionHandlerHook hook) {
        Assertions.assertThat(INSTANCE.hooks.add(hook)).isTrue();
    }

    @Override
    public void loggedException(final LoggedRuntimeException exc, final boolean uncaughtException) {
        if (uncaughtException && !ShutdownHookManager.isShuttingDown()) {
            handleException(exc, false);
        }
    }

    public Future<Void> handleException(final LoggedRuntimeException exc, final boolean forced) {
        if (exc == null) {
            return ImmutableFuture.of(null);
        }
        if (!forced) {
            for (final IGuiExceptionHandlerHook hook : hooks) {
                if (hook.shouldHideException(exc)) {
                    return ImmutableFuture.of(null);
                }
            }
        }
        final ResourceMap resourceMap = getResourceMap();
        final String title = GuiService.i18n(resourceMap, "errorInfo.title", "Unexpected Error");
        final StringBuilder basicErrorMessage = new StringBuilder("<html>");
        basicErrorMessage.append(
                GuiService.i18n(resourceMap, "errorInfo.text", "An unexpected error occured. Please contact support."));
        basicErrorMessage.append("<br>");
        basicErrorMessage.append("<br><b>");
        basicErrorMessage.append(Components.getDefaultToolTipFormatter()
                .format(shortenMessage(Throwables.concatMessages(exc)).replace("\n", "<br>")));
        basicErrorMessage.append("</b>");

        /*
         * prevent the window manager from overflowing due to endless bombardment with error dialogs
         */
        if (ALREADY_SHOWING_COUNT.incrementAndGet() <= MAX_EXCEPTIONS_SHOWING) {
            return EventDispatchThreadUtil.invokeLater(new Callable<Void>() {
                @Override
                public Void call() {
                    final ErrorInfo info = new ErrorInfo(title, basicErrorMessage.toString(), null, null, exc, null,
                            null);
                    final JXErrorPane pane = new JXErrorPane();
                    pane.setErrorInfo(info);
                    final JDialog dialog = JXErrorPane.createDialog(getWindow(), pane);
                    dialog.setTitle("Error");
                    dialog.setSize(new Dimension(800, dialog.getHeight()));
                    dialog.setMinimumSize(new Dimension(100, 100));
                    dialog.addWindowListener(new WindowListenerSupport() {
                        @Override
                        public void windowClosed(final WindowEvent e) {
                            ALREADY_SHOWING_COUNT.decrementAndGet();
                            if (Err.isSameMeaning(shutdownAfterShowing, exc)) {
                                System.exit(1);
                            }
                        }
                    });
                    Dialogs.installEscapeCloseOperation(dialog);
                    dialog.setVisible(true);
                    return null;
                }

            });
        } else {
            ALREADY_SHOWING_COUNT.decrementAndGet();
        }
        return ImmutableFuture.of(null);
    }

    private String shortenMessage(final String message) {
        final String text = HtmlToPlainText.htmlToPlainText(message);
        if (text.length() > MAX_PREVIEW_MESSAGE_LENGTH) {
            return text.substring(0, MAX_PREVIEW_MESSAGE_LENGTH) + " ...";
        } else {
            return text;
        }
    }

    private ResourceMap getResourceMap() {
        try {
            return DelegateRichApplication.getInstance().getContext().getResourceMap(this.getClass());
        } catch (final Throwable t) {
            return null;
        }
    }

    private Window getWindow() {
        try {
            return GuiService.get().getWindow();
        } catch (final Throwable t) {
            return Dialogs.getRootFrame();
        }
    }
}
