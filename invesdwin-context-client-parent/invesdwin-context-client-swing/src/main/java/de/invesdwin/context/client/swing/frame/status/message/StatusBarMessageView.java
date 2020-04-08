package de.invesdwin.context.client.swing.frame.status.message;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.annotation.concurrent.ThreadSafe;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.util.lang.Strings;
import de.invesdwin.util.swing.Components;
import de.invesdwin.util.swing.MouseEnteredListener;
import de.invesdwin.util.time.duration.Duration;

@SuppressWarnings("serial")
@ThreadSafe
public class StatusBarMessageView extends AView<StatusBarMessageView, JPanel> {

    private JLabel lblMessage;
    private MouseEnteredListener lblMessage_mouseEnteredListener;

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

        lblMessage_mouseEnteredListener = MouseEnteredListener.get(lblMessage);

        component.add(lblMessage, BorderLayout.CENTER);
        component.setVisible(false);
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
                            final String adjText = Strings.putPrefixIgnoreCase("<html>", text.replace("\n", "<br>"));
                            lblMessage.setText(adjText);
                            Components.setToolTipText(lblMessage, adjText,
                                    lblMessage_mouseEnteredListener.isMouseEntered());
                            StatusBarMessageTimeoutThread.startInstance(timeout);
                            getComponent().setVisible(true);
                        } else {
                            lblMessage.setText(null);
                            Components.setToolTipText(lblMessage, null,
                                    lblMessage_mouseEnteredListener.isMouseEntered());
                            StatusBarMessageTimeoutThread.stopInstance();
                            getComponent().setVisible(false);
                        }
                    }
                });
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
