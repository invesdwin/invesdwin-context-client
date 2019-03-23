package de.invesdwin.context.client.swing.api.guiservice.dialog;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.util.JButtons;
import de.invesdwin.util.swing.Dialogs;

@NotThreadSafe
public class ModalMessageView extends AView<ModalMessage, JPanel> {

    public ModalMessageView(final ModalMessage model) {
        super(model);
    }

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    protected JPanel initComponent() {
        final JPanel panel = new JPanel();

        panel.setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("center:1px:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, },
                new RowSpec[] { FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
                        RowSpec.decode("default:grow"), FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC, }));

        final JEditorPane editorPane = Dialogs.newHtmlMessagePane();
        editorPane.setName("message");
        panel.add(editorPane, "3, 3, fill, fill");

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FormLayout(
                new ColumnSpec[] { ColumnSpec.decode("left:default:grow"), FormSpecs.RELATED_GAP_COLSPEC,
                        FormSpecs.PREF_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.PREF_COLSPEC, },
                new RowSpec[] { FormSpecs.LINE_GAP_ROWSPEC, FormSpecs.PREF_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
                        RowSpec.decode("bottom:default"), }));
        final JButton btnOk = new JButton("ok");
        btnOk.setName("ok");
        JButtons.setDefaultSize(btnOk);
        buttonPanel.add(btnOk, "3, 2");

        panel.add(buttonPanel, "3, 6, fill, top");

        final JButton btnCancel = new JButton("cancel");
        btnCancel.setName("cancel");
        JButtons.setDefaultSize(btnCancel);
        buttonPanel.add(btnCancel, "5, 2");

        return panel;
    }

}
