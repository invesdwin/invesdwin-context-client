package de.invesdwin.context.client.wicket.internal;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.context.beans.init.MergedContext;

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start
 * class.
 * 
 * @see remoteliste.Start#main(String[])
 */
@ThreadSafe
public class SimpleHomePageTestWebApplication extends org.apache.wicket.protocol.http.WebApplication {
    /**
     * Constructor
     */
    public SimpleHomePageTestWebApplication() {}

    @Override
    protected void init() {
        MergedContext.autowire(this);
        super.init();
    }

    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<SimpleHomePage> getHomePage() {
        return SimpleHomePage.class;
    }

    @Override
    protected void outputDevelopmentModeWarning() {}

}
