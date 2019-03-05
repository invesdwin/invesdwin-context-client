package de.invesdwin.context.client.swing.test.internal;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Named;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.beans.init.locations.PositionedResource;
import de.invesdwin.context.client.swing.test.SwingExplorerTest;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.test.stub.StubSupport;
import de.invesdwin.util.lang.Reflections;
import de.invesdwin.util.shutdown.IShutdownHook;
import de.invesdwin.util.shutdown.ShutdownHookManager;

@Named
@NotThreadSafe
public class SwingExplorerTestStub extends StubSupport {

    private static volatile org.swingexplorer.internal.SwingExplorerApplication lastInstance;

    static {
        ShutdownHookManager.register(new IShutdownHook() {
            @Override
            public void shutdown() throws Exception {
                maybeCloseLastInstance();
            }
        });
    }

    @Override
    public void setUpContextLocations(final ATest test, final List<PositionedResource> locations) throws Exception {
        //if for some reason the tearDownOnce was not executed on the last test (maybe maven killed it?), then try to stop here aswell
        maybeCloseLastInstance();
    }

    @Override
    public void setUpOnce(final ATest test, final TestContext ctx) throws Exception {
        final SwingExplorerTest annotation = Reflections.getAnnotation(test, SwingExplorerTest.class);
        if (annotation != null && annotation.value()) {
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
    public void tearDownOnce(final ATest test) throws Exception {
        maybeCloseLastInstance();
    }

    private static void maybeCloseLastInstance() throws Exception {
        if (lastInstance != null) {
            lastInstance.close();
            lastInstance = null;
        }
    }

}
