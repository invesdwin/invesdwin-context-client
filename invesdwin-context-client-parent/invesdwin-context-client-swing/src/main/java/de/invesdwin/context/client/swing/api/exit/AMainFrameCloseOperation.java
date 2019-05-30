package de.invesdwin.context.client.swing.api.exit;

import java.awt.event.WindowEvent;
import java.util.EventObject;

import javax.annotation.concurrent.Immutable;
import javax.swing.JFrame;

import org.jdesktop.application.Application.ExitListener;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.client.swing.impl.app.DelegateRichApplication;
import de.invesdwin.context.log.Log;
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
            frame.dispose();
        }
    };
    public static final AMainFrameCloseOperation HIDE = new AMainFrameCloseOperation() {

        @Override
        public void end(final EventObject e) {
            frame.setVisible(false);
        }
    };
    public static final AMainFrameCloseOperation MINIMIZE = new AMainFrameCloseOperation() {

        @Override
        public void end(final EventObject e) {
            frame.setState(JFrame.ICONIFIED);
        }
    };
    public static final AMainFrameCloseOperation EXIT = new AMainFrameCloseOperation() {
        @Override
        public void end(final EventObject e) {
            application.end();
        }
    };
    protected DelegateRichApplication application;
    protected JFrame frame;

    private final Log log = new Log(this);

    public void configureFrame(final DelegateRichApplication application, final JFrame frame) {
        this.application = application;
        this.frame = frame;
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowListenerSupport() {
            @Override
            public void windowClosing(final WindowEvent e) {
                end(e);
            }
        });
    }

    public final void close(final EventObject event) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (final ExitListener listener : application.getExitListeners()) {
                    if (!listener.canExit(event)) {
                        return;
                    }
                }
                try {
                    for (final ExitListener listener : application.getExitListeners()) {
                        try {
                            listener.willExit(event);
                        } catch (final Exception e) {
                            log.warn("ExitListener.willExit() failed", e);
                        }
                    }
                    shutdown();
                } catch (final Exception e) {
                    log.warn("unexpected error in Application.shutdown()", e);
                } finally {
                    end(event);
                }
            }

        };

        try {
            EventDispatchThreadUtil.invokeAndWait(runnable);
        } catch (final InterruptedException e) {
            //ignore
        }
    }

    public void shutdown() {
        application.shutdown();
    }

    public abstract void end(EventObject event);

}
