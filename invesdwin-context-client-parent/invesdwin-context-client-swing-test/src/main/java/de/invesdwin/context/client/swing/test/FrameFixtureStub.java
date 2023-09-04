package de.invesdwin.context.client.swing.test;

import java.awt.AWTEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.ThreadSafe;

import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.fixture.FrameFixture;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

import de.invesdwin.context.client.swing.test.edt.ITimeoutEventQueueListener;
import de.invesdwin.context.client.swing.test.edt.TimeoutEventQueue;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.test.stub.StubSupport;
import de.invesdwin.util.swing.EventDispatchThreadUtil;
import de.invesdwin.util.time.duration.Duration;
import jakarta.inject.Named;

@ThreadSafe
@Named
public class FrameFixtureStub extends StubSupport {

    private static volatile FrameFixture frameFixture;
    private static volatile boolean installFailOnThreadViolationRepaintManager = true;
    private static volatile Duration installEventDispatchThreadBlockingTimeout = null;

    @Override
    public void setUpContext(final ATest test, final TestContext ctx) {
        if (installFailOnThreadViolationRepaintManager) {
            FailOnThreadViolationRepaintManager.install();
        }
        final Duration timeout = installEventDispatchThreadBlockingTimeout;
        if (timeout != null) {
            final TimeoutEventQueue timeoutEventQueue = TimeoutEventQueue.install();
            final Duration checkInterval = Duration.ONE_SECOND.orLower(timeout.divide(2))
                    .orHigher(Duration.ONE_MILLISECOND);
            timeoutEventQueue.addTimeoutListener(timeout, checkInterval, new ITimeoutEventQueueListener() {
                @Override
                public void onTimeout(final AWTEvent event, final Thread eventDispatchThread) {
                    final StackTraceElement[] stackTrace = eventDispatchThread.getStackTrace();
                    final TimeoutException exception = new TimeoutException(
                            "EventDispatchThread timeout [" + timeout + "] occured");
                    exception.setStackTrace(stackTrace);
                    Err.process(exception);
                }
            }, false);
        }
    }

    public static boolean isInstallFailOnThreadViolationRepaintManager() {
        return installFailOnThreadViolationRepaintManager;
    }

    public static void setInstallFailOnThreadViolationRepaintManager(
            final boolean installFailOnThreadViolationRepaintManager) {
        FrameFixtureStub.installFailOnThreadViolationRepaintManager = installFailOnThreadViolationRepaintManager;
    }

    public static void setInstallEventDispatchThreadBlockingTimeout(
            final Duration installEventDispatchThreadBlockingTimeout) {
        FrameFixtureStub.installEventDispatchThreadBlockingTimeout = installEventDispatchThreadBlockingTimeout;
    }

    public static Duration getInstallEventDispatchThreadBlockingTimeout() {
        return installEventDispatchThreadBlockingTimeout;
    }

    @Override
    public void setUpOnce(final ATest test, final TestContext ctx) throws Exception {
        initFrameFixture(); //so that views can be added in setupOnce already without there being problems with ProxyActions
    }

    @Override
    public void setUp(final ATest test, final TestContext ctx) {
        initFrameFixture();
    }

    private void initFrameFixture() {
        if (frameFixture == null) {
            RichApplicationStub.maybeLaunch();
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
        if (frameFixture != null) {
            frameFixture.cleanUp();
            frameFixture = null;
        }
    }

    public FrameFixture getFrameFixture() {
        return frameFixture;
    }

}
