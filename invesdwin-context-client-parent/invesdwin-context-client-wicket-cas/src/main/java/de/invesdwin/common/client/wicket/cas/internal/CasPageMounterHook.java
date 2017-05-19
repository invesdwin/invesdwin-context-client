package de.invesdwin.common.client.wicket.cas.internal;

import javax.annotation.concurrent.Immutable;
import javax.inject.Named;

import de.invesdwin.common.client.wicket.cas.page.CasLoginPage;
import de.invesdwin.common.client.wicket.cas.page.CasLoginSuccessPage;
import de.invesdwin.common.client.wicket.cas.page.CasLogoutPage;
import de.invesdwin.common.client.wicket.cas.page.CasLogoutSuccessPage;
import de.invesdwin.nowicket.application.auth.ABaseWebApplication;
import de.invesdwin.nowicket.application.filter.init.hook.IWebApplicationInitializerHook;

@Immutable
@Named
public class CasPageMounterHook implements IWebApplicationInitializerHook {

    @Override
    public void onInit(final ABaseWebApplication webApplication) {
        if (webApplication.getDelegate().getAuthenticationService() != null) {
            webApplication.mountPage(CasLoginPage.MOUNT_PATH, CasLoginPage.class);
            webApplication.mountPage(CasLoginSuccessPage.MOUNT_PATH, CasLoginSuccessPage.class);
            webApplication.mountPage(CasLogoutPage.MOUNT_PATH, CasLogoutPage.class);
            webApplication.mountPage(CasLogoutSuccessPage.MOUNT_PATH, CasLogoutSuccessPage.class);
        }
    }

}
