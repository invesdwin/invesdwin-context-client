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

    private static boolean launched = false;
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
        if (!launched) {
            return;
        }
        launched = false;
        if (Application.getInstance() instanceof SingleFrameApplication) {
            final SingleFrameApplication application = (SingleFrameApplication) Application.getInstance();
            application.getMainFrame().setVisible(false);
        }
        statusBar.reset();
        statusBar = null;
        contentPane.reset();
        contentPane = null;
        for (final Task<?, ?> task : GuiService.get().getTaskService().getTasks()) {
            task.cancel(true);
        }
        RichApplicationProperties.reset();
    }

    public static synchronized void maybeLaunch() {
        if (!launched) {
            statusBar = MergedContext.getInstance().getBean(StatusBar.class);
            contentPane = MergedContext.getInstance().getBean(ContentPane.class);
            DelegateRichApplication.launch();
            launched = true;
        }
    }

}
