package de.invesdwin.context.client.wicket.saml.internal;

import javax.annotation.concurrent.Immutable;
import jakarta.inject.Named;

import de.invesdwin.context.client.wicket.saml.page.SamlLoginPage;
import de.invesdwin.context.client.wicket.saml.page.SamlLoginSuccessPage;
import de.invesdwin.context.client.wicket.saml.page.SamlLogoutPage;
import de.invesdwin.context.client.wicket.saml.page.SamlLogoutSuccessPage;
import de.invesdwin.nowicket.application.auth.ABaseWebApplication;
import de.invesdwin.nowicket.application.filter.init.hook.IWebApplicationInitializerHook;

@Immutable
@Named
public class SamlPageMounterHook implements IWebApplicationInitializerHook {

    @Override
    public void onInit(final ABaseWebApplication webApplication) {
        if (webApplication.getConfig().getAuthenticationService() != null) {
            webApplication.mountPage(SamlLoginPage.MOUNT_PATH, SamlLoginPage.class);
            webApplication.mountPage(SamlLoginSuccessPage.MOUNT_PATH, SamlLoginSuccessPage.class);
            webApplication.mountPage(SamlLogoutPage.MOUNT_PATH, SamlLogoutPage.class);
            webApplication.mountPage(SamlLogoutSuccessPage.MOUNT_PATH, SamlLogoutSuccessPage.class);
        }
    }

}
