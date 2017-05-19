package de.invesdwin.common.client.wicket.saml.page;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.security.web.saml.SamlProperties;
import de.invesdwin.nowicket.application.AWebPage;
import de.invesdwin.nowicket.application.auth.AWebSession;
import de.invesdwin.nowicket.application.auth.Roles;
import de.invesdwin.nowicket.page.auth.SignIn;

@NotThreadSafe
public class SamlLoginSuccessPage extends AWebPage {

    public static final String MOUNT_PATH = SamlProperties.MOUNT_PATH_SAML_LOGIN_SUCCESS;

    public SamlLoginSuccessPage() {
        super(null);
        if (Roles.getAuthenticationService().shouldReplaceSessionAfterSignIn()) {
            AWebSession.get().replaceSession();
        }
        final SignIn signIn = new SignIn();
        signIn.setComponent(this);
        signIn.onSignInSucceeded();
    }

}
