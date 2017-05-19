package de.invesdwin.context.client.wicket.saml;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.security.web.saml.SamlProperties;
import de.invesdwin.nowicket.application.auth.Roles;

@Immutable
public final class SamlRoles {

    private SamlRoles() {}

    public static boolean isSamlAuthenticated() {
        return Roles.evaluateExpression("hasRole('" + SamlProperties.ROLE_SAML_AUTHENTICATED + "')");
    }

}
