package de.invesdwin.context.client.swing.util;

import java.awt.Component;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;

import com.jgoodies.common.base.Strings;

import de.invesdwin.util.swing.AComponentVisitor;

/**
 * For instance this sets PopupMenus or changes the look to fulfill standards. This should be invoked on any component,
 * before it is displayed.
 * 
 * @author subes
 * 
 */
@NotThreadSafe
public class ComponentStandardizer extends AComponentVisitor {

    @Override
    protected void visit(final Component component) {
        if (component instanceof JTextComponent) {
            final JTextComponent text = (JTextComponent) component;
            new de.invesdwin.context.client.swing.frame.menu.TextFieldPopupMenuView(text).getComponent();
        }
        if (component instanceof JButton) {
            final JButton button = (JButton) component;
            if (!button.isPreferredSizeSet()
                    && (Strings.isBlank(button.getName()) || NamedModelComponentFinder.INSTANCE.matches(button))) {
                JButtons.setDefaultSize(button);
            }
        }
        if (component instanceof JComboBox) {
            final JComboBox<?> combobox = (JComboBox<?>) component;
            JComboBoxes.enableAutoComplete(combobox);
            JComboBoxes.preventScrollWheelFromClosingPopup(combobox);
        }
    }

}