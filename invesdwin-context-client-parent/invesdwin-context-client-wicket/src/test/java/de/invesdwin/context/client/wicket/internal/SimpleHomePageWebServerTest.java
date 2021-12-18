package de.invesdwin.context.client.wicket.internal;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.jupiter.api.Test;

import de.invesdwin.context.integration.IntegrationProperties;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.webserver.test.WebserverTest;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.uri.URIs;

@NotThreadSafe
@WebserverTest
public class SimpleHomePageWebServerTest extends ATest {

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.activateBean(SimpleHomePageTestApplication.class);
    }

    @Test
    public void testWebServer() throws InterruptedException {
        final String website = URIs.connect(IntegrationProperties.WEBSERVER_BIND_URI).download();
        Assertions.assertThat(website).contains(SimpleHomePage.SUCCESS_MESSAGE);
    }
}
