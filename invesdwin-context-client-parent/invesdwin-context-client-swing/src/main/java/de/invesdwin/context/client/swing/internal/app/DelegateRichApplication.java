package de.invesdwin.context.client.swing.internal.app;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JFrame;

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
import de.invesdwin.context.client.swing.GuiExceptionHandler;
import de.invesdwin.context.client.swing.api.IRichApplication;
import de.invesdwin.context.client.swing.api.MainFrameCloseOperation;
import de.invesdwin.context.client.swing.internal.splash.ConfiguredSplashScreen;
import de.invesdwin.context.client.swing.listener.WindowListenerSupport;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.lang.Reflections;

/**
 * This class initialized only rudimentary things, so that the EDT is not blocked for too long.
 * 
 * @author subes
 * 
 */
@NotThreadSafe
@ProxyActions({ "select-all", "undo", "redo" })
public class DelegateRichApplication extends SingleFrameApplication {

    public static final String KEY_APPLICATION_SPLASH = "Application.splash";

    private static volatile String[] initializationArgs;

    public DelegateRichApplication() {
        super();
        Assertions.assertThat(GuiExceptionHandler.INSTANCE).isNotNull();
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
                //Bootstrap must run in another Thread, so that splash progress does not halt because the EDT is blocked
                MergedContext.autowire(this);
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
    public void showMainView() {
        final FrameView frameView = getMainView();
        final JFrame frame = frameView.getFrame();
        frame.setVisible(true);
        frame.repaint(); //to be safe we call a repaint so that the temporary grey area on the top is less likely to occur
        show(frameView);
        final IRichApplication application = MergedContext.getInstance().getBean(IRichApplication.class);
        final MainFrameCloseOperation closeOperation = application.getMainFrameCloseOperation();
        switch (closeOperation) {
        case HideFrame:
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            break;
        case MinimizeFrame:
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowListenerSupport() {
                @Override
                public void windowClosing(final WindowEvent e) {
                    frame.setState(JFrame.ICONIFIED);
                }
            });
            break;
        case Nothing:
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            break;
        case SystemExit:
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            break;
        default:
            throw UnknownArgumentException.newInstance(MainFrameCloseOperation.class, closeOperation);
        }
        final WindowListener[] listeners = frame.getWindowListeners();
        for (final WindowListener l : listeners) {
            final String name = l.getClass().getName();
            if ("org.jdesktop.application.SingleFrameApplication$MainFrameListener".equals(name)) {
                frame.removeWindowListener(l);
                break;
            }
        }
    }

}
