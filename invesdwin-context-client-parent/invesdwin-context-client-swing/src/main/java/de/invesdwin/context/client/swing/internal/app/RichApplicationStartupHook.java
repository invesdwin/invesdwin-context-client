package de.invesdwin.context.client.swing.internal.app;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;

import org.jdesktop.application.Application;
import org.jdesktop.application.Application.ExitListener;
import org.jdesktop.application.FrameView;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;
import de.invesdwin.context.beans.hook.IStartupHook;
import de.invesdwin.context.beans.init.MergedContext;
import de.invesdwin.context.client.swing.ContentPane;
import de.invesdwin.context.client.swing.Dialogs;
import de.invesdwin.context.client.swing.SplashScreen;
import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.api.IRichApplication;
import de.invesdwin.context.client.swing.internal.content.ContentPaneView;
import de.invesdwin.context.client.swing.internal.menu.MenuBarView;
import de.invesdwin.context.client.swing.internal.status.StatusBarView;

/**
 * This class handles the initilization and displaying of the MainFrame.
 * 
 * @author subes
 * 
 */
@NotThreadSafe
public class RichApplicationStartupHook implements IStartupHook {

    @Inject
    private IRichApplication delegate;
    @Inject
    private ExitListener[] exitListeners;
    @Inject
    private MenuBarView menuBarView;
    @Inject
    private ContentPaneView contentPaneView;
    @Inject
    private ContentPane contentPane;
    @Inject
    private StatusBarView statusBarView;
    @Inject
    private SplashScreen splashScreen;

    @Override
    @EventDispatchThread(InvocationType.INVOKE_LATER)
    public void startup() throws Exception {
        //Dependency Injection must be invoked manually here
        MergedContext.autowire(this);

        final DelegateRichApplication application = (DelegateRichApplication) Application.getInstance();
        //Set exit listener
        for (final ExitListener exitListener : exitListeners) {
            application.addExitListener(exitListener);
        }

        setupFrame(application);

        EventDispatchThreadUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
                //JForex for instance calls an invokeLater on its own, this causes an Exception if it is not invoked out of the EDT
                new Thread() {
                    @Override
                    public void run() {
                        delegate.startupDone(DelegateRichApplication.getInitializationArgs());
                    }
                }.start();
            }
        });
    }

    public void setupFrame(final DelegateRichApplication application) {
        final FrameView frameView = application.getMainView();
        Dialogs.setRootFrame(frameView.getFrame());
        frameView.setComponent(contentPaneView.getComponent());
        final AView<?, ?> initialView = delegate.getInitialView();
        if (initialView != null) {
            contentPane.addView(initialView);
        }
        frameView.setMenuBar(menuBarView.getComponent());
        frameView.setStatusBar(statusBarView.getComponent());

        frameView.getFrame().pack();
        setInitialFrameSize(frameView);
        if (!delegate.isKeepSplashVisible()) {
            splashScreen.dispose();
        }

        if (!delegate.isHideMainFrameOnStartup()) {
            application.showMainView();
        }
    }

    /**
     * To set the size to the maximum seems to be the only reliable way to maximize a window
     * 
     * http://stackoverflow.com/questions/479523/java-swing-maximize-window
     * 
     * JFrame.MAXIMIZED_BOTH seems not to work properly.
     */
    private void setInitialFrameSize(final FrameView frameView) {
        if (delegate.getInitialFrameSize() == null) {
            final GraphicsConfiguration config = frameView.getFrame().getGraphicsConfiguration();
            final Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(config);
            final int left = screenInsets.left;
            final int right = screenInsets.right;
            final int top = screenInsets.top;
            final int bottom = screenInsets.bottom;

            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            final int width = screenSize.width - left - right;
            final int height = screenSize.height - top - bottom;
            frameView.getFrame().setSize(width, height);
            frameView.getFrame().setLocation(0, 0);
        } else {
            frameView.getFrame().setSize(delegate.getInitialFrameSize());
            frameView.getFrame().setLocationRelativeTo(null);
        }
    }

}
