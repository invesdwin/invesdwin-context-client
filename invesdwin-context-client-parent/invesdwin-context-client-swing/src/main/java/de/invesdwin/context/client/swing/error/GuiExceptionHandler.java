package de.invesdwin.context.client.swing.error;

import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;
import javax.swing.JFrame;
import javax.swing.UIManager;

import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.context.log.error.LoggedRuntimeException;
import de.invesdwin.context.log.error.hook.ErrHookManager;
import de.invesdwin.context.log.error.hook.IErrHook;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.shutdown.ShutdownHookManager;
import de.invesdwin.util.swing.Dialogs;
import de.invesdwin.util.swing.listener.WindowListenerSupport;

@ThreadSafe
public final class GuiExceptionHandler implements IErrHook {

    public static final GuiExceptionHandler INSTANCE = new GuiExceptionHandler();
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
            for (final IGuiExceptionHandlerHook hook : hooks) {
                if (hook.shouldHideException(exc)) {
                    return;
                }
            }
            final ResourceMap resourceMap = Application.getInstance().getContext().getResourceMap(this.getClass());
            final String title = resourceMap.getString("errorInfo.title");
            final StringBuilder basicErrorMessage = new StringBuilder("<html>");
            basicErrorMessage.append(resourceMap.getString("errorInfo.text"));
            basicErrorMessage.append("<br>");
            basicErrorMessage.append("<br><b>");
            basicErrorMessage.append(exc.toString());
            basicErrorMessage.append("</b>");

            /*
             * prevent the window manager from overflowing due to endless bombardment with error dialogs
             */
            if (ALREADY_SHOWING_COUNT.incrementAndGet() < 10) {
                EventDispatchThreadUtil.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        final ErrorInfo info = new ErrorInfo(title, basicErrorMessage.toString(), null, null, exc, null,
                                null);
                        final JXErrorPane pane = new JXErrorPane();
                        pane.setErrorInfo(info);
                        final JFrame frame = JXErrorPane.createFrame(Dialogs.getRootFrame(), pane);
                        frame.setVisible(true);
                        frame.addWindowListener(new WindowListenerSupport() {
                            @Override
                            public void windowClosed(final WindowEvent e) {
                                ALREADY_SHOWING_COUNT.decrementAndGet();
                                if (Err.isSameMeaning(shutdownAfterShowing, exc)) {
                                    System.exit(1);
                                }
                            }
                        });
                    }
                });
            } else {
                ALREADY_SHOWING_COUNT.decrementAndGet();
            }
        }
    }
}
