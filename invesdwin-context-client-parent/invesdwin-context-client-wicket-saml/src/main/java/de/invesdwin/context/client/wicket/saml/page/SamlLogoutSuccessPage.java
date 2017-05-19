package de.invesdwin.context.client.wicket.saml.page;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.wicket.RestartResponseException;

import de.invesdwin.context.security.web.saml.SamlProperties;
import de.invesdwin.nowicket.application.AWebPage;
import de.invesdwin.nowicket.application.auth.ABaseWebApplication;
import de.invesdwin.nowicket.application.auth.AWebSession;

@NotThreadSafe
public class SamlLogoutSuccessPage extends AWebPage {

    public static final String MOUNT_PATH = SamlProperties.MOUNT_PATH_SAML_LOGOUT_SUCCESS;

    public SamlLogoutSuccessPage() {
        super(null);
        AWebSession.get().invalidate();
        throw new RestartResponseException(ABaseWebApplication.get().getSignOutPage());
    }

}
