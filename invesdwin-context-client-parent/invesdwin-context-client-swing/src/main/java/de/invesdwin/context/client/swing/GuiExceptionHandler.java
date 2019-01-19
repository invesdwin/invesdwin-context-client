package de.invesdwin.context.client.swing;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;
import javax.swing.UIManager;

import org.jdesktop.application.ResourceMap;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.client.swing.api.guiservice.Dialogs;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.internal.DetailedErrorPaneUI;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.context.log.error.LoggedRuntimeException;
import de.invesdwin.context.log.error.hook.ErrHookManager;
import de.invesdwin.context.log.error.hook.IErrHook;
import de.invesdwin.util.assertions.Assertions;

@ThreadSafe
public final class GuiExceptionHandler implements IErrHook {

    public static final GuiExceptionHandler INSTANCE = new GuiExceptionHandler();
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
    public void loggedException(final LoggedRuntimeException e, final boolean uncaughtException) {
        if (uncaughtException) {
            for (final IGuiExceptionHandlerHook hook : hooks) {
                if (hook.shouldHideException(e)) {
                    return;
                }
            }
            final ResourceMap resourceMap = GuiService.get().getResourceMap(this.getClass());
            final String title = resourceMap.getString("errorInfo.title");
            final StringBuilder basicErrorMessage = new StringBuilder("<html>");
            basicErrorMessage.append(resourceMap.getString("errorInfo.text"));
            basicErrorMessage.append("<br>");
            basicErrorMessage.append("<br><b>");
            basicErrorMessage.append(e.toString());
            basicErrorMessage.append("</b>");

            EventDispatchThreadUtil.invokeLater(new Runnable() {
                @Override
                public void run() {
                    final ErrorInfo errorInfo = new ErrorInfo(title, basicErrorMessage.toString(), null, null, e, null,
                            null);
                    JXErrorPane.showFrame(Dialogs.getRootFrame(), errorInfo);
                    if (Err.isSameMeaning(shutdownAfterShowing, e)) {
                        System.exit(1);
                    }
                }
            });
        }
    }
}
