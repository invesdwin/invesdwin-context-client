package de.invesdwin.context.client.wicket.examples;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.wicket.markup.html.WebPage;
import org.springframework.security.authentication.AuthenticationManager;

import de.invesdwin.context.client.wicket.examples.guestbook.GuestbookExamplePage;
import de.invesdwin.nowicket.application.WebApplicationConfigSupport;
import de.invesdwin.nowicket.application.auth.IAuthenticationService;
import de.invesdwin.nowicket.security.spring.SpringSecurityAuthenticationService;

@NotThreadSafe
@Named
public class ExampleWebApplication extends WebApplicationConfigSupport {

    @Inject
    @Named("exampleAuthenticationManager")
    private AuthenticationManager authenticationManager;

    @Override
    public Class<? extends WebPage> getHomePage() {
        return GuestbookExamplePage.class;
    }

    @Override
    public IAuthenticationService getAuthenticationService() {
        return new SpringSecurityAuthenticationService(authenticationManager);
    }

}
