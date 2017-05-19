package de.invesdwin.context.client.swing.internal.status.message;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.annotation.concurrent.ThreadSafe;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.internal.status.StatusBarView;
import de.invesdwin.util.lang.Strings;
import de.invesdwin.util.time.duration.Duration;

@SuppressWarnings("serial")
@ThreadSafe
public class StatusBarMessageView extends AView<StatusBarMessageView, JPanel> {

    private JLabel lblMessage;
    private JLabel lblSpace;

    @Override
    protected StatusBarMessageView initModel() {
        return null;
    }

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    protected JPanel initComponent() {
        final JPanel component = new JPanel();
        component.setLayout(new BorderLayout(0, 0));

        lblMessage = new JLabel("");
        lblMessage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(final MouseEvent e) {
                final StatusBarMessageTimeoutThread instance = StatusBarMessageTimeoutThread.getInstance();
                if (instance != null) {
                    instance.setPaused(true);
                }
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                final StatusBarMessageTimeoutThread instance = StatusBarMessageTimeoutThread.getInstance();
                if (instance != null) {
                    instance.setPaused(false);
                }
            }
        });

        lblSpace = new JLabel("");
        component.add(lblSpace, BorderLayout.WEST);
        component.add(lblMessage, BorderLayout.CENTER);
        return component;
    }

    public synchronized void setMessageText(final String text, final Color color, final Duration timeout) {
        if (lblMessage != null) {
            try {
                EventDispatchThreadUtil.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        lblMessage.setForeground(color);
                        if (Strings.isNotBlank(text)) {
                            lblSpace.setText(StatusBarView.DISTANCE_TO_BORDER);
                            lblMessage.setText(text);
                            lblMessage.setToolTipText(text);
                            StatusBarMessageTimeoutThread.startInstance(timeout);
                        } else {
                            lblSpace.setText(null);
                            lblMessage.setText(null);
                            lblMessage.setToolTipText(null);
                            StatusBarMessageTimeoutThread.stopInstance();
                        }
                    }
                });
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
