package de.invesdwin.context.client.wicket.samples;

import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.Test;

import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.webserver.test.WebserverTest;

@NotThreadSafe
@WebserverTest
public class FileUploadRequiredPageTestWebServer extends ATest {

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.activate(FileUploadRequiredPageTestApplication.class);
    }

    @Test
    public void testWebServer() throws InterruptedException {
        TimeUnit.DAYS.sleep(Long.MAX_VALUE);
    }
}
