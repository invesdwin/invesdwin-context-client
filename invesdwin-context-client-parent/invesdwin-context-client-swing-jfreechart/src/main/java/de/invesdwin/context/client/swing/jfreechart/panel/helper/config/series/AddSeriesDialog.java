package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.PlotConfigurationHelper;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.dialog.SettingsDialog;
import de.invesdwin.util.swing.Dialogs;
import de.invesdwin.util.swing.listener.WindowListenerSupport;

@NotThreadSafe
public class AddSeriesDialog extends JDialog {

    public static final int MAX_WIDTH = SettingsDialog.MAX_WIDTH;
    private final AddSeriesPanel panel;

    public AddSeriesDialog(final PlotConfigurationHelper plotConfigurationHelper) {
        super(Dialogs.getFrameForComponent(plotConfigurationHelper.getChartPanel()), true);
        final Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        panel = new AddSeriesPanel(plotConfigurationHelper, this);
        contentPane.add(panel);
        setTitle("Add Series");

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowListenerSupport() {
            @Override
            public void windowClosing(final WindowEvent e) {
                close();
            }
        });
        Dialogs.installEscapeCloseOperation(this);

        getRootPane().registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                close();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        setMinimumSize(new Dimension(400, 300));
        setResizable(true);
        pack();
        if (getWidth() > MAX_WIDTH) {
            setSize(MAX_WIDTH, getHeight());
        }
        setLocationRelativeTo(plotConfigurationHelper.getChartPanel());
    }

    protected void close() {
        dispose();
    }
}
