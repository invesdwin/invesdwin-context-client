package de.invesdwin.context.client.wicket.generated.markup.internal.run;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import de.invesdwin.context.client.wicket.generated.markup.internal.AnotherSampleModel;
import de.invesdwin.context.client.wicket.generated.markup.internal.CustomModalModel;
import de.invesdwin.context.client.wicket.test.WebApplicationStub;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.nowicket.application.auth.Roles;
import de.invesdwin.nowicket.generated.guiservice.GuiService;
import de.invesdwin.nowicket.generated.guiservice.test.GuiServiceMethod;
import de.invesdwin.nowicket.generated.guiservice.test.GuiServiceMethodCall;
import de.invesdwin.nowicket.generated.guiservice.test.GuiServiceTester;
import de.invesdwin.util.assertions.Assertions;

@NotThreadSafe
public class AutomatedAnotherSampleModelTest extends ATest {

    @Inject
    private WebApplicationStub webApplicationStub;

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.activateBean(AnotherSampleModelPageTestApplication.class);
    }

    @Test
    public void testStubEnabled() throws InterruptedException {
        Assertions.assertThat(webApplicationStub.isEnabled()).isTrue();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testHomePageNotRendered() {
        final WicketTester tester = webApplicationStub.getWicketTester();
        //homepage gets rendered
        Assertions.assertThat(tester.getLastRenderedPage()).isNull();
    }

    @Test
    public void testGuiServiceMock() {
        final GuiServiceTester mock = webApplicationStub.getGuiServiceTester();
        Assertions.assertThat(mock.getMethodCalls()).isEmpty();
        final AnotherSampleModel anotherSampleModel = new AnotherSampleModel();
        Assertions.assertThat(mock.getMethodCalls()).hasSize(2);
        for (final GuiServiceMethodCall call : mock.getMethodCalls()) {
            Assertions.assertThat(call.getMethod()).isEqualTo(GuiServiceMethod.showStatusMessage);
        }
        mock.reset();
        anotherSampleModel.openCustomModal();
        Assertions.assertThat(mock.getMethodCalls()).hasSize(1);
        final GuiServiceMethodCall call = mock.getMethodCalls().pop();
        Assertions.assertThat(GuiService.get().isModalPanelShowing()).isTrue();
        Assertions.assertThat(call.getMethod()).isEqualTo(GuiServiceMethod.showModalPanel);
        Assertions.assertThat(call.getArgs()).hasSize(1);
        final CustomModalModel customModalModel = (CustomModalModel) call.getArgs().get(0);
        customModalModel.close();
        Assertions.assertThat(GuiService.get().isModalPanelShowing()).isTrue();
        GuiService.get().hideModalPanel();
        Assertions.assertThat(GuiService.get().isModalPanelShowing()).isFalse();
    }

    @Test
    public void testRolesWithoutAuthenticationManager() {
        Assertions.assertThat(Roles.get().hasRole("SOME")).isFalse();
        Assertions.assertThat(Roles.evaluateExpression("hasRole('SOME')")).isFalse();
        Assertions.assertThat(Roles.evaluateExpression("permitAll")).isTrue();
    }

    @Test
    public void testRememberMeConversion() {
        Roles.getAuthenticationService()
                .setAuthentication(new UsernamePasswordAuthenticationToken("someUser", "somePassword"));
        Assertions.assertThat(Roles.isAuthenticated()).isTrue();
        Assertions.assertThat(Roles.isRememberMe()).isFalse();
        Assertions.assertThat(Roles.isAnonymous()).isFalse();
        Assertions.assertThat(Roles.isFullyAuthenticated()).isTrue();
        Assertions.assertThat(Roles.evaluateExpression("isAuthenticated()")).isTrue();
        Assertions.assertThat(Roles.evaluateExpression("isRememberMe()")).isFalse();
        Assertions.assertThat(Roles.evaluateExpression("isAnonymous()")).isFalse();
        Assertions.assertThat(Roles.evaluateExpression("isFullyAuthenticated()")).isTrue();
        Roles.getAuthenticationService().convertUsernamePasswordToRememberMeAuthentication();
        Assertions.assertThat(Roles.isAuthenticated()).isTrue();
        Assertions.assertThat(Roles.isRememberMe()).isTrue();
        Assertions.assertThat(Roles.isAnonymous()).isFalse();
        Assertions.assertThat(Roles.isFullyAuthenticated()).isFalse();
        Assertions.assertThat(Roles.evaluateExpression("isAuthenticated()")).isTrue();
        Assertions.assertThat(Roles.evaluateExpression("isRememberMe()")).isTrue();
        Assertions.assertThat(Roles.evaluateExpression("isAnonymous()")).isFalse();
        Assertions.assertThat(Roles.evaluateExpression("isFullyAuthenticated()")).isFalse();
    }
}
