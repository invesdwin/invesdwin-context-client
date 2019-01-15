package de.invesdwin.context.client.swing.internal.status;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.plaf.basic.BasicStatusBarUI;

import com.jidesoft.swing.PartialEtchedBorder;

import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.internal.status.message.StatusBarMessageView;
import de.invesdwin.context.client.swing.internal.status.task.StatusBarTaskView;

@SuppressWarnings("serial")
@ThreadSafe
public class StatusBarView extends AView<StatusBarView, JXStatusBar> {

    public static final String DISTANCE_TO_BORDER = " ";

    @Inject
    private StatusBarMessageView messageView;
    @Inject
    private StatusBarTaskView taskView;

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    protected JXStatusBar initComponent() {
        final JXStatusBar component = new JXStatusBar();
        //Separator looks bad on Windows XP LAF
        component.putClientProperty(BasicStatusBarUI.AUTO_ADD_SEPARATOR, false);
        component.setBorder(new PartialEtchedBorder(PartialEtchedBorder.NORTH));
        component.add(messageView.getComponent(),
                new JXStatusBar.Constraint(JXStatusBar.Constraint.ResizeBehavior.FILL));
        component.add(taskView.getComponent());
        return component;
    }

}
