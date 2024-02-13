package de.invesdwin.context.client.swing.api.view;

import java.awt.GridLayout;

import javax.annotation.concurrent.ThreadSafe;
import javax.swing.JPanel;
import javax.swing.JTextArea;

@ThreadSafe
public class PlaceholderView extends AView<PlaceholderView, JPanel> {

    private final String placeholderId;

    public PlaceholderView(final String placeholderId) {
        this.placeholderId = placeholderId;
    }

    @Override
    public String getId() {
        return placeholderId;
    }

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    protected JPanel initComponent() {
        final JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 0, 0, 0));

        final JTextArea textArea = new JTextArea();
        panel.add(textArea);
        textArea.setText("Placeholder: " + this.placeholderId);

        return panel;
    }

    @Override
    public String getTitle() {
        return this.placeholderId;
    }

    @Override
    public String getDockableUniqueId() {
        return this.placeholderId;
    }
}
