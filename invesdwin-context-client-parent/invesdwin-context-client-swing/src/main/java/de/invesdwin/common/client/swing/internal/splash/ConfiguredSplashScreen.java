package de.invesdwin.common.client.swing.internal.splash;

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
import de.invesdwin.common.client.swing.internal.app.DelegateRichApplication;
import de.invesdwin.context.beans.init.MergedContext;

@NotThreadSafe
public final class ConfiguredSplashScreen implements SplashScreen, FactoryBean<ConfiguredSplashScreen> {

    public static final ConfiguredSplashScreen INSTANCE = new ConfiguredSplashScreen();

    @GuardedBy("this.class")
    private static ProgressSplashScreen splashScreen;

    private ConfiguredSplashScreen() {}

    public void splash(final boolean force) {
        synchronized (ConfiguredSplashScreen.class) {
            if (splashScreen == null || force) {
                dispose();

                final ApplicationMessageSource messageSource = new ApplicationMessageSource();

                splashScreen = new ProgressSplashScreen();
                splashScreen.setMessageSource(messageSource);

                splashScreen.setImageResourcePath(new ClassPathResource(splashScreen.getMessageSource()
                        .getMessage(DelegateRichApplication.KEY_APPLICATION_SPLASH, null, null)));
                splashScreen.setIconResourcePath(
                        splashScreen.getMessageSource().getMessage(Application.KEY_APPLICATION_ICON, null, null));
                final ProgressMonitor tracker = ((MonitoringSplashScreen) splashScreen).getProgressMonitor();
                MergedContext.autowire(new ProgressMonitoringBeanFactoryPostProcessor(tracker, messageSource));

                try {
                    EventDispatchThreadUtil.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            splashScreen.splash();
                        }
                    });
                } catch (final Exception e) {
                    throw new RuntimeException("EDT threading issue while showing splash screen", e);
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

}
