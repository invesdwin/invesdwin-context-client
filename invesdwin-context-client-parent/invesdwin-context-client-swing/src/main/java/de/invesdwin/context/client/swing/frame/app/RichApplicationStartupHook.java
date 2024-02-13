package de.invesdwin.context.client.swing.frame.app;

import java.io.File;

import javax.annotation.concurrent.NotThreadSafe;

import org.jdesktop.application.Application.ExitListener;
import org.jdesktop.application.FrameView;

import de.invesdwin.context.beans.hook.IStartupHook;
import de.invesdwin.context.beans.init.MergedContext;
import de.invesdwin.context.client.swing.api.IRichApplication;
import de.invesdwin.context.client.swing.api.guiservice.ContentPane;
import de.invesdwin.context.client.swing.api.guiservice.PersistentLayoutManager;
import de.invesdwin.context.client.swing.api.hook.RichApplicationHookManager;
import de.invesdwin.context.client.swing.api.hook.RichApplicationHookSupport;
import de.invesdwin.context.client.swing.frame.RichApplicationProperties;
import de.invesdwin.context.client.swing.frame.content.ContentPaneView;
import de.invesdwin.context.client.swing.frame.menu.MenuBarView;
import de.invesdwin.context.client.swing.frame.splash.ConfiguredSplashScreen;
import de.invesdwin.context.client.swing.frame.status.StatusBarView;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.swing.Dialogs;
import de.invesdwin.util.swing.EventDispatchThreadUtil;
import de.invesdwin.util.swing.Frames;
import jakarta.inject.Inject;

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
    private PersistentLayoutManager persistentLayoutManager;
    @Inject
    private StatusBarView statusBarView;

    @Override
    public void startup() throws Exception {
        //Dependency Injection must be invoked manually here
        MergedContext.autowire(this);

        final DelegateRichApplication application = DelegateRichApplication.getInstance();
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
                if (delegate.isSaveRestorePersistentLayout()) {
                    configurePersistentLayout(application);
                }

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
                            RichApplicationHookManager.INSTANCE.triggerStartupDone();
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

    private void configurePersistentLayout(final DelegateRichApplication application) {
        final File layoutFile = new File(RichApplicationProperties.getStorageDirectory(),
                PersistentLayoutManager.LAYOUT_FILE_NAME);
        if (!layoutFile.exists()) {
            return;
        }
        persistentLayoutManager.restoreLayout(layoutFile);
        RichApplicationHookManager.register(new RichApplicationHookSupport() {
            @Override
            public void hideMainFrameDone() {
                //save layout on close
                persistentLayoutManager.saveLayout(layoutFile);
            }
        });
    }

    private void setupFrame(final DelegateRichApplication application) {
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
