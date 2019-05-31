package de.invesdwin.context.client.swing.impl;

import java.util.Arrays;

import javax.annotation.concurrent.ThreadSafe;

import org.springframework.beans.factory.config.BeanDefinition;

import de.invesdwin.context.beans.init.MergedContext;
import de.invesdwin.context.client.swing.api.IRichApplication;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.Reflections;

@ThreadSafe
public final class RichApplicationProperties {

    private static volatile boolean hideSplashOnStartup;
    private static volatile Class<? extends IRichApplication> delegateClass;
    private static volatile String[] initializationArgs;

    private RichApplicationProperties() {}

    public static boolean isHideSplashOnStartup() {
        return hideSplashOnStartup;
    }

    public static void setHideSplashOnStartup(final boolean hideSplashOnStartup) {
        RichApplicationProperties.hideSplashOnStartup = hideSplashOnStartup;
    }

    public static Class<? extends IRichApplication> getDelegateClass() {
        if (delegateClass == null) {
            if (MergedContext.getInstance() == null) {
                MergedContext.autowire(null);
            }
            final String[] beanNames = MergedContext.getInstance().getBeanNamesForType(IRichApplication.class);
            Assertions.assertThat(beanNames.length)
                    .as("Exactly one bean of type [%s] must exist: %s", IRichApplication.class.getSimpleName(),
                            Arrays.toString(beanNames))
                    .isEqualTo(1);
            final BeanDefinition beanDefinition = MergedContext.getInstance().getBeanDefinition(beanNames[0]);
            delegateClass = Reflections.classForName(beanDefinition.getBeanClassName());
        }
        return delegateClass;
    }

    public static boolean hasDelegateClass() {
        try {
            return getDelegateClass() != null;
        } catch (final Throwable t) {
            return false;
        }
    }

    public static IRichApplication getDelegate() {
        if (MergedContext.getInstance() == null) {
            MergedContext.autowire(null);
        }
        return MergedContext.getInstance().getBean(RichApplicationProperties.getDelegateClass());
    }

    public static void setDelegateClass(final Class<? extends IRichApplication> delegateClass) {
        RichApplicationProperties.delegateClass = delegateClass;
    }

    public static String[] getInitializationArgs() {
        return initializationArgs.clone();
    }

    public static void setInitializationArgs(final String[] initializationArgs) {
        RichApplicationProperties.initializationArgs = initializationArgs;
    }

    public static void reset() {
        setHideSplashOnStartup(false);
        setDelegateClass(null);
        setInitializationArgs(null);
    }

}
