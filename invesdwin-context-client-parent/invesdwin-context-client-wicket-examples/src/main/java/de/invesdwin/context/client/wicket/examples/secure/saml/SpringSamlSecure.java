package de.invesdwin.context.client.wicket.examples.secure.saml;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.wicket.examples.guestbook.GuestbookExample;
import de.invesdwin.context.client.wicket.examples.secure.ExampleRoles;
import de.invesdwin.context.client.wicket.saml.SamlRoles;
import de.invesdwin.nowicket.generated.markup.annotation.GeneratedMarkup;
import de.invesdwin.util.bean.AValueObject;

@NotThreadSafe
@GeneratedMarkup
public class SpringSamlSecure extends AValueObject {

    public boolean isAdmin() {
        return ExampleRoles.isAdmin();
    }

    public boolean isSamlAuthentication() {
        return SamlRoles.isSamlAuthenticated();
    }

    public GuestbookExample home() throws Exception {
        return new GuestbookExample();
    }

}
