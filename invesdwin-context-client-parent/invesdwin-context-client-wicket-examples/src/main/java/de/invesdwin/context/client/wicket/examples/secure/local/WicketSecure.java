package de.invesdwin.context.client.wicket.examples.secure.local;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.wicket.examples.guestbook.GuestbookExample;
import de.invesdwin.nowicket.application.auth.Roles;
import de.invesdwin.nowicket.generated.markup.annotation.GeneratedMarkup;
import de.invesdwin.util.bean.AValueObject;

@NotThreadSafe
@GeneratedMarkup
public class WicketSecure extends AValueObject {

    public boolean isAdmin() {
        return Roles.get().hasRole(Roles.ADMIN);
    }

    public GuestbookExample home() throws Exception {
        return new GuestbookExample();
    }

}
