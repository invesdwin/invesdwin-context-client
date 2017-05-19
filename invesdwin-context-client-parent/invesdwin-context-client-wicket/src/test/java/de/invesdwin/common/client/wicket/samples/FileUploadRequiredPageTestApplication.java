package de.invesdwin.common.client.wicket.samples;

import javax.annotation.concurrent.Immutable;

import org.apache.wicket.markup.html.WebPage;

import de.invesdwin.nowicket.application.WebApplicationConfigSupport;

@Immutable
public class FileUploadRequiredPageTestApplication extends WebApplicationConfigSupport {

    @Override
    public Class<? extends WebPage> getHomePage() {
        return FileUploadRequiredPage.class;
    }

}
