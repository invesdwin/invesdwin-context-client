package de.invesdwin.context.client.wicket.examples.secure.cas;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.wicket.cas.CasRoles;
import de.invesdwin.context.client.wicket.examples.guestbook.GuestbookExample;
import de.invesdwin.context.client.wicket.examples.secure.ExampleRoles;
import de.invesdwin.nowicket.generated.markup.annotation.GeneratedMarkup;
import de.invesdwin.util.bean.AValueObject;

@NotThreadSafe
@GeneratedMarkup
public class SpringCasSecure extends AValueObject {

    public boolean isAdmin() {
        return ExampleRoles.isAdmin();
    }

    public boolean isCasAuthentication() {
        return CasRoles.isCasAuthenticated();
    }

    public GuestbookExample home() throws Exception {
        return new GuestbookExample();
    }

}
