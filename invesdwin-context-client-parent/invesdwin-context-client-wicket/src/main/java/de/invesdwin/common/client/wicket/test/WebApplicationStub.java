package de.invesdwin.common.client.wicket.test;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Named;

import org.apache.wicket.util.tester.WicketTester;
import org.springframework.security.core.context.SecurityContextHolder;

import de.invesdwin.context.integration.IntegrationProperties;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.test.stub.StubSupport;
import de.invesdwin.nowicket.generated.guiservice.GuiService;
import de.invesdwin.nowicket.generated.guiservice.test.GuiServiceTester;
import de.invesdwin.util.assertions.Assertions;

@NotThreadSafe
@Named
public class WebApplicationStub extends StubSupport {

    private WicketTester wicketTester;
    private GuiServiceTester guiServiceTester;

    @Override
    public void setUpOnce(final ATest test, final TestContext ctx) throws Exception {
        if (isEnabled()) {
            Assertions.assertThat(wicketTester).isNull();
            this.wicketTester = new WicketTester(
                    new de.invesdwin.common.client.wicket.internal.DelegateWebApplication());
            this.guiServiceTester = new GuiServiceTester();
            GuiService.setGuiServiceOverride(guiServiceTester);
        }
    }

    /**
     * Since SessionGuiService is deactivated here, you cannot test generated modals in wicketTester, but need to check
     * GuiServiceMock instead. You should rather use model based testing anyway instead of rendered tests with wicket
     * tester.
     */
    @Deprecated
    public WicketTester getWicketTester() {
        assertEnabled();
        return wicketTester;
    }

    public GuiServiceTester getGuiServiceTester() {
        Assertions.assertThat(isEnabled()).isTrue();
        return guiServiceTester;
    }

    @Override
    public void setUp(final ATest test, final TestContext ctx) throws Exception {
        if (isEnabled()) {
            if (wicketTester.getLastRenderedPage() != null) {
                wicketTester.clearFeedbackMessages();
            }
            guiServiceTester.reset();
        }
    }

    @Override
    public void tearDownOnce(final ATest test) throws Exception {
        if (isEnabled()) {
            Assertions.assertThat(wicketTester).isNotNull();
            wicketTester.destroy();
            wicketTester = null;
            GuiService.setGuiServiceOverride(null);
            this.guiServiceTester = null;
        }
        //need to clear security context anyway, even if not enabled
        SecurityContextHolder.clearContext();
        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    public boolean isEnabled() {
        return !IntegrationProperties.isWebserverTest();
    }

    private void assertEnabled() {
        Assertions.assertThat(isEnabled())
                .as("%s is not enabled since a test webserver is running at: %s", getClass().getSimpleName(),
                        IntegrationProperties.WEBSERVER_BIND_URI)
                .isTrue();
    }
}
