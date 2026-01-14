package de.invesdwin.context.client.swing.test;

import java.awt.AWTEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.fixture.FrameFixture;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

import de.invesdwin.context.client.swing.test.edt.ITimeoutEventQueueListener;
import de.invesdwin.context.client.swing.test.edt.TimeoutEventQueue;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.ITestContext;
import de.invesdwin.context.test.ITestContextSetup;
import de.invesdwin.context.test.stub.StubSupport;
import de.invesdwin.util.swing.EventDispatchThreadUtil;
import de.invesdwin.util.time.duration.Duration;
import jakarta.inject.Named;

@ThreadSafe
@Named
public class FrameFixtureStub extends StubSupport {

    @GuardedBy("this.class")
    private static volatile FrameFixture frameFixture;
    private static volatile boolean installFailOnThreadViolationRepaintManager = true;
    private static volatile Duration installEventDispatchThreadBlockingTimeout = null;

    @GuardedBy("this.class")
    private static boolean addedFailOnThreadViolationRepaintManager;
    @GuardedBy("this.class")
    private static boolean addedEventDispatchThreadBlockingTimeout;

    @Override
    public void setUpContext(final ATest test, final ITestContextSetup ctx) {
        if (ctx.isPreMergedContext()) {
            return;
        }
        updateFailOnThreadViolationRepaintManager();
        updateEventDispatchThreadBlockingTimeout();
    }

    private static synchronized void updateEventDispatchThreadBlockingTimeout() {
        final Duration timeout = installEventDispatchThreadBlockingTimeout;
        if (timeout != null) {
            final TimeoutEventQueue timeoutEventQueue = TimeoutEventQueue.install();
            addedEventDispatchThreadBlockingTimeout = true;
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
        } else if (addedEventDispatchThreadBlockingTimeout) {
            TimeoutEventQueue.uninstall();
            addedEventDispatchThreadBlockingTimeout = false;
        }
    }

    private static synchronized void updateFailOnThreadViolationRepaintManager() {
        if (installFailOnThreadViolationRepaintManager) {
            FailOnThreadViolationRepaintManager.install();
            addedFailOnThreadViolationRepaintManager = true;
        } else if (addedFailOnThreadViolationRepaintManager) {
            FailOnThreadViolationRepaintManager.uninstall();
            addedFailOnThreadViolationRepaintManager = false;
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
    public void setUpOnce(final ATest test, final ITestContext ctx) throws Exception {
        initFrameFixture(); //so that views can be added in setupOnce already without there being problems with ProxyActions
    }

    @Override
    public void setUp(final ATest test, final ITestContext ctx) {
        initFrameFixture();
    }

    private static synchronized void initFrameFixture() {
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
    public void tearDown(final ATest test, final ITestContext ctx) {
        if (!ctx.isFinishedGlobal()) {
            return;
        }
        cleanUpFrameFixture();
    }

    private static synchronized void cleanUpFrameFixture() {
        if (frameFixture != null) {
            frameFixture.cleanUp();
            frameFixture = null;
        }
    }

    public FrameFixture getFrameFixture() {
        synchronized (FrameFixtureStub.class) {
            return frameFixture;
        }
    }

}
