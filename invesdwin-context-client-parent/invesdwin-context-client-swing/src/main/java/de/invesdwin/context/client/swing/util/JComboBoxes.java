package de.invesdwin.context.client.swing.util;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.annotation.concurrent.Immutable;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import com.jidesoft.swing.DelegateMouseListener;

import de.invesdwin.util.math.Booleans;

@Immutable
public final class JComboBoxes {

    private static final String KEY_PREVENT_SCROLL_WHEEL_FROM_CLOSING_POPUP = JComboBoxes.class.getSimpleName()
            + ".preventScrollWheelFromClosingPopup";

    private static final class PreventScrollWheelFromClosingPopupMouseListener extends DelegateMouseListener {
        private PreventScrollWheelFromClosingPopupMouseListener(final MouseListener listener) {
            super(listener);
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
            //fix scroll wheel closing the popup
            if (e.getButton() > 3) {
                return;
            }
            super.mouseReleased(e);
        }
    }

    private JComboBoxes() {
    }

    public static void enableAutoComplete(final JComboBox<?> combobox) {
        AutoCompleteDecorator.decorate(combobox);
    }

    public static void preventScrollWheelFromClosingPopup(final JComboBox<?> combobox) {
        if (!Booleans.isTrue((Boolean) combobox.getClientProperty(KEY_PREVENT_SCROLL_WHEEL_FROM_CLOSING_POPUP))) {
            combobox.putClientProperty(KEY_PREVENT_SCROLL_WHEEL_FROM_CLOSING_POPUP, true);
            combobox.addPropertyChangeListener("UI", new PropertyChangeListener() {
                @Override
                public void propertyChange(final PropertyChangeEvent evt) {
                    preventScrollWheelFromClosingPopup(combobox);
                }
            });
            final ComboBoxUI ui = combobox.getUI();
            final BasicComboPopup popup = (BasicComboPopup) ui.getAccessibleChild(null, 0);
            final JList<Object> list = popup.getList();
            final MouseListener[] mouseListeners = list.getMouseListeners();
            for (int i = 0; i < mouseListeners.length; i++) {
                final MouseListener listener = mouseListeners[i];
                if (!(listener instanceof PreventScrollWheelFromClosingPopupMouseListener)) {
                    list.removeMouseListener(listener);
                    list.addMouseListener(new PreventScrollWheelFromClosingPopupMouseListener(listener));
                }
            }
        }
    }

}
