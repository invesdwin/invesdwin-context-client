package de.invesdwin.context.client.wicket.cas.page;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.wicket.RestartResponseException;

import de.invesdwin.context.security.web.cas.CasProperties;
import de.invesdwin.nowicket.application.AWebPage;
import de.invesdwin.nowicket.application.auth.ABaseWebApplication;
import de.invesdwin.nowicket.application.auth.AWebSession;

@NotThreadSafe
public class CasLogoutSuccessPage extends AWebPage {

    public static final String MOUNT_PATH = CasProperties.MOUNT_PATH_CAS_LOGOUT_SUCCESS;

    public CasLogoutSuccessPage() {
        super(null);
        AWebSession.get().invalidate();
        throw new RestartResponseException(ABaseWebApplication.get().getSignOutPage());
    }

}
