package de.invesdwin.context.client.wicket.generated.markup.internal.run;

import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.jupiter.api.Test;

import de.invesdwin.context.client.wicket.test.WebApplicationStub;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.webserver.test.WebserverTest;
import de.invesdwin.util.assertions.Assertions;
import jakarta.inject.Inject;

@NotThreadSafe
@WebserverTest
public class InteractiveTestAnotherSampleModelPage extends ATest {

    @Inject
    private WebApplicationStub webApplicationStub;

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.activateBean(AnotherSampleModelPageTestApplication.class);
    }

    @Test
    public void testWebServer() throws InterruptedException {
        Assertions.assertThat(webApplicationStub.isEnabled()).isFalse();
        TimeUnit.DAYS.sleep(Long.MAX_VALUE);
    }
}
