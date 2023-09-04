package de.invesdwin.context.client.swing.error;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.jupiter.api.Test;

import de.invesdwin.context.client.swing.TestModelView;
import de.invesdwin.context.client.swing.TestRichApplication;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.util.swing.EventDispatchThreadUtil;
import de.invesdwin.util.time.date.FTimeUnit;

@NotThreadSafe
public class GuiExceptionHandlerTest extends ATest {

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.activateBean(TestModelView.class);
        ctx.activateBean(TestRichApplication.class);
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
