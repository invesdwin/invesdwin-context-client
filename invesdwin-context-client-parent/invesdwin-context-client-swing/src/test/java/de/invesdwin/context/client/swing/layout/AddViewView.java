package de.invesdwin.context.client.swing.layout;

import javax.annotation.concurrent.ThreadSafe;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import de.invesdwin.context.client.swing.api.view.AView;
import net.miginfocom.swing.MigLayout;

@ThreadSafe
public class AddViewView extends AView<AddView, JPanel> {

    public AddViewView(final AddView model) {
        super(model);
    }

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    protected JPanel initComponent() {
        final JPanel panel = new JPanel();
        panel.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), },
                new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.MIN_ROWSPEC, RowSpec.decode("default:grow"),
                        RowSpec.decode("37px"), }));

        final JPanel pnl_top = new JPanel();
        panel.add(pnl_top, "1, 3, fill, fill");
        pnl_top.setLayout(new MigLayout("", "[right][grow]", "[][grow,top]"));

        final JLabel lbl_viewId = new JLabel("<html><b>ViewId:</b>");
        pnl_top.add(lbl_viewId, "cell 0 0,alignx trailing");

        final JTextField tF_viewId = new JTextField();
        tF_viewId.setName("viewId");
        pnl_top.add(tF_viewId, "cell 1 0,growx");
        tF_viewId.setColumns(10);

        final JLabel lbl_location = new JLabel("<html><b>Location:</b>");
        pnl_top.add(lbl_location, "cell 0 1");

        final JComboBox cmb_workingAreaLocation = new JComboBox();
        cmb_workingAreaLocation.setName("workingAreaLocation");
        pnl_top.add(cmb_workingAreaLocation, "cell 1 1,growx");

        final JPanel pnl_buttons = new JPanel();
        panel.add(pnl_buttons, "1, 4, fill, top");
        pnl_buttons.setLayout(new FormLayout(
                new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                        FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
                        ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                        FormSpecs.LABEL_COMPONENT_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                        FormSpecs.RELATED_GAP_COLSPEC, },
                new RowSpec[] { FormSpecs.LINE_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC, }));

        final JButton btn_cancel = new JButton("Cancel");
        btn_cancel.setDefaultCapable(false);
        btn_cancel.setName("cancel");
        pnl_buttons.add(btn_cancel, "8, 2, left, top");

        final JButton btn_ok = new JButton("Ok");
        btn_ok.setDefaultCapable(true);
        btn_ok.setName("ok");
        pnl_buttons.add(btn_ok, "10, 2, left, top");

        return panel;
    }
}
