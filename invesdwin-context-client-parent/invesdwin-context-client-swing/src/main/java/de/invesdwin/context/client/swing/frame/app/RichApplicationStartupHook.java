package de.invesdwin.context.client.swing.frame.app;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.Map;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;

import org.jdesktop.application.Application;
import org.jdesktop.application.Application.ExitListener;
import org.jdesktop.application.FrameView;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.beans.hook.IStartupHook;
import de.invesdwin.context.beans.init.MergedContext;
import de.invesdwin.context.client.swing.api.IRichApplication;
import de.invesdwin.context.client.swing.api.guiservice.ContentPane;
import de.invesdwin.context.client.swing.api.hook.IRichApplicationHook;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.context.client.swing.frame.content.ContentPaneView;
import de.invesdwin.context.client.swing.frame.menu.MenuBarView;
import de.invesdwin.context.client.swing.frame.splash.ConfiguredSplashScreen;
import de.invesdwin.context.client.swing.frame.status.StatusBarView;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.swing.Dialogs;

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

    @Override
    public void startup() throws Exception {
        //Dependency Injection must be invoked manually here
        MergedContext.autowire(this);

        final DelegateRichApplication application = (DelegateRichApplication) Application.getInstance();
        //Set exit listener
        for (final ExitListener exitListener : exitListeners) {
            application.addExitListener(exitListener);
        }

        EventDispatchThreadUtil.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Assertions.checkNotNull(application.getMainFrame());
            }
        });
        Assertions.checkNotNull(contentPaneView.getComponent()); // eager init
        EventDispatchThreadUtil.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                setupFrame(application);
            }
        });

        EventDispatchThreadUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
                //JForex for instance calls an invokeLater on its own, this causes an Exception if it is not invoked out of the EDT
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            delegate.startupDone();
                            final Map<String, IRichApplicationHook> hooks = MergedContext.getInstance()
                                    .getBeansOfType(IRichApplicationHook.class);
                            for (final IRichApplicationHook hook : hooks.values()) {
                                hook.startupDone();
                            }
                        } finally {
                            if (!delegate.isKeepSplashVisible()) {
                                ConfiguredSplashScreen.INSTANCE.dispose();
                            }
                            if (!delegate.isHideMainFrameOnStartup()) {
                                application.showMainFrame();
                            }
                        }
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
            contentPane.showView(initialView);
        }
        frameView.setMenuBar(menuBarView.getComponent());
        frameView.setStatusBar(statusBarView.getComponent());

        frameView.getFrame().pack();
        setInitialFrameSize(frameView);
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
