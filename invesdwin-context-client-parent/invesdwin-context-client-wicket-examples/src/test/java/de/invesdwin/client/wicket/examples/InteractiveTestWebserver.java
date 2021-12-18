package de.invesdwin.client.wicket.examples;

import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import de.invesdwin.context.security.ldap.directory.server.DirectoryServer;
import de.invesdwin.context.security.ldap.directory.server.test.DirectoryServerTest;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.webserver.test.WebserverTest;

@WebserverTest
@NotThreadSafe
@DirectoryServerTest
public class InteractiveTestWebserver extends ATest {

    @Inject
    private DirectoryServer directoryServer;

    @Override
    public void setUpOnce() throws Exception {
        super.setUpOnce();
        directoryServer.createKerberosPrincipal(KerberosLoginTest.PRINCIPAL, KerberosLoginTest.PASSPHRASE);
    }

    @Test
    public void test() throws Exception {
        TimeUnit.DAYS.sleep(Long.MAX_VALUE);
    }

}
