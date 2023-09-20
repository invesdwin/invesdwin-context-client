package de.invesdwin.context.client.swing.api.view;

import java.awt.Dimension;

import javax.swing.JComponent;

import bibliothek.gui.dock.common.layout.RequestDimension;
import de.invesdwin.util.time.duration.Duration;

public interface IDockable {

    String getTitle();

    void setTitle(String title);

    String getUniqueId();

    JComponent getComponent();

    void setComponent(JComponent component);

    void requestFocus();

    void setView(AView<?, ?> view);

    AView<?, ?> getView();

    void setResizeRequest(RequestDimension dimension, Duration timeout);

    Dimension getSize();

}
