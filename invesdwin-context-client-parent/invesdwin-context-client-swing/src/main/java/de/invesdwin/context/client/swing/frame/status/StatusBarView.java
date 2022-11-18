package de.invesdwin.context.client.swing.frame.status;

import java.awt.Component;
import java.awt.event.ComponentEvent;

import javax.annotation.concurrent.ThreadSafe;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.plaf.basic.BasicStatusBarUI;

import com.jidesoft.swing.PartialEtchedBorder;

import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.context.client.swing.frame.status.heap.HeapIndicatorView;
import de.invesdwin.context.client.swing.frame.status.message.StatusBarMessageView;
import de.invesdwin.context.client.swing.frame.status.task.StatusBarTaskView;
import de.invesdwin.util.swing.listener.ComponentListenerSupport;
import jakarta.inject.Inject;

@SuppressWarnings("serial")
@ThreadSafe
public class StatusBarView extends AView<StatusBarView, JXStatusBar> {

    private static final Border VISIBLE_BORDER = BorderFactory.createCompoundBorder(
            new PartialEtchedBorder(PartialEtchedBorder.NORTH), BorderFactory.createEmptyBorder(0, 5, 0, 5));
    private static final Border INVISIBLE_BORDER = BorderFactory.createEmptyBorder(0, 0, 0, 0);
    @Inject
    private StatusBarMessageView messageView;
    @Inject
    private StatusBarTaskView taskView;
    @Inject
    private HeapIndicatorView heapIndicatorView;

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    protected JXStatusBar initComponent() {
        final JXStatusBar component = new JXStatusBar() {
            @Override
            protected void addImpl(final Component comp, final Object constraints, final int index) {
                comp.addComponentListener(new ComponentListenerSupport() {
                    @Override
                    public void componentShown(final ComponentEvent e) {
                        updateBorder(StatusBarView.this.getComponent());
                    }

                    @Override
                    public void componentHidden(final ComponentEvent e) {
                        updateBorder(StatusBarView.this.getComponent());
                    }
                });
                super.addImpl(comp, constraints, index);
            }
        };
        //Separator looks bad on Windows XP LAF
        component.putClientProperty(BasicStatusBarUI.AUTO_ADD_SEPARATOR, false);
        component.add(messageView.getComponent(),
                new JXStatusBar.Constraint(JXStatusBar.Constraint.ResizeBehavior.FILL));
        component.add(taskView.getComponent());
        component.add(heapIndicatorView.getComponent());

        updateBorder(component);
        return component;
    }

    private void updateBorder(final JXStatusBar component) {
        boolean visible = false;
        for (int i = 0; i < component.getComponentCount(); i++) {
            final Component c = component.getComponent(i);
            if (c.isVisible()) {
                visible = true;
                break;
            }
        }
        if (visible) {
            component.setBorder(VISIBLE_BORDER);
        } else {
            component.setBorder(INVISIBLE_BORDER);
        }
    }

}
