package de.invesdwin.context.client.swing.frame.app;

import java.awt.Dimension;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Locale;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JFrame;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.LocalStorage;
import org.jdesktop.application.ProxyActions;
import org.jdesktop.application.SingleFrameApplication;
import org.springframework.context.i18n.LocaleContextHolder;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;
import de.invesdwin.context.beans.hook.StartupHookManager;
import de.invesdwin.context.beans.init.MergedContext;
import de.invesdwin.context.client.swing.api.IRichApplication;
import de.invesdwin.context.client.swing.api.exit.AMainFrameCloseOperation;
import de.invesdwin.context.client.swing.api.hook.IRichApplicationHook;
import de.invesdwin.context.client.swing.error.GuiExceptionHandler;
import de.invesdwin.context.client.swing.frame.RichApplicationProperties;
import de.invesdwin.context.client.swing.frame.splash.ConfiguredSplashScreen;
import de.invesdwin.context.client.swing.util.ComponentStandardizer;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.Reflections;
import de.invesdwin.util.swing.Dialogs;
import de.invesdwin.util.swing.Frames;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.util.time.fdate.FTimeUnit;

/**
 * This class initialized only rudimentary things, so that the EDT is not blocked for too long.
 * 
 * @author subes
 * 
 */
@NotThreadSafe
@ProxyActions({ "select-all", "undo", "redo" })
public class DelegateRichApplication extends SingleFrameApplication {

    public static final String KEY_APPLICATION_NAME = "Application.name";
    public static final String KEY_APPLICATION_SPLASH = "Application.splash";
    public static final boolean INITIALIZED;

    private static final String GTK_LAF = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
    private static final String WIN_LAF = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
    @GuardedBy("ConfiguredSplashScreen.class")
    private static boolean lookAndFeelConfigured = false;

    static {
        Assertions.checkNull(Dialogs.getDialogVisitor());
        Dialogs.setDialogVisitor(new ComponentStandardizer());
        ConfiguredSplashScreen.INSTANCE.splash();
        INITIALIZED = true;
    }

    /**
     * Use getInstance() instead!
     */
    @Deprecated
    public DelegateRichApplication() {
        super();
        Assertions.assertThat(GuiExceptionHandler.INSTANCE).isNotNull();
        ToolTipManager.sharedInstance()
                .setDismissDelay(new Duration(10, FTimeUnit.MINUTES).intValue(FTimeUnit.MILLISECONDS));
    }

    /**
     * This method does not get propagated in the hooks, because the arguments should rather be handled by the Main
     * class in the front. This ensures that the help parameter displays the correct result and not the empty result
     * from the RichApplication Main.
     */
    @Override
    protected void initialize(final String[] args) {
        super.initialize(args);
        RichApplicationProperties.initApplicatonBundleNames(this, true);
        RichApplicationProperties.setInitializationArgs(args);

        //Do Lazy-Init earlier so that UndoRedoActions work correctly
        final ApplicationContext ctx = getContext();
        Assertions.assertThat(ctx.getActionMap()).isNotNull();

        //Replace default TaskService our own
        ctx.removeTaskService(ctx.getTaskService());
        MergedContext.autowire(this); //make sure mergedContext is initialized
        ctx.addTaskService(MergedContext.getInstance().getBean(DefaultTaskService.class));
        Assertions.assertThat(ctx.getTaskService()).isInstanceOf(DefaultTaskService.class);
        initLocalStorageDirectory(ctx);

        final IRichApplication delegate = RichApplicationProperties.getDelegate();
        configureLookAndFeel(delegate);
        configureLocale(delegate);

        delegate.initializeDone();
        final Map<String, IRichApplicationHook> hooks = MergedContext.getInstance()
                .getBeansOfType(IRichApplicationHook.class);
        for (final IRichApplicationHook hook : hooks.values()) {
            hook.initializeDone();
        }
    }

    private void initLocalStorageDirectory(final ApplicationContext ctx) {
        final File storageDir = new File(RichApplicationProperties.getStorageDirectory(),
                LocalStorage.class.getSimpleName());
        ctx.getLocalStorage().setDirectory(storageDir);
    }

    @Override
    protected void startup() {
        final RichApplicationStartupHook startupHook = new RichApplicationStartupHook();
        StartupHookManager.registerOrCall(startupHook);
        //Thread must be started after the splash is shown, or else a deadlock might occur, thus invokeLater
        EventDispatchThreadUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Thread() {
                    @Override
                    public void run() {
                        //Bootstrap must run in another Thread, so that splash progress does not halt because the EDT is blocked
                        try {
                            MergedContext.awaitBootstrapFinishedIfRunning();
                        } catch (final InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        MergedContext.autowire(this);
                    };
                }.start();
            }
        });
    }

    @EventDispatchThread(InvocationType.INVOKE_LATER_IF_NOT_IN_EDT)
    public void showMainFrame() {
        final FrameView frameView = getMainView();
        final JFrame frame = frameView.getFrame();
        frame.setMinimumSize(new Dimension(100, 100));
        frame.setVisible(true);
        frame.repaint(); //to be safe we call a repaint so that the temporary grey area on the top is less likely to occur
        show(frameView);
        final IRichApplication application = MergedContext.getInstance()
                .getBean(RichApplicationProperties.getDelegateClass());
        final AMainFrameCloseOperation closeOperation = application.getMainFrameCloseOperation();
        closeOperation.configureFrame();
        final WindowListener[] listeners = frame.getWindowListeners();
        for (final WindowListener l : listeners) {
            final String name = l.getClass().getName();
            if ("org.jdesktop.application.SingleFrameApplication$MainFrameListener".equals(name)) {
                frame.removeWindowListener(l);
                break;
            }
        }
        EventDispatchThreadUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
                //repeat later (might be needed on windows)
                Frames.setInitialFrameSize(frameView.getFrame(), application.getInitialFrameSize());
            }
        });
    }

    @Override
    public void shutdown() {
        super.shutdown();
        final IRichApplication delegate = RichApplicationProperties.getDelegate();
        delegate.startupDone();
        final Map<String, IRichApplicationHook> hooks = MergedContext.getInstance()
                .getBeansOfType(IRichApplicationHook.class);
        for (final IRichApplicationHook hook : hooks.values()) {
            hook.shutdownDone();
        }
    }

    @Override
    public void end() {
        super.end();
    }

    public static DelegateRichApplication getInstance() {
        return SingleFrameApplication.getInstance(DelegateRichApplication.class);
    }

    private void configureLocale(final IRichApplication richApplication) {
        final Locale localeOverride = richApplication.getLocaleOverride();
        if (localeOverride != null) {
            Locale.setDefault(localeOverride);
            Dialogs.setDefaultLocale(localeOverride);
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
            if (lookAndFeel.equals(UIManager.getCrossPlatformLookAndFeelClassName())
                    && Reflections.classExists(GTK_LAF)) {
                //use GTK in XFCE
                lookAndFeel = GTK_LAF;
            }
            if (lookAndFeel.equals(WIN_LAF)) {
                //use a better windows L&F
                lookAndFeel = com.jgoodies.looks.windows.WindowsLookAndFeel.class.getCanonicalName();
            }
        }
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            Err.process(e);
        }

        lookAndFeelConfigured = true;
    }

    public static synchronized void launch() {
        launchInternal(new String[0]);
    }

    public static synchronized void launch(final String[] args) {
        launchInternal(args);
    }

    @Deprecated
    public static synchronized <T extends Application> void launch(final Class<T> applicationClass,
            final String[] args) {
        launchInternal(args);
    }

    public static synchronized void launchInternal(final String[] args) {
        try {
            final DelegateRichApplication application = (DelegateRichApplication) Reflections.method("create")
                    .withReturnType(Application.class)
                    .withParameterTypes(Class.class)
                    .in(Application.class)
                    .invoke(DelegateRichApplication.class);
            Reflections.field("application").ofType(Application.class).in(Application.class).set(application);
            application.initialize(args);
            application.startup();
            EventDispatchThreadUtil.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    Reflections.method("waitForReady").in(application).invoke();
                }
            });
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}
