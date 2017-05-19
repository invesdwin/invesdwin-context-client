package de.invesdwin.context.client.wicket.cas;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.security.web.cas.CasProperties;
import de.invesdwin.nowicket.application.auth.Roles;

@Immutable
public final class CasRoles {

    private CasRoles() {}

    public static boolean isCasAuthenticated() {
        return Roles.evaluateExpression("hasRole('" + CasProperties.ROLE_CAS_AUTHENTICATED + "')");
    }

}
