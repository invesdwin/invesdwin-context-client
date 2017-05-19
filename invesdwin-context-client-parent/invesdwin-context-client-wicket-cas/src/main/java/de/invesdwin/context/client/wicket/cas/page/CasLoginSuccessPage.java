package de.invesdwin.context.client.wicket.cas.page;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.security.web.cas.CasProperties;
import de.invesdwin.nowicket.application.AWebPage;
import de.invesdwin.nowicket.application.auth.AWebSession;
import de.invesdwin.nowicket.application.auth.Roles;
import de.invesdwin.nowicket.page.auth.SignIn;

@NotThreadSafe
public class CasLoginSuccessPage extends AWebPage {

    public static final String MOUNT_PATH = CasProperties.MOUNT_PATH_CAS_LOGIN_SUCCESS;

    public CasLoginSuccessPage() {
        super(null);
        if (Roles.getAuthenticationService().shouldReplaceSessionAfterSignIn()) {
            AWebSession.get().replaceSession();
        }
        final SignIn signIn = new SignIn();
        signIn.setComponent(this);
        signIn.onSignInSucceeded();
    }

}
