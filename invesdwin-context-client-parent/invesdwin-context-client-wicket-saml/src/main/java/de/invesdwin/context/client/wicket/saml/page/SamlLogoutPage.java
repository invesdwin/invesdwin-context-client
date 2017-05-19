package de.invesdwin.context.client.wicket.saml.page;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.security.web.saml.SamlProperties;
import de.invesdwin.nowicket.application.AWebPage;

@NotThreadSafe
public class SamlLogoutPage extends AWebPage {

    public static final String MOUNT_PATH = SamlProperties.MOUNT_PATH_SAML_LOGOUT;

    public SamlLogoutPage() {
        super(null);
    }

}
