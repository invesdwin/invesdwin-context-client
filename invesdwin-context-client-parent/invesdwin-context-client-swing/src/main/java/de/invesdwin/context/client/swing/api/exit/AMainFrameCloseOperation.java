package de.invesdwin.context.client.swing.api.exit;

import java.awt.event.WindowEvent;
import java.util.EventObject;

import javax.annotation.concurrent.Immutable;
import javax.swing.JFrame;

import org.jdesktop.application.Application.ExitListener;

import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;
import de.invesdwin.context.client.swing.impl.app.DelegateRichApplication;
import de.invesdwin.context.log.Log;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.util.swing.listener.WindowListenerSupport;

@Immutable
public abstract class AMainFrameCloseOperation {

    public static final AMainFrameCloseOperation NOTHING = new AMainFrameCloseOperation() {
        @Override
        public void end(final EventObject e) {
            //noop
        }
    };
    public static final AMainFrameCloseOperation DISPOSE = new AMainFrameCloseOperation() {

        @Override
        public void end(final EventObject e) {
            DelegateRichApplication.getInstance().getMainFrame().dispose();
        }
    };
    public static final AMainFrameCloseOperation HIDE = new AMainFrameCloseOperation() {

        @Override
        public void end(final EventObject e) {
            DelegateRichApplication.getInstance().getMainFrame().setVisible(false);
        }
    };
    public static final AMainFrameCloseOperation MINIMIZE = new AMainFrameCloseOperation() {

        @Override
        public void end(final EventObject e) {
            DelegateRichApplication.getInstance().getMainFrame().setState(JFrame.ICONIFIED);
        }
    };
    public static final AMainFrameCloseOperation EXIT = new AMainFrameCloseOperation() {
        @Override
        public void end(final EventObject e) {
            DelegateRichApplication.getInstance().end();
        }
    };

    private final Log log = new Log(this);

    public void configureFrame() {
        final JFrame frame = DelegateRichApplication.getInstance().getMainFrame();
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowListenerSupport() {
            @Override
            public void windowClosing(final WindowEvent e) {
                end(e);
            }
        });
    }

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public final void close(final EventObject event) {
        final DelegateRichApplication application = DelegateRichApplication.getInstance();
        for (final ExitListener listener : application.getExitListeners()) {
            if (!listener.canExit(event)) {
                return;
            }
        }
        try {
            for (final ExitListener listener : application.getExitListeners()) {
                try {
                    listener.willExit(event);
                } catch (final Throwable e) {
                    Err.process(e);
                }
            }
            shutdown();
        } catch (final Throwable e) {
            Err.process(e);
        } finally {
            end(event);
        }
    }

    public void shutdown() {
        DelegateRichApplication.getInstance().shutdown();
    }

    public abstract void end(EventObject event);

}
