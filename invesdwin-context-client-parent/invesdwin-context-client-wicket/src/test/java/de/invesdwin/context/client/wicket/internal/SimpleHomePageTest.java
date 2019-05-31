package de.invesdwin.context.client.wicket.internal;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;

import de.invesdwin.context.client.wicket.test.WebApplicationStub;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;

@NotThreadSafe
public class SimpleHomePageTest extends ATest {
    private WicketTester tester;

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.deactivateBean(WebApplicationStub.class);
    }

    @SuppressWarnings("JUnit4SetUpNotRun")
    @Override
    public void setUp() throws Exception {
        super.setUp();
        tester = new WicketTester(new SimpleHomePageTestWebApplication());
    }

    @Test
    public void testRenderMyPage() {
        //start and render the test page
        tester.startPage(SimpleHomePage.class);

        //assert rendered page class
        tester.assertRenderedPage(SimpleHomePage.class);

        //assert rendered label component
        tester.assertLabel("message", SimpleHomePage.SUCCESS_MESSAGE);
    }
}
