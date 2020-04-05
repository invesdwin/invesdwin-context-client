package de.invesdwin.context.client.swing.frame.app;

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
import de.invesdwin.context.client.swing.frame.content.ContentPaneView;
import de.invesdwin.context.client.swing.frame.menu.MenuBarView;
import de.invesdwin.context.client.swing.frame.splash.ConfiguredSplashScreen;
import de.invesdwin.context.client.swing.frame.status.StatusBarView;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.swing.Dialogs;
import de.invesdwin.util.swing.Frames;

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
        delegate.showInitialViews(contentPane);
        frameView.setMenuBar(menuBarView.getComponent());
        frameView.setStatusBar(statusBarView.getComponent());

        frameView.getFrame().pack();
        Frames.setInitialFrameSize(frameView.getFrame(), delegate.getInitialFrameSize());
    }

}
