package de.invesdwin.context.client.wicket.examples.secure.kerberos;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.wicket.examples.guestbook.GuestbookExample;
import de.invesdwin.context.client.wicket.examples.secure.ExampleRoles;
import de.invesdwin.context.client.wicket.kerberos.KerberosRoles;
import de.invesdwin.nowicket.generated.markup.annotation.GeneratedMarkup;
import de.invesdwin.util.bean.AValueObject;

@NotThreadSafe
@GeneratedMarkup
public class SpringKerberosSecure extends AValueObject {

    public boolean isAdmin() {
        return ExampleRoles.isAdmin();
    }

    public boolean isKerberosAuthentication() {
        return KerberosRoles.isKerberosAuthenticated();
    }

    public GuestbookExample home() throws Exception {
        return new GuestbookExample();
    }

}
