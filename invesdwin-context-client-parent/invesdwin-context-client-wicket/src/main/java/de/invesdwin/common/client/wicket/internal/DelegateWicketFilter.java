package de.invesdwin.common.client.wicket.internal;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.nowicket.application.IWebApplicationConfig;
import de.invesdwin.nowicket.application.filter.AWicketFilter;

@NotThreadSafe
public class DelegateWicketFilter extends AWicketFilter {

    @Override
    protected IWebApplicationConfig newConfig() {
        return DelegateWebApplication.staticResolveDelegate();
    }

}
