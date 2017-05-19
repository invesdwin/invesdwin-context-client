package de.invesdwin.context.client.swing;

import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.junit.Test;

import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;

@ThreadSafe
public class InteractiveTestRichApplication extends ATest {

    @Inject
    private ContentPane contentPane;

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.activate(TestRichApplication.class);
    }

    @Override
    public void setUpOnce() throws Exception {
        super.setUpOnce();
        for (int i = 0; i < 10; i++) {
            contentPane.addView(new TestView());
        }
    }

    @Test
    public void testRichApplication() throws InterruptedException {
        TimeUnit.DAYS.sleep(Long.MAX_VALUE);
    }

}
