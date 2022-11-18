package de.invesdwin.context.client.swing;

import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;

import org.junit.jupiter.api.Test;

import de.invesdwin.context.client.swing.api.guiservice.ContentPane;
import de.invesdwin.context.client.swing.frame.content.WorkingAreaLocation;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import jakarta.inject.Inject;

@ThreadSafe
public class InteractiveTestRichApplication extends ATest {

    @Inject
    private ContentPane contentPane;

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.activateBean(TestRichApplication.class);
    }

    @Override
    public void setUpOnce() throws Exception {
        super.setUpOnce();
        for (int i = 0; i < 2; i++) {
            for (final WorkingAreaLocation location : WorkingAreaLocation.values()) {
                contentPane.showView(new TestModelView(new TestModel("Test Model " + location + " " + i)), location);
            }
        }
    }

    @Test
    public void testRichApplication() throws InterruptedException {
        TimeUnit.DAYS.sleep(Long.MAX_VALUE);
    }

}
