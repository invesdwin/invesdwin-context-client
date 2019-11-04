package de.invesdwin.context.client.wicket.internal;

import java.io.IOException;

import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;

import de.invesdwin.context.integration.IntegrationProperties;
import de.invesdwin.nowicket.application.IWebApplicationConfig;
import de.invesdwin.nowicket.application.filter.AWicketFilter;

@NotThreadSafe
public class DelegateWicketFilter extends AWicketFilter {

    @Override
    protected IWebApplicationConfig newConfig() {
        return DelegateWebApplication.staticResolveDelegate();
    }

    @Override
    protected boolean processRequestCycle(final RequestCycle requestCycle, final WebResponse webResponse,
            final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse,
            final FilterChain chain) throws IOException, ServletException {
        final boolean registerRetryDisabled = IntegrationProperties.registerThreadRetryDisabled();
        try {
            return super.processRequestCycle(requestCycle, webResponse, httpServletRequest, httpServletResponse, chain);
        } finally {
            IntegrationProperties.unregisterThreadRetryDisabled(registerRetryDisabled);
        }
    }

}
