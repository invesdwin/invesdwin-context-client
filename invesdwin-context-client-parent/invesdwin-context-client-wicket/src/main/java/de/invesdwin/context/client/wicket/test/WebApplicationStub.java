package de.invesdwin.context.client.wicket.test;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.wicket.util.tester.WicketTester;
import org.springframework.security.core.context.SecurityContextHolder;

import de.invesdwin.context.integration.IntegrationProperties;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.test.stub.StubSupport;
import de.invesdwin.nowicket.generated.guiservice.GuiService;
import de.invesdwin.nowicket.generated.guiservice.test.GuiServiceTester;
import de.invesdwin.util.assertions.Assertions;
import io.netty.util.concurrent.FastThreadLocal;
import jakarta.inject.Named;

@ThreadSafe
@Named
public class WebApplicationStub extends StubSupport {

    private static final FastThreadLocal<WicketTester> WICKET_TESTER_HOLDER = new FastThreadLocal<>();
    private static final FastThreadLocal<GuiServiceTester> GUI_SERVICE_TESTER_HOLDER = new FastThreadLocal<>();

    @Override
    public void setUp(final ATest test, final TestContext ctx) throws Exception {
        if (isEnabled()) {
            initWicketTester();
        }
    }

    private static synchronized void initWicketTester() {
        if (WICKET_TESTER_HOLDER.get() == null) {
            final WicketTester wicketTester = new WicketTester(
                    new de.invesdwin.context.client.wicket.internal.DelegateWebApplication());
            WICKET_TESTER_HOLDER.set(wicketTester);
            final GuiServiceTester guiServiceTester = new GuiServiceTester();
            GUI_SERVICE_TESTER_HOLDER.set(guiServiceTester);
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
        return WICKET_TESTER_HOLDER.get();
    }

    public GuiServiceTester getGuiServiceTester() {
        assertEnabled();
        return GUI_SERVICE_TESTER_HOLDER.get();
    }

    public void reset() {
        resetWicketTester();
        resetGuiServiceTester();
    }

    public void resetWicketTester() {
        final WicketTester wicketTester = WICKET_TESTER_HOLDER.get();
        if (wicketTester == null) {
            return;
        }
        if (wicketTester.getLastRenderedPage() != null) {
            wicketTester.clearFeedbackMessages();
        }
    }

    public void resetGuiServiceTester() {
        final GuiServiceTester guiServiceTester = GUI_SERVICE_TESTER_HOLDER.get();
        guiServiceTester.reset();
    }

    @Override
    public void tearDown(final ATest test, final TestContext ctx) throws Exception {
        if (!ctx.isFinished()) {
            return;
        }
        cleanUpWicketTester();
    }

    @Override
    public void tearDownOnce(final ATest test, final TestContext ctx) {
        if (!ctx.isFinished()) {
            return;
        }
        //need to clear security context anyway, even if not enabled
        SecurityContextHolder.clearContext();
        Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private static void cleanUpWicketTester() {
        WicketTester wicketTester = WICKET_TESTER_HOLDER.get();
        if (wicketTester == null) {
            return;
        }
        WICKET_TESTER_HOLDER.remove();
        wicketTester.destroy();
        wicketTester = null;
        GuiService.setGuiServiceOverride(null);
        GUI_SERVICE_TESTER_HOLDER.remove();
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
