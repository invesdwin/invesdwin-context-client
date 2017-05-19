package de.invesdwin.context.client.wicket.internal;

import javax.annotation.concurrent.Immutable;

import org.apache.wicket.markup.html.WebPage;

import de.invesdwin.nowicket.application.WebApplicationConfigSupport;

@Immutable
public class SimpleHomePageTestApplication extends WebApplicationConfigSupport {

    @Override
    public Class<? extends WebPage> getHomePage() {
        return SimpleHomePage.class;
    }

}
