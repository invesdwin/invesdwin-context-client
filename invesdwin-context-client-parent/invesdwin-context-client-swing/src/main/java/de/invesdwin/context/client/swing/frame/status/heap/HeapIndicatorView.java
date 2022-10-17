package de.invesdwin.context.client.swing.frame.status.heap;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;

import javax.annotation.concurrent.ThreadSafe;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.util.swing.Components;
import de.invesdwin.util.swing.HiDPI;
import de.invesdwin.util.swing.listener.ComponentListenerSupport;

@ThreadSafe
public class HeapIndicatorView extends AView<HeapIndicatorView, JPanel> {

    private JPanel pnlHeapIndicator;
    private HeapIndicator pgbHeapIndicator;

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    protected JPanel initComponent() {
        final JPanel component = new JPanel();
        component.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        component.setMinimumSize(new Dimension(0, 0));
        component.setLayout(new BorderLayout(0, 0));

        pnlHeapIndicator = new JPanel();
        pnlHeapIndicator.setMinimumSize(new Dimension(0, 0));
        pnlHeapIndicator.setBorder(BorderFactory.createEmptyBorder(1, 0, 2, 0));
        component.add(pnlHeapIndicator, BorderLayout.CENTER);
        pnlHeapIndicator.setLayout(new BorderLayout(0, 0));

        pgbHeapIndicator = new HeapIndicator();
        pnlHeapIndicator.add(pgbHeapIndicator, BorderLayout.SOUTH);
        pgbHeapIndicator.setMinimumSize(new Dimension(0, 0));

        calculateProgressbarPreferredSize();

        Components.showTooltipWithoutDelay(pgbHeapIndicator);

        component.addComponentListener(new ComponentListenerSupport() {
            @Override
            public void componentShown(final ComponentEvent e) {
                pnlHeapIndicator.setVisible(component.isVisible());
            }

            @Override
            public void componentHidden(final ComponentEvent e) {
                pnlHeapIndicator.setVisible(component.isVisible());
            }
        });

        return component;
    }

    private void calculateProgressbarPreferredSize() {
        final Dimension pgbPreferredSize = new Dimension();
        pgbPreferredSize.height = HiDPI.scale(HeapIndicator.FONT_SIZE + 2);
        pgbPreferredSize.width = pgbPreferredSize.height * 7;
        pgbHeapIndicator.setPreferredSize(pgbPreferredSize);
    }

}
