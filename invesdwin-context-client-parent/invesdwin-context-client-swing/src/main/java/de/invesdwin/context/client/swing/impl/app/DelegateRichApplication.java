package de.invesdwin.context.client.swing.impl.app;

import java.awt.Dimension;
import java.awt.event.WindowListener;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JFrame;
import javax.swing.ToolTipManager;

import org.jdesktop.application.FrameView;
import org.jdesktop.application.ProxyActions;
import org.jdesktop.application.SingleFrameApplication;
import org.springframework.beans.factory.config.BeanDefinition;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;
import de.invesdwin.context.beans.hook.StartupHookManager;
import de.invesdwin.context.beans.init.MergedContext;
import de.invesdwin.context.beans.init.PreMergedContext;
import de.invesdwin.context.client.swing.api.IRichApplication;
import de.invesdwin.context.client.swing.api.exit.AMainFrameCloseOperation;
import de.invesdwin.context.client.swing.error.GuiExceptionHandler;
import de.invesdwin.context.client.swing.impl.splash.ConfiguredSplashScreen;
import de.invesdwin.context.client.swing.util.ComponentStandardizer;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.Reflections;
import de.invesdwin.util.swing.Dialogs;
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

    static {
        Assertions.checkNull(Dialogs.getDialogVisitor());
        Dialogs.setDialogVisitor(new ComponentStandardizer());
    }

    public static final String KEY_APPLICATION_SPLASH = "Application.splash";

    private static volatile String[] initializationArgs;

    public DelegateRichApplication() {
        super();
        Assertions.assertThat(GuiExceptionHandler.INSTANCE).isNotNull();
        ToolTipManager.sharedInstance()
                .setDismissDelay(new Duration(10, FTimeUnit.MINUTES).intValue(FTimeUnit.MILLISECONDS));
        DelegateResourceManager.inject(getContext());
    }

    /**
     * This method does not get propagated in the hooks, because the arguments should rather be handled by the Main
     * class in the front. This ensures that the help parameter displays the correct result and not the empty result
     * from the RichApplication Main.
     */
    @Override
    protected void initialize(final String[] args) {
        super.initialize(args);
        setInitializationArgs(args);

        //Show splash in invokeLater so that short gray area is prevented
        EventDispatchThreadUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
                final ConfiguredSplashScreen splashScreen = ConfiguredSplashScreen.INSTANCE;
                splashScreen.splash();
            }
        });

        //Do Lazy-Init earlier so that UndoRedoActions work correctly
        Assertions.assertThat(getContext().getActionMap()).isNotNull();

        //Replace default TaskService our own
        getContext().removeTaskService(getContext().getTaskService());
        getContext().addTaskService(new DefaultTaskService());
        Assertions.assertThat(getContext().getTaskService()).isInstanceOf(DefaultTaskService.class);
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

    public static String[] getInitializationArgs() {
        return initializationArgs.clone();
    }

    private static void setInitializationArgs(final String[] initializationArgs) {
        DelegateRichApplication.initializationArgs = initializationArgs;
    }

    public static Class<?> getDelegateClass() {
        final String[] beanNames = PreMergedContext.getInstance().getBeanNamesForType(IRichApplication.class);
        Assertions.assertThat(beanNames.length)
                .as("Exactly one bean of type [%s] must exist.", IRichApplication.class.getSimpleName())
                .isEqualTo(1);
        final BeanDefinition beanDefinition = PreMergedContext.getInstance().getBeanDefinition(beanNames[0]);
        return Reflections.classForName(beanDefinition.getBeanClassName());
    }

    @EventDispatchThread(InvocationType.INVOKE_LATER_IF_NOT_IN_EDT)
    public void showMainFrame() {
        final FrameView frameView = getMainView();
        final JFrame frame = frameView.getFrame();
        frame.setMinimumSize(new Dimension(100, 100));
        frame.setVisible(true);
        frame.repaint(); //to be safe we call a repaint so that the temporary grey area on the top is less likely to occur
        show(frameView);
        final IRichApplication application = MergedContext.getInstance().getBean(IRichApplication.class);
        final AMainFrameCloseOperation closeOperation = application.getMainFrameCloseOperation();
        closeOperation.configureFrame(this, frame);
        final WindowListener[] listeners = frame.getWindowListeners();
        for (final WindowListener l : listeners) {
            final String name = l.getClass().getName();
            if ("org.jdesktop.application.SingleFrameApplication$MainFrameListener".equals(name)) {
                frame.removeWindowListener(l);
                break;
            }
        }
        addExitListener(application);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    public void end() {
        super.end();
    }

}
