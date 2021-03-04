package de.invesdwin.context.client.swing.frame;

import java.awt.Toolkit;
import java.beans.Beans;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceManager;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.utils.PlatformType;
import org.springframework.beans.factory.config.BeanDefinition;

import de.invesdwin.context.ContextProperties;
import de.invesdwin.context.beans.init.MergedContext;
import de.invesdwin.context.client.swing.api.IRichApplication;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.frame.app.DelegateRichApplication;
import de.invesdwin.context.log.Log;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.error.Throwables;
import de.invesdwin.util.lang.Strings;
import de.invesdwin.util.lang.reflection.Reflections;

@ThreadSafe
public final class RichApplicationProperties {

    private static final Log LOG = new Log(RichApplicationProperties.class);
    private static final String UNDEFINED_APPLICATION_ID = "[Application.name]";
    @GuardedBy("this.class")
    private static Application designTimeApplication;
    private static volatile boolean hideSplashOnStartup;
    private static volatile Class<? extends IRichApplication> delegateClass;
    private static volatile String[] initializationArgs;
    private static Boolean windowBuilder;

    private RichApplicationProperties() {
    }

    public static synchronized Application getDesignTimeApplication() {
        if (designTimeApplication == null) {
            final boolean prevDesignTime = Beans.isDesignTime();
            Beans.setDesignTime(true);
            try {
                //maybe initialize DesignTimeApplication to grant access to resourcemap and other stuff when application itself is not needed actually here
                designTimeApplication = DelegateRichApplication.getInstance();
                initApplicatonBundleNames(designTimeApplication, false);
            } finally {
                Beans.setDesignTime(prevDesignTime);
            }
        }
        return designTimeApplication;
    }

    public static boolean isHideSplashOnStartup() {
        return hideSplashOnStartup;
    }

    public static void setHideSplashOnStartup(final boolean hideSplashOnStartup) {
        RichApplicationProperties.hideSplashOnStartup = hideSplashOnStartup;
    }

    public static Class<? extends IRichApplication> getDelegateClass(final boolean force) {
        if (delegateClass == null) {
            delegateClass = getDelegateClassFromServiceLoader();
            if (delegateClass == null) {
                delegateClass = getDelegateClassFromMergedContext(force);
            }
            if (designTimeApplication != null) {
                initApplicatonBundleNames(designTimeApplication, true);
            }
        }
        return delegateClass;
    }

    private static Class<? extends IRichApplication> getDelegateClassFromMergedContext(final boolean force) {
        if (MergedContext.getInstance() == null) {
            if (force) {
                MergedContext.autowire(null);
            } else {
                return null;
            }
        }
        final String[] beanNames = MergedContext.getInstance().getBeanNamesForType(IRichApplication.class);
        Assertions.assertThat(beanNames.length)
                .as("Exactly one bean of type [%s] must exist: %s", IRichApplication.class.getSimpleName(),
                        Arrays.toString(beanNames))
                .isEqualTo(1);
        final BeanDefinition beanDefinition = MergedContext.getInstance().getBeanDefinition(beanNames[0]);
        return Reflections.classForName(beanDefinition.getBeanClassName());
    }

    private static Class<? extends IRichApplication> getDelegateClassFromServiceLoader() {
        final ServiceLoader<IRichApplication> loader = ServiceLoader.load(IRichApplication.class);
        final List<IRichApplication> services = new ArrayList<>();
        for (final IRichApplication service : loader) {
            services.add(service);
        }
        if (services.isEmpty()) {
            return null;
        } else {
            final IRichApplication firstService = services.get(0);
            if (services.size() > 1) {
                LOG.error("More than one %s service provider available, using first one: ",
                        IRichApplication.class.getSimpleName(), firstService.getClass().getName());
            }
            return firstService.getClass();
        }
    }

    public static boolean hasDelegateClass() {
        try {
            return getDelegateClass(true) != null;
        } catch (final Throwable t) {
            return false;
        }
    }

    public static IRichApplication getDelegate() {
        if (MergedContext.getInstance() == null) {
            MergedContext.autowire(null);
        }
        return MergedContext.getInstance().getBean(RichApplicationProperties.getDelegateClass(true));
    }

    public static void setDelegateClass(final Class<? extends IRichApplication> delegateClass) {
        RichApplicationProperties.delegateClass = delegateClass;
        if (designTimeApplication != null) {
            initApplicatonBundleNames(designTimeApplication, false);
        }
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
        designTimeApplication = null;
    }

    public static void initApplicatonBundleNames(final Application application, final boolean forceDelegateClass) {
        final ResourceManager resourceManager = application.getContext().getResourceManager();
        final List<String> applicationBundleNames = new ArrayList<String>(resourceManager.getApplicationBundleNames());

        //DelegateRichApplication properties must be loaded in any case
        if (!applicationBundleNames.contains(DelegateRichApplication.class.getName())) {
            applicationBundleNames.add(0, DelegateRichApplication.class.getName());
        }

        //Use the interface implementation properties first in chain
        final String delegateClassName = getDelegateClass(forceDelegateClass).getName();
        if (!applicationBundleNames.contains(delegateClassName)) {
            applicationBundleNames.add(0, delegateClassName);
        }
        resourceManager.setApplicationBundleNames(applicationBundleNames);

        //reset app resource map
        final PlatformType prevPlatform = resourceManager.getResourceMap().getPlatform();
        Reflections.field("appResourceMap").ofType(ResourceMap.class).in(resourceManager).set(null);
        final ResourceMap appResourceMap = resourceManager.getResourceMap();
        if (prevPlatform != null) {
            appResourceMap.setPlatform(prevPlatform);
        }
        resourceManager.setResourceFolder(null);
        //https://locademiaz.wordpress.com/2011/08/30/turn-your-java-apps-gnome-shell-friendly/
        final String applicationName = appResourceMap.getString(DelegateRichApplication.KEY_APPLICATION_NAME);
        if (Strings.isNotBlank(applicationName)) {
            final Toolkit toolkit = Toolkit.getDefaultToolkit();
            try {
                Reflections.field("awtAppClassName").ofType(String.class).in(toolkit).set(applicationName);
            } catch (final Throwable t) {
                //ignore, might not be X-Windows
            }
        }
    }

    public static File getStorageDirectory() {
        //use design time application so that the resource bundle is properly initialized
        final ApplicationContext context = getDesignTimeApplication().getContext();
        final String applicationId = GuiService.i18n(context.getResourceMap(), "Application.id",
                context.getApplicationClass().getSimpleName());
        if (UNDEFINED_APPLICATION_ID.equals(applicationId)) {
            throw new IllegalStateException("Please override Application.id: " + applicationId);
        }
        return new File(ContextProperties.getHomeDirectory(), applicationId);
    }

    /**
     * This is used to detect if the widgets are running from SWT designer, because in this case we have to skip some
     * initialization code.
     * 
     * https://stackoverflow.com/questions/3844608/detect-window-builder/4030788#4030788
     */
    public static boolean isWindowBuilder() {
        if (windowBuilder == null) {
            final String s = Throwables.getFullStackTrace(new RuntimeException("Just to get the Stacktrace."));
            windowBuilder = s.contains("org.eclipse.wb.");
        }
        return windowBuilder;
    }

}
