package de.invesdwin.context.client.swing.impl.splash;

import java.util.Locale;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jdesktop.application.Application;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.richclient.application.ProgressMonitoringBeanFactoryPostProcessor;
import org.springframework.richclient.application.splash.MonitoringSplashScreen;
import org.springframework.richclient.application.splash.ProgressSplashScreen;
import org.springframework.richclient.application.splash.SplashScreen;
import org.springframework.richclient.progress.ProgressMonitor;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.beans.init.MergedContext;
import de.invesdwin.context.beans.init.PreMergedContext;
import de.invesdwin.context.client.swing.api.IRichApplication;
import de.invesdwin.context.client.swing.impl.app.DelegateRichApplication;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.context.system.properties.SystemProperties;
import de.invesdwin.util.lang.Reflections;

@NotThreadSafe
public final class ConfiguredSplashScreen implements SplashScreen, FactoryBean<ConfiguredSplashScreen> {

    public static final ConfiguredSplashScreen INSTANCE = new ConfiguredSplashScreen();
    private static final String GTK_LAF = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
    private static final String WIN_LAF = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";

    static {
        if (Reflections.JAVA_VERSION < 13) {
            //gtk3 looks wrong in a lot of places in openjdk-11, fix is supposed to arrive with java 13 
            //https://bugs.openjdk.java.net/browse/JDK-8203627?attachmentOrder=desc
            new SystemProperties().setInteger("jdk.gtk.version", 2);
        }
    }

    @GuardedBy("ConfiguredSplashScreen.class")
    private static ProgressSplashScreen splashScreen;
    @GuardedBy("ConfiguredSplashScreen.class")
    private static boolean lookAndFeelConfigured = false;
    protected boolean showing;

    private ConfiguredSplashScreen() {}

    public void splash(final boolean force) {
        synchronized (ConfiguredSplashScreen.class) {
            if (splashScreen == null || force) {
                dispose();

                final IRichApplication richApplication = PreMergedContext.getInstance().getBean(IRichApplication.class);

                configureLookAndFeel(richApplication);
                configureLocale(richApplication);
                final ApplicationMessageSource messageSource = new ApplicationMessageSource();

                splashScreen = new ProgressSplashScreen();
                splashScreen.setMessageSource(messageSource);

                splashScreen.setImageResourcePath(new ClassPathResource(splashScreen.getMessageSource()
                        .getMessage(DelegateRichApplication.KEY_APPLICATION_SPLASH, null, null)));
                splashScreen.setIconResourcePath(
                        splashScreen.getMessageSource().getMessage(Application.KEY_APPLICATION_ICON, null, null));

                if (!richApplication.isHideSplashOnStartup()) {
                    final ProgressMonitor tracker = ((MonitoringSplashScreen) splashScreen).getProgressMonitor();
                    MergedContext.autowire(new ProgressMonitoringBeanFactoryPostProcessor(tracker, messageSource));
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
                }
            }
        }
    }

    private void configureLocale(final IRichApplication richApplication) {
        final Locale localeOverride = richApplication.getLocaleOverride();
        if (localeOverride != null) {
            Locale.setDefault(localeOverride);
            LocaleContextHolder.setDefaultLocale(localeOverride);
        }
    }

    private void configureLookAndFeel(final IRichApplication richApplication) {
        if (lookAndFeelConfigured) {
            return;
        }
        String lookAndFeel = richApplication.getLookAndFeelOverride();
        if (lookAndFeel == null) {
            lookAndFeel = UIManager.getSystemLookAndFeelClassName();
        }
        if (lookAndFeel.equals(UIManager.getCrossPlatformLookAndFeelClassName()) && Reflections.classExists(GTK_LAF)) {
            //use GTK in XFCE
            lookAndFeel = GTK_LAF;
        }
        if (lookAndFeel.equals(WIN_LAF)) {
            //use a better windows L&F
            lookAndFeel = com.jgoodies.looks.windows.WindowsLookAndFeel.class.getCanonicalName();
        }
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            Err.process(e);
        }
        lookAndFeelConfigured = true;
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
