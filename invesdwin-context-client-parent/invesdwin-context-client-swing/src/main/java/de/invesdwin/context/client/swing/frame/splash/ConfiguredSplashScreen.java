package de.invesdwin.context.client.swing.frame.splash;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.NotThreadSafe;

import org.jdesktop.application.Application;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.richclient.application.ProgressMonitoringBeanFactoryPostProcessor;
import org.springframework.richclient.application.splash.MonitoringSplashScreen;
import org.springframework.richclient.application.splash.ProgressSplashScreen;
import org.springframework.richclient.application.splash.SplashScreen;
import org.springframework.richclient.progress.ProgressMonitor;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.beans.init.MergedContext;
import de.invesdwin.context.client.swing.frame.RichApplicationProperties;
import de.invesdwin.context.client.swing.frame.app.DelegateRichApplication;
import de.invesdwin.util.lang.reflection.Reflections;

@NotThreadSafe
public final class ConfiguredSplashScreen implements SplashScreen, FactoryBean<ConfiguredSplashScreen> {

    public static final ConfiguredSplashScreen INSTANCE = new ConfiguredSplashScreen();

    static {
        if (Reflections.JAVA_VERSION < 13) {
            //gtk3 looks wrong in a lot of places in openjdk-11, fix is supposed to arrive with java 13
            //https://bugs.openjdk.java.net/browse/JDK-8203627?attachmentOrder=desc
            //CHECKSTYLE:OFF
            //don't use SystemProperties class to not invoke ContextProperties loading too early
            System.setProperty("jdk.gtk.version", "2");
            //CHECKSTYLE:ON
        }
        if (Reflections.JAVA_DEBUG_MODE) {
            //without this workaround swing debugging might hang on linux
            //https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6714678
            //CHECKSTYLE:OFF
            //don't use SystemProperties class to not invoke ContextProperties loading too early
            System.setProperty("sun.awt.disablegrab", "true");
            //CHECKSTYLE:ON
        }
    }

    @GuardedBy("ConfiguredSplashScreen.class")
    private static ProgressSplashScreen splashScreen;
    protected boolean showing;

    private ConfiguredSplashScreen() {}

    public void splash(final boolean force) {
        if (RichApplicationProperties.isWindowBuilder()) {
            return;
        }
        synchronized (ConfiguredSplashScreen.class) {
            if (splashScreen == null || force) {
                dispose();

                if (RichApplicationProperties.hasDelegateClass()) {
                    final ApplicationMessageSource messageSource = new ApplicationMessageSource();
                    ConfiguredSplashScreen.splashScreen = new ProgressSplashScreen();
                    splashScreen.setMessageSource(messageSource);

                    splashScreen.setImageResourcePath(new ClassPathResource(splashScreen.getMessageSource()
                            .getMessage(DelegateRichApplication.KEY_APPLICATION_SPLASH, null, null)));
                    splashScreen.setIconResourcePath(
                            splashScreen.getMessageSource().getMessage(Application.KEY_APPLICATION_ICON, null, null));

                    if (!RichApplicationProperties.isHideSplashOnStartup()) {
                        try {
                            EventDispatchThreadUtil.invokeAndWait(new Runnable() {
                                @Override
                                public void run() {
                                    showing = true;
                                    splashScreen.splash();
                                }
                            });
                        } catch (final Exception e) {
                            throw new RuntimeException("EDT threading issue while showing splash screen", e);
                        }
                        final ProgressMonitor tracker = ((MonitoringSplashScreen) splashScreen).getProgressMonitor();
                        MergedContext.autowire(new ProgressMonitoringBeanFactoryPostProcessor(tracker, messageSource));
                    }
                }
            }
        }
    }

    @Override
    public void splash() {
        splash(false);
    }

    @Override
    public void dispose() {
        synchronized (ConfiguredSplashScreen.class) {
            if (splashScreen != null) {
                try {
                    EventDispatchThreadUtil.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            splashScreen.dispose();
                            showing = false;
                        }
                    });
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public ConfiguredSplashScreen getObject() throws Exception {
        return INSTANCE;
    }

    @Override
    public Class<?> getObjectType() {
        return getClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public boolean isShowing() {
        return showing;
    }

}
