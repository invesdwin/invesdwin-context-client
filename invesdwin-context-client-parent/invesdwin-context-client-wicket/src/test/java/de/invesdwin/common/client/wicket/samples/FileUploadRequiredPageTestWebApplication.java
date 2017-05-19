package de.invesdwin.common.client.wicket.samples;

// @ThreadSafe
public class FileUploadRequiredPageTestWebApplication extends org.apache.wicket.protocol.http.WebApplication {

    public FileUploadRequiredPageTestWebApplication() {}

    @Override
    public Class<FileUploadRequiredPage> getHomePage() {
        return FileUploadRequiredPage.class;
    }

    @Override
    protected void outputDevelopmentModeWarning() {}

}
