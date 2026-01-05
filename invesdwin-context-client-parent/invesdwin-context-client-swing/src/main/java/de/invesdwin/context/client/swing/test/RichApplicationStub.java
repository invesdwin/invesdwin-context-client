package de.invesdwin.context.client.swing.test;

import javax.annotation.concurrent.ThreadSafe;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;

import de.invesdwin.context.beans.init.MergedContext;
import de.invesdwin.context.client.swing.api.guiservice.ContentPane;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.api.guiservice.StatusBar;
import de.invesdwin.context.client.swing.frame.RichApplicationProperties;
import de.invesdwin.context.client.swing.frame.app.DelegateRichApplication;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.test.stub.StubSupport;
import jakarta.inject.Named;

@ThreadSafe
@Named
public class RichApplicationStub extends StubSupport {

    private static Application lastApplication = null;
    private static boolean launched;
    private static StatusBar statusBar;
    private static ContentPane contentPane;

    @Override
    public void setUpContext(final ATest test, final TestContext ctx) throws Exception {}

    @Override
    public void setUpOnce(final ATest test, final TestContext ctx) throws Exception {
        maybeLaunch();
    }

    @Override
    public void tearDown(final ATest test, final TestContext ctx) {
        if (!ctx.isFinishedGlobal()) {
            return;
        }
        maybeReset();
    }

    public static synchronized void maybeReset() {
        final Application existingApplication = Application.getInstance();
        if (!launched && lastApplication == existingApplication
                && !RichApplicationProperties.isMainFrameVisible(existingApplication)) {
            return;
        }
        lastApplication = existingApplication;
        launched = false;
        reset(existingApplication);
    }

    public static void reset(final Application existingApplication) {
        if (existingApplication instanceof SingleFrameApplication) {
            final SingleFrameApplication application = (SingleFrameApplication) existingApplication;
            application.getMainFrame().setVisible(false);
        }
        if (statusBar == null) {
            statusBar = MergedContext.getInstance().getBean(StatusBar.class);
        }
        statusBar.reset();
        statusBar = null;
        if (contentPane == null) {
            contentPane = MergedContext.getInstance().getBean(ContentPane.class);
        }
        contentPane.reset();
        contentPane = null;
        for (final Task<?, ?> task : GuiService.get().getTaskService().getTasks()) {
            task.cancel(true);
        }
        RichApplicationProperties.reset();
    }

    public static synchronized void maybeLaunch() {
        if (lastApplication == null) {
            statusBar = MergedContext.getInstance().getBean(StatusBar.class);
            contentPane = MergedContext.getInstance().getBean(ContentPane.class);
            lastApplication = DelegateRichApplication.launch();
            launched = true;
        }
    }

}
