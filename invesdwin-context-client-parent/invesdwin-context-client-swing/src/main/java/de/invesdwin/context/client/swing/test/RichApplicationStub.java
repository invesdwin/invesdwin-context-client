package de.invesdwin.context.client.swing.test;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;

import de.invesdwin.context.client.swing.api.guiservice.ContentPane;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.api.guiservice.StatusBar;
import de.invesdwin.context.client.swing.frame.RichApplicationProperties;
import de.invesdwin.context.client.swing.frame.app.DelegateRichApplication;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.test.stub.StubSupport;

@ThreadSafe
@Named
public class RichApplicationStub extends StubSupport {

    private static boolean launched = false;
    @Inject
    private StatusBar statusBar;
    @Inject
    private ContentPane contentPane;

    @Override
    public void setUpContext(final ATest test, final TestContext ctx) throws Exception {
    }

    @Override
    public void setUpOnce(final ATest test, final TestContext ctx) throws Exception {
        maybeLaunch();
    }

    @Override
    public void tearDown(final ATest test, final TestContext ctx) {
        launched = false;
        if (Application.getInstance() instanceof SingleFrameApplication) {
            final SingleFrameApplication application = (SingleFrameApplication) Application.getInstance();
            application.getMainFrame().setVisible(false);
        }
        statusBar.reset();
        contentPane.reset();
        for (final Task<?, ?> task : GuiService.get().getTaskService().getTasks()) {
            task.cancel(true);
        }
        RichApplicationProperties.reset();
    }

    public static void maybeLaunch() {
        if (!launched) {
            DelegateRichApplication.launch();
            launched = true;
        }
    }

}
