package de.invesdwin.context.client.swing.test;

import java.io.Closeable;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.beans.init.locations.PositionedResource;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.test.stub.StubSupport;
import de.invesdwin.util.lang.reflection.Reflections;
import de.invesdwin.util.shutdown.IShutdownHook;
import de.invesdwin.util.shutdown.ShutdownHookManager;
import de.invesdwin.util.swing.EventDispatchThreadUtil;
import jakarta.inject.Named;

@Named
@NotThreadSafe
public class SwingExplorerStub extends StubSupport implements Closeable {

    private static volatile org.swingexplorer.internal.SwingExplorerApplication lastInstance;

    static {
        ShutdownHookManager.register(new IShutdownHook() {
            @Override
            public void shutdown() throws Exception {
                closeStatic();
            }
        });
    }

    @Override
    public void setUpContextLocations(final ATest test, final List<PositionedResource> locations) throws Exception {
        //if for some reason the tearDownOnce was not executed on the last test (maybe maven killed it?), then try to stop here aswell
        close();
    }

    @Override
    public void setUpOnce(final ATest test, final TestContext ctx) throws Exception {
        final SwingExplorerTest annotation = Reflections.getAnnotation(test, SwingExplorerTest.class);
        if (annotation != null && annotation.value()) {
            open();
        }
    }

    public void open() throws InterruptedException {
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
    public void tearDownOnce(final ATest test, final TestContext ctx) {
        if (!ctx.isFinished()) {
            return;
        }
        close();
    }

    @Override
    public void close() {
        closeStatic();
    }

    private static void closeStatic() {
        if (lastInstance != null) {
            lastInstance.close();
            lastInstance = null;
        }
    }

}
