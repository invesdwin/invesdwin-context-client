package de.invesdwin.client.wicket.examples;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;
import jakarta.inject.Inject;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.LoginContext;

import org.apache.commons.io.IOUtils;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.kerberos.client.config.SunJaasKrb5LoginConfig;

import de.invesdwin.context.ContextProperties;
import de.invesdwin.context.client.wicket.examples.secure.kerberos.SpringKerberosSecurePage;
import de.invesdwin.context.integration.IntegrationProperties;
import de.invesdwin.context.security.kerberos.Keytabs;
import de.invesdwin.context.security.kerberos.ProxyEnabledKerberosRestTemplate;
import de.invesdwin.context.security.ldap.directory.server.DirectoryServer;
import de.invesdwin.context.security.ldap.directory.server.test.DirectoryServerTest;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.webserver.test.WebserverTest;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.Files;

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
        Assertions.checkEquals(1, subject.getPrincipals().size());
        Assertions.checkEquals(KerberosPrincipal.class, subject.getPrincipals().iterator().next().getClass());
        Assertions.checkEquals(PRINCIPAL, subject.getPrincipals().iterator().next().getName());
        loginContext.logout();
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

    @Test
    @Disabled("does not seem to work properly with apacheds; works fine with MIT Kerberos")
    public void testKerberosRestTemplate() throws Exception {
        //with keytab
        final String responseFromKeytab = new ProxyEnabledKerberosRestTemplate(keytab, PRINCIPAL).getForObject(
                IntegrationProperties.WEBSERVER_BIND_URI + "/" + SpringKerberosSecurePage.MOUNT_PATH, String.class);
        Assertions.assertThat(responseFromKeytab).contains("Kerberos Authentication");

        //from ticket cache
        final String responseFromTicketCache = new ProxyEnabledKerberosRestTemplate().getForObject(
                IntegrationProperties.WEBSERVER_BIND_URI + "/" + SpringKerberosSecurePage.MOUNT_PATH, String.class);
        Assertions.assertThat(responseFromTicketCache).contains("Kerberos Authentication");
    }

    /**
     * https://stackoverflow.com/questions/24633380/how-do-i-authenticate-with-spnego-kerberos-and-apaches-httpclient
     */
    @SuppressWarnings("deprecation")
    @Test
    @Disabled("does not seem to work")
    public void testKerberosHttpClient() throws Exception {
        // Depending on your AD configuration, this file is not needed
        final File krb5Config = createKrb5Configuration("INVESDWIN.DE",
                IntegrationProperties.WEBSERVER_BIND_URI.getHost());
        final String url = IntegrationProperties.WEBSERVER_BIND_URI + "/" + SpringKerberosSecurePage.MOUNT_PATH;

        //CHECKSTYLE:OFF
        System.out.println("Created config '" + krb5Config.getAbsolutePath() + "':");
        System.out.println(Files.readFileToString(krb5Config, Charset.defaultCharset()));

        System.setProperty("java.security.krb5.conf", krb5Config.toURI().toString());
        System.setProperty("sun.security.krb5.debug", "true");
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        //CHECKSTYLE:ON

        final Lookup<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider> create()
                .register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory(true))
                .build();

        final CloseableHttpClient client = HttpClients.custom()
                .setDefaultAuthSchemeRegistry(authSchemeRegistry)
                .build();
        final HttpClientContext context = HttpClientContext.create();
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        // This may seem odd, but specifying 'null' as principal tells java to use the logged in user's credentials
        final Credentials useJaasCreds = new Credentials() {

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public Principal getUserPrincipal() {
                return null;
            }

        };
        credentialsProvider.setCredentials(new AuthScope(null, -1, null), useJaasCreds);
        context.setCredentialsProvider(credentialsProvider);

        final HttpGet httpget = new HttpGet(url);
        final CloseableHttpResponse response = client.execute(httpget, context);
        final String responseString = IOUtils.toString(response.getEntity().getContent());
        Assertions.assertThat(responseString).contains("Kerberos Authentication");
    }

    @SuppressWarnings("deprecation")
    private static File createKrb5Configuration(final String domain, final String kdc) throws IOException {
        final File tempFile = new File(ContextProperties.TEMP_DIRECTORY, "krb5.kdc");
        final ArrayList<String> lines = new ArrayList<>();
        lines.add("[libdefaults]");
        lines.add("\tdefault_realm = " + domain);
        lines.add("[realms]");
        lines.add("\t" + domain + " = {");
        lines.add("\t\tkdc = " + kdc);
        lines.add("\t\tadmin_server = " + kdc);
        lines.add("\t}");
        final FileWriter writer = new FileWriter(tempFile);
        IOUtils.writeLines(lines, System.lineSeparator(), writer);
        IOUtils.closeQuietly(writer);
        return tempFile;
    }

}
