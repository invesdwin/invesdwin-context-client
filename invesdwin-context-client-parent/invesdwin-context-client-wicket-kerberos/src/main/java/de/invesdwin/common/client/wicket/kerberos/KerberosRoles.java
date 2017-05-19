package de.invesdwin.common.client.wicket.kerberos;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.security.kerberos.KerberosProperties;
import de.invesdwin.nowicket.application.auth.Roles;

@Immutable
public final class KerberosRoles {

    private KerberosRoles() {}

    public static boolean isKerberosAuthenticated() {
        return Roles.evaluateExpression("hasRole('" + KerberosProperties.ROLE_KERBEROS_AUTHENTICATED + "')");
    }

}
