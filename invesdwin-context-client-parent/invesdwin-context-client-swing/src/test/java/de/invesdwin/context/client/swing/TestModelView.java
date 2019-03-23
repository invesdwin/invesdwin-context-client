package de.invesdwin.context.client.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import de.invesdwin.context.client.swing.api.AView;

@NotThreadSafe
public class TestModelView extends AView<TestModel, JPanel> {

    private JTextArea descriptionJTextArea;
    private JTextField nameJTextField;
    private JButton nextJButton;
    private JScrollPane scrollPane;
    private JButton btnDoNothing;
    private JLabel lblNotice;
    private JList list;
    private JLabel lblBeanlist;
    private JTable table;
    private JLabel lblBeantable;
    private JScrollPane scrollPane_1;
    private JScrollPane scrollPane_2;
    private JCheckBox chckbxCheckboxTest;

    public TestModelView(final TestModel model) {
        super(model);
    }

    /**
     * @wbp.parser.entryPoint
     */
    //CHECKSTYLE:OFF
    @Override
    protected JPanel initComponent() {
        //CHECKSTYLE:ON
        final JPanel component = new JPanel();

        final GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0 };
        gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
        gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 1.0, 1.0E-4 };
        gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 1.0E-4 };
        component.setLayout(gridBagLayout);

        final JLabel nameLabel = new JLabel("Name:");
        nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
        final GridBagConstraints gbc_nameLabel = new GridBagConstraints();
        gbc_nameLabel.anchor = GridBagConstraints.EAST;
        gbc_nameLabel.insets = new Insets(5, 5, 5, 5);
        gbc_nameLabel.gridx = 0;
        gbc_nameLabel.gridy = 0;
        component.add(nameLabel, gbc_nameLabel);

        nameJTextField = new JTextField();
        nameJTextField.setName("name");
        final GridBagConstraints gbc_nameJTextField = new GridBagConstraints();
        gbc_nameJTextField.gridwidth = 2;
        gbc_nameJTextField.insets = new Insets(5, 0, 5, 0);
        gbc_nameJTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_nameJTextField.gridx = 1;
        gbc_nameJTextField.gridy = 0;
        component.add(nameJTextField, gbc_nameJTextField);

        final JLabel descriptionLabel = new JLabel("Description:");
        final GridBagConstraints gbc_descriptionLabel = new GridBagConstraints();
        gbc_descriptionLabel.anchor = GridBagConstraints.NORTHEAST;
        gbc_descriptionLabel.insets = new Insets(0, 0, 5, 5);
        gbc_descriptionLabel.gridx = 0;
        gbc_descriptionLabel.gridy = 1;
        component.add(descriptionLabel, gbc_descriptionLabel);

        scrollPane = new JScrollPane();
        final GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridwidth = 2;
        gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
        gbc_scrollPane.gridx = 1;
        gbc_scrollPane.gridy = 1;
        component.add(scrollPane, gbc_scrollPane);

        descriptionJTextArea = new JTextArea();
        descriptionJTextArea.setName("description");
        scrollPane.setViewportView(descriptionJTextArea);

        lblBeanlist = new JLabel("BeanList:");
        final GridBagConstraints gbc_lblBeanlist = new GridBagConstraints();
        gbc_lblBeanlist.anchor = GridBagConstraints.NORTH;
        gbc_lblBeanlist.insets = new Insets(0, 0, 5, 5);
        gbc_lblBeanlist.gridx = 0;
        gbc_lblBeanlist.gridy = 2;
        component.add(lblBeanlist, gbc_lblBeanlist);

        scrollPane_2 = new JScrollPane();
        final GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
        gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_2.gridwidth = 2;
        gbc_scrollPane_2.insets = new Insets(0, 0, 5, 0);
        gbc_scrollPane_2.gridx = 1;
        gbc_scrollPane_2.gridy = 2;
        component.add(scrollPane_2, gbc_scrollPane_2);

        list = new JList();
        scrollPane_2.setViewportView(list);
        list.setName("beanList");

        lblBeantable = new JLabel("BeanTable:");
        final GridBagConstraints gbc_lblBeantable = new GridBagConstraints();
        gbc_lblBeantable.anchor = GridBagConstraints.NORTH;
        gbc_lblBeantable.insets = new Insets(0, 0, 5, 5);
        gbc_lblBeantable.gridx = 0;
        gbc_lblBeantable.gridy = 3;
        component.add(lblBeantable, gbc_lblBeantable);

        scrollPane_1 = new JScrollPane();
        final GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
        gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_1.gridwidth = 2;
        gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
        gbc_scrollPane_1.gridx = 1;
        gbc_scrollPane_1.gridy = 3;
        component.add(scrollPane_1, gbc_scrollPane_1);

        table = new JTable();
        scrollPane_1.setViewportView(table);
        table.setModel(new DefaultTableModel(new Object[][] { { null, null }, },
                new String[] { "columnInteger", "columnString", "columnBoolean" }));
        table.setName("beanTable");

        chckbxCheckboxTest = new JCheckBox("Checkbox Test");
        chckbxCheckboxTest.setName("checkboxTest");
        final GridBagConstraints gbc_chckbxCheckboxTest = new GridBagConstraints();
        gbc_chckbxCheckboxTest.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxCheckboxTest.gridx = 1;
        gbc_chckbxCheckboxTest.gridy = 4;
        component.add(chckbxCheckboxTest, gbc_chckbxCheckboxTest);

        lblNotice = new JLabel("empty");
        lblNotice.setName("inner.notice");
        final GridBagConstraints gbc_lblNotice = new GridBagConstraints();
        gbc_lblNotice.insets = new Insets(0, 0, 0, 5);
        gbc_lblNotice.gridx = 0;
        gbc_lblNotice.gridy = 5;
        component.add(lblNotice, gbc_lblNotice);

        btnDoNothing = new JButton("i18nTest");
        btnDoNothing.setName("inner.doNothing");
        final GridBagConstraints gbc_btnDoNothing = new GridBagConstraints();
        gbc_btnDoNothing.insets = new Insets(0, 0, 0, 5);
        gbc_btnDoNothing.gridx = 1;
        gbc_btnDoNothing.gridy = 5;
        component.add(btnDoNothing, gbc_btnDoNothing);

        nextJButton = new JButton("Next");
        nextJButton.setName("next");
        nextJButton.setIcon(new ImageIcon(
                new ImageIcon(TestModelView.class.getResource("/de/invesdwin/context/client/swing/icon.png")).getImage()
                        .getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH)));
        final GridBagConstraints gbc_nextJButton = new GridBagConstraints();
        gbc_nextJButton.anchor = GridBagConstraints.EAST;
        gbc_nextJButton.gridx = 2;
        gbc_nextJButton.gridy = 5;
        component.add(nextJButton, gbc_nextJButton);

        return component;
    }

}
