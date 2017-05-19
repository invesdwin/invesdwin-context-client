package de.invesdwin.context.client.wicket.saml.page;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.security.web.saml.SamlProperties;
import de.invesdwin.nowicket.application.AWebPage;

@NotThreadSafe
public class SamlLoginPage extends AWebPage {

    public static final String MOUNT_PATH = SamlProperties.MOUNT_PATH_SAML_LOGIN;

    public SamlLoginPage() {
        super(null);
    }

}
