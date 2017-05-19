package de.invesdwin.context.client.wicket.internal;

import java.io.File;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.wicket.RuntimeConfigurationType;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import de.invesdwin.context.ContextProperties;
import de.invesdwin.context.beans.init.MergedContext;
import de.invesdwin.context.system.properties.SystemProperties;
import de.invesdwin.nowicket.application.IWebApplicationConfig;
import de.invesdwin.nowicket.application.WebApplicationConfigSupport;
import de.invesdwin.nowicket.application.filter.AWebApplication;
import de.invesdwin.nowicket.application.filter.init.hook.IWebApplicationInitializerHook;

@ThreadSafe
public class DelegateWebApplication extends AWebApplication {

    static {
        final SystemProperties systemProperties = new SystemProperties();
        final String keyWicketConfiguration = "wicket." + CONFIGURATION;
        if (!systemProperties.containsKey(keyWicketConfiguration)) {
            final String wicketConfiguration;
            if (!ContextProperties.IS_TEST_ENVIRONMENT) {
                wicketConfiguration = RuntimeConfigurationType.DEPLOYMENT.toString();
            } else {
                wicketConfiguration = RuntimeConfigurationType.DEVELOPMENT.toString();
            }
            systemProperties.setString(keyWicketConfiguration, wicketConfiguration);
        }
    }

    @Override
    protected void init() {
        MergedContext.autowire(this);
        super.init();
    }

    @Override
    protected IWebApplicationConfig newConfig() {
        return staticResolveDelegate();
    }

    public static IWebApplicationConfig staticResolveDelegate() {
        try {
            return MergedContext.getInstance().getBean(IWebApplicationConfig.class);
        } catch (final NoSuchBeanDefinitionException e) {
            if (ContextProperties.IS_TEST_ENVIRONMENT) {
                return new WebApplicationConfigSupport();
            } else {
                throw e;
            }
        }
    }

    @Override
    public Iterable<IWebApplicationInitializerHook> getWebApplicationInitializerHooks() {
        final Map<String, IWebApplicationInitializerHook> beansOfType = MergedContext.getInstance()
                .getBeansOfType(IWebApplicationInitializerHook.class);
        return beansOfType.values();
    }

    @Override
    public String getSessionEncryptionKey() {
        return "3LJVpcMBUj2cZpDdRd9ykvNhER3HBZ49A59TmLZVfE9sF884Mx7AAXGgZmCJ2UfM";
    }

    @Override
    public Set<String> getClasspathBasePackages() {
        return ContextProperties.getBasePackages();
    }

    @Override
    public File getSessionsDirectory() {
        return ContextProperties.TEMP_DIRECTORY;
    }

}
