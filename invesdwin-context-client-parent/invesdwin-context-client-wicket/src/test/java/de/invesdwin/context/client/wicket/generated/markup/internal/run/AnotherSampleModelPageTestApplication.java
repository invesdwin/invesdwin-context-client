package de.invesdwin.context.client.wicket.generated.markup.internal.run;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.wicket.markup.html.WebPage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import de.invesdwin.context.client.wicket.generated.markup.internal.AnotherSampleModelPage;
import de.invesdwin.nowicket.application.WebApplicationConfigSupport;
import de.invesdwin.nowicket.application.auth.IAuthenticationService;
import de.invesdwin.nowicket.security.spring.SpringSecurityAuthenticationService;

@ThreadSafe
public class AnotherSampleModelPageTestApplication extends WebApplicationConfigSupport {

    @Override
    public Class<? extends WebPage> getHomePage() {
        return AnotherSampleModelPage.class;
    }

    @Override
    public IAuthenticationService getAuthenticationService() {
        return new SpringSecurityAuthenticationService(new AuthenticationManager() {
            @Override
            public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
                return authentication;
            }
        });
    }

}
