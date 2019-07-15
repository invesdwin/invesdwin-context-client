package de.invesdwin.context.client.swing.api;

import javax.swing.JComponent;

public interface IDockable {

    String getUniqueId();

    JComponent getComponent();

    void setComponent(JComponent component);

    void requestFocus();

    void setView(AView<?, ?> view);

    AView<?, ?> getView();

}
