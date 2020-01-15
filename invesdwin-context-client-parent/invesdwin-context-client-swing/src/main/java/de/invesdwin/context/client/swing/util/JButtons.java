package de.invesdwin.context.client.swing.util;

import java.awt.Dimension;

import javax.annotation.concurrent.Immutable;
import javax.swing.JButton;
import javax.swing.JComponent;

import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.util.LayoutStyle;

import de.invesdwin.util.math.Integers;

@Immutable
public final class JButtons {

    private static final ConstantSize DEFAULT_BUTTON_WIDTH = (ConstantSize) LayoutStyle.getCurrent()
            .getDefaultButtonWidth();
    private static final ConstantSize DEFAULT_BUTTON_HEIGHT = (ConstantSize) LayoutStyle.getCurrent()
            .getDefaultButtonHeight();

    private JButtons() {}

    public static Dimension getDefaultSize(final JComponent button) {
        //+4 is required for GTK3 and does not hurt on other look and feels
        final int width = Integers.max(DEFAULT_BUTTON_WIDTH.getPixelSize(button) + 4, button.getPreferredSize().width);
        final int height = Integers.max(DEFAULT_BUTTON_HEIGHT.getPixelSize(button), button.getPreferredSize().height);
        return new Dimension(width, height);
    }

    public static void setDefaultSize(final JButton button) {
        final Dimension defaultSize = getDefaultSize(button);
        button.setPreferredSize(defaultSize);
        button.setMinimumSize(defaultSize);
    }
}
