package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.dialog;

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
import de.invesdwin.context.client.swing.jfreechart.panel.helper.legend.HighlightedLegendInfo;
import de.invesdwin.util.swing.Dialogs;
import de.invesdwin.util.swing.HiDPI;
import de.invesdwin.util.swing.listener.WindowListenerSupport;

@NotThreadSafe
public class SettingsDialog extends JDialog {

    public static final int MAX_WIDTH = HiDPI.scale(800);
    private final SettingsPanel panel;

    public SettingsDialog(final PlotConfigurationHelper plotConfigurationHelper,
            final HighlightedLegendInfo highlighted) {
        super(Dialogs.getFrameForComponent(plotConfigurationHelper.getChartPanel()), true);
        final Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        panel = new SettingsPanel(plotConfigurationHelper, highlighted, this);
        contentPane.add(panel);
        setTitle(highlighted.getSeriesTitle() + " - Series Settings");

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

        setResizable(true);
        setMinimumSize(HiDPI.scale(new Dimension(400, 300)));
        pack();
        if (getWidth() > MAX_WIDTH) {
            setSize(MAX_WIDTH, getHeight());
        }
        setLocationRelativeTo(plotConfigurationHelper.getChartPanel());
    }

    @Override
    public void pack() {
        //don't automatically change width
        if (isShowing()) {
            final int prevWidth = getWidth();
            final Dimension prevPreferredSize = getPreferredSize();
            setPreferredSize(new Dimension(prevWidth, 0));
            super.pack();
            setPreferredSize(prevPreferredSize);
        } else {
            super.pack();
        }
    }

    protected void close() {
        //tradingview also regards this as ok()
        panel.ok();
        dispose();
    }
}
