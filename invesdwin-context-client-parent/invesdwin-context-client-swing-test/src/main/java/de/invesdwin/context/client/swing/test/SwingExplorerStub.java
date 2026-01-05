package de.invesdwin.context.client.swing.test;

import java.io.Closeable;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.context.beans.init.locations.PositionedResource;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.ITestContext;
import de.invesdwin.context.test.ITestContextSetup;
import de.invesdwin.context.test.stub.StubSupport;
import de.invesdwin.util.lang.reflection.Reflections;
import de.invesdwin.util.shutdown.IShutdownHook;
import de.invesdwin.util.shutdown.ShutdownHookManager;
import de.invesdwin.util.swing.EventDispatchThreadUtil;
import jakarta.inject.Named;

@Named
@ThreadSafe
public class SwingExplorerStub extends StubSupport implements Closeable {

    @GuardedBy("this.class")
    private static org.swingexplorer.internal.SwingExplorerApplication lastInstance;

    static {
        ShutdownHookManager.register(new IShutdownHook() {
            @Override
            public void shutdown() throws Exception {
                closeStatic();
            }
        });
    }

    @Override
    public void setUpContextLocations(final ATest test, final List<PositionedResource> locations) throws Exception {}

    @Override
    public void setUpContext(final ATest test, final ITestContextSetup ctx) throws Exception {
        if (ctx.isPreMergedContext()) {
            return;
        }
        //if for some reason the tearDownOnce was not executed on the last test (maybe maven killed it?), then try to stop here aswell
        close();
    }

    @Override
    public void setUpOnce(final ATest test, final ITestContext ctx) throws Exception {
        final SwingExplorerTest annotation = Reflections.getAnnotation(test, SwingExplorerTest.class);
        if (annotation != null && annotation.value()) {
            open();
        }
    }

    public void open() throws InterruptedException {
        openStatic();
    }

    private static synchronized void openStatic() throws InterruptedException {
        if (lastInstance == null) {
            lastInstance = new org.swingexplorer.internal.SwingExplorerApplication();
            EventDispatchThreadUtil.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    lastInstance.run();
                }
            });
        }
    }

    @Override
    public void tearDownOnce(final ATest test, final ITestContext ctx) {
        if (!ctx.isFinishedGlobal()) {
            return;
        }
        close();
    }

    @Override
    public void close() {
        closeStatic();
    }

    private static synchronized void closeStatic() {
        if (lastInstance != null) {
            lastInstance.close();
            lastInstance = null;
        }
    }

}
