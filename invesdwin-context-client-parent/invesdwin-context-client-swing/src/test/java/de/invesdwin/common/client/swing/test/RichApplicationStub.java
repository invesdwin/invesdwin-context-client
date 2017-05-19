package de.invesdwin.common.client.swing.test;

import java.util.concurrent.Callable;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.fixture.FrameFixture;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.common.client.swing.ContentPane;
import de.invesdwin.common.client.swing.StatusBar;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.test.stub.StubSupport;

@ThreadSafe
@Named
public class RichApplicationStub extends StubSupport {

    private static volatile FrameFixture frameFixture;

    @Inject
    private StatusBar statusBar;
    @Inject
    private ContentPane contentPane;

    @Override
    public void setUpContext(final ATest test, final TestContext ctx) {
        FailOnThreadViolationRepaintManager.install();
        initFrameFixture(); //so that views can be added in setupOnce already without there being problems with ProxyActions
    }

    @Override
    public void setUp(final ATest test, final TestContext ctx) {
        initFrameFixture();
    }

    private void initFrameFixture() {
        if (frameFixture == null) {
            Application.launch(de.invesdwin.common.client.swing.internal.app.DelegateRichApplication.class,
                    new String[] {});
            try {
                frameFixture = EventDispatchThreadUtil.invokeAndWait(new Callable<FrameFixture>() {
                    @Override
                    public FrameFixture call() throws Exception {
                        final SingleFrameApplication application = (SingleFrameApplication) Application.getInstance();
                        return new FrameFixture(application.getMainFrame());
                    }
                });
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void tearDown(final ATest test, final TestContext ctx) {
        statusBar.reset();
        contentPane.reset();
        Application.getInstance().getContext().getTaskService().shutdownNow();
        frameFixture.cleanUp();
        frameFixture = null;
    }

    public FrameFixture getFrameFixture() {
        return frameFixture;
    }

}
