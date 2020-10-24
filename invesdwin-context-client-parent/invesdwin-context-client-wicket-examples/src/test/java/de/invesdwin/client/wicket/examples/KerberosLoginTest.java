package de.invesdwin.client.wicket.examples;

import java.io.File;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.LoginContext;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.kerberos.client.config.SunJaasKrb5LoginConfig;

import de.invesdwin.context.client.wicket.examples.secure.kerberos.SpringKerberosSecurePage;
import de.invesdwin.context.integration.IntegrationProperties;
import de.invesdwin.context.security.kerberos.Keytabs;
import de.invesdwin.context.security.kerberos.ProxyEnabledKerberosRestTemplate;
import de.invesdwin.context.security.ldap.directory.server.DirectoryServer;
import de.invesdwin.context.security.ldap.directory.server.test.DirectoryServerTest;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.webserver.test.WebserverTest;
import de.invesdwin.util.assertions.Assertions;

@WebserverTest
@NotThreadSafe
@DirectoryServerTest
public class KerberosLoginTest extends ATest {

    public static final String PASSPHRASE = "kus3rFTW";
    public static final String PRINCIPAL = "kuser@INVESDWIN.DE";
    private static File keytab;
    @Inject
    private DirectoryServer directoryServer;

    @Override
    public void setUpOnce() throws Exception {
        super.setUpOnce();
        directoryServer.createKerberosPrincipal(PRINCIPAL, PASSPHRASE);
        keytab = Keytabs.createKeytab(PRINCIPAL, PASSPHRASE);
    }

    @Test
    public void testKerberosLogin() throws Exception {
        final Set<Principal> principals = new HashSet<Principal>();
        principals.add(new KerberosPrincipal(PRINCIPAL));

        // client login
        Subject subject = new Subject(false, principals, new HashSet<Object>(), new HashSet<Object>());
        final LoginContext loginContext = new LoginContext("", subject, null, createClientConfig(PRINCIPAL, keytab));
        loginContext.login();
        subject = loginContext.getSubject();
        org.junit.Assert.assertEquals(1, subject.getPrincipals().size());
        org.junit.Assert.assertEquals(KerberosPrincipal.class, subject.getPrincipals().iterator().next().getClass());
        org.junit.Assert.assertEquals(PRINCIPAL, subject.getPrincipals().iterator().next().getName());
        loginContext.logout();
    }

    @Test
    @Ignore("does not seem to work properly with apacheds; works fine with MIT Kerberos")
    public void testKerberosRestTemplate() throws InterruptedException {
        //with keytab
        final String responseFromKeytab = new ProxyEnabledKerberosRestTemplate(keytab, PRINCIPAL).getForObject(
                IntegrationProperties.WEBSERVER_BIND_URI + "/" + SpringKerberosSecurePage.MOUNT_PATH, String.class);
        Assertions.assertThat(responseFromKeytab).contains("Kerberos Authentication");

        //from ticket cache
        final String responseFromTicketCache = new ProxyEnabledKerberosRestTemplate().getForObject(
                IntegrationProperties.WEBSERVER_BIND_URI + "/" + SpringKerberosSecurePage.MOUNT_PATH, String.class);
        Assertions.assertThat(responseFromTicketCache).contains("Kerberos Authentication");
    }

    private SunJaasKrb5LoginConfig createClientConfig(final String servicePrincipal, final File keytab)
            throws Exception {
        final SunJaasKrb5LoginConfig config = createConfig(servicePrincipal, keytab);
        config.setIsInitiator(true);
        return config;
    }

    private SunJaasKrb5LoginConfig createConfig(final String servicePrincipal, final File keytab) throws Exception {
        final SunJaasKrb5LoginConfig config = new SunJaasKrb5LoginConfig();
        config.setKeyTabLocation(new FileSystemResource(keytab));
        config.setServicePrincipal(servicePrincipal);
        config.setDebug(true);
        config.afterPropertiesSet();
        return config;
    }
}
