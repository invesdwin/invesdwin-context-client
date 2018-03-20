package de.invesdwin.context.client.swing;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.Test;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.util.time.fdate.FTimeUnit;

@NotThreadSafe
public class GuiExceptionHandlerTest extends ATest {

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.activate(TestView.class);
        ctx.activate(TestRichApplication.class);
    }

    @Test
    public void test() throws InterruptedException {
        EventDispatchThreadUtil.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                final NullPointerException e = new NullPointerException();
                GuiExceptionHandler.INSTANCE.loggedException(Err.process(e), true);
            }
        });
        try {
            FTimeUnit.SECONDS.sleep(3);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
