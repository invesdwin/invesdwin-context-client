package de.invesdwin.context.client.swing.api.view;

import javax.swing.JComponent;

public interface IDockable {

    String getTitle();

    void setTitle(String title);

    String getUniqueId();

    JComponent getComponent();

    void setComponent(JComponent component);

    void requestFocus();

    void setView(AView<?, ?> view);

    AView<?, ?> getView();

}
