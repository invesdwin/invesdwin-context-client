package de.invesdwin.context.client.vaadin.internal;

import java.util.Properties;

import javax.annotation.concurrent.ThreadSafe;

import com.vaadin.server.Constants;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.VaadinServlet;

import de.invesdwin.context.ContextProperties;
import de.invesdwin.context.system.properties.SystemProperties;

@ThreadSafe
public class DelegateVaadinServlet extends VaadinServlet {

    static {
        final SystemProperties systemProperties = new SystemProperties();
        final String keyProductionMode = Constants.SERVLET_PARAMETER_PRODUCTION_MODE;
        if (!systemProperties.containsKey(keyProductionMode)) {
            final boolean productionMode = !ContextProperties.IS_TEST_ENVIRONMENT;
            systemProperties.setBoolean(keyProductionMode, productionMode);
        }
    }

    @Override
    protected DeploymentConfiguration createDeploymentConfiguration(final Properties initParameters) {
        if (!initParameters.containsKey(Constants.SERVLET_PARAMETER_PRODUCTION_MODE)) {
            initParameters.setProperty(Constants.SERVLET_PARAMETER_PRODUCTION_MODE,
                    new SystemProperties().getString(Constants.SERVLET_PARAMETER_PRODUCTION_MODE));
        }
        return super.createDeploymentConfiguration(initParameters);
    }

}
