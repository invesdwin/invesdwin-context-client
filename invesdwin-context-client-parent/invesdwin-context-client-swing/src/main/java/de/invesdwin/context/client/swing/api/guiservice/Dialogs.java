package de.invesdwin.context.client.swing.api.guiservice;

import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;

import javax.annotation.concurrent.Immutable;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import de.invesdwin.context.client.swing.util.ComponentStandardizer;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.util.lang.uri.URIs;

@Immutable
public final class Dialogs extends javax.swing.JOptionPane {

    private Dialogs() {}

    public static String showInputDialog(final Object message) {
        return javax.swing.JOptionPane.showInputDialog(standardizeMessage(message));
    }

    public static String showInputDialog(final Object message, final Object initialSelectionValue) {
        return javax.swing.JOptionPane.showInputDialog(standardizeMessage(message), initialSelectionValue);
    }

    public static String showInputDialog(final Component parentComponent, final Object message) {
        return javax.swing.JOptionPane.showInputDialog(parentComponent, standardizeMessage(message));
    }

    public static String showInputDialog(final Component parentComponent, final Object message,
            final Object initialSelectionValue) {
        return javax.swing.JOptionPane.showInputDialog(parentComponent, standardizeMessage(message),
                initialSelectionValue);
    }

    public static String showInputDialog(final Component parentComponent, final Object message, final String title,
            final int messageType) {
        return javax.swing.JOptionPane.showInputDialog(parentComponent, standardizeMessage(message), title,
                messageType);
    }

    public static Object showInputDialog(final Component parentComponent, final Object message, final String title,
            final int messageType, final Icon icon, final Object[] selectionValues,
            final Object initialSelectionValue) {
        return javax.swing.JOptionPane.showInputDialog(parentComponent, standardizeMessage(message), title, messageType,
                icon, selectionValues, initialSelectionValue);
    }

    public static void showMessageDialog(final Component parentComponent, final Object message) {
        javax.swing.JOptionPane.showMessageDialog(parentComponent, standardizeMessage(message));
    }

    public static void showMessageDialog(final Component parentComponent, final Object message, final String title,
            final int messageType) {
        javax.swing.JOptionPane.showMessageDialog(parentComponent, standardizeMessage(message), title, messageType);
    }

    public static void showMessageDialog(final Component parentComponent, final Object message, final String title,
            final int messageType, final Icon icon) {
        javax.swing.JOptionPane.showMessageDialog(parentComponent, standardizeMessage(message), title, messageType,
                icon);
    }

    public static int showConfirmDialog(final Component parentComponent, final Object message) {
        return javax.swing.JOptionPane.showConfirmDialog(parentComponent, standardizeMessage(message));
    }

    public static int showConfirmDialog(final Component parentComponent, final Object message, final String title,
            final int optionType) {
        return javax.swing.JOptionPane.showConfirmDialog(parentComponent, standardizeMessage(message), title,
                optionType);
    }

    public static int showConfirmDialog(final Component parentComponent, final Object message, final String title,
            final int optionType, final int messageType) {
        return javax.swing.JOptionPane.showConfirmDialog(parentComponent, standardizeMessage(message), title,
                optionType, messageType);
    }

    public static int showConfirmDialog(final Component parentComponent, final Object message, final String title,
            final int optionType, final int messageType, final Icon icon) {
        return javax.swing.JOptionPane.showConfirmDialog(parentComponent, standardizeMessage(message), title,
                optionType, messageType, icon);
    }

    //CHECKSTYLE:OFF
    public static int showOptionDialog(final Component parentComponent, final Object message, final String title,
            final int optionType, final int messageType, final Icon icon, final Object[] options,
            final Object initialValue) {
        //CHECKSTYLE:ON
        return javax.swing.JOptionPane.showOptionDialog(parentComponent, standardizeMessage(message), title, optionType,
                messageType, icon, options, initialValue);
    }

    public static void showInternalMessageDialog(final Component parentComponent, final Object message) {
        javax.swing.JOptionPane.showInternalMessageDialog(parentComponent, standardizeMessage(message));
    }

    public static void showInternalMessageDialog(final Component parentComponent, final Object message,
            final String title, final int messageType) {
        javax.swing.JOptionPane.showInternalMessageDialog(parentComponent, standardizeMessage(message), title,
                messageType);
    }

    public static void showInternalMessageDialog(final Component parentComponent, final Object message,
            final String title, final int messageType, final Icon icon) {
        javax.swing.JOptionPane.showInternalMessageDialog(parentComponent, standardizeMessage(message), title,
                messageType, icon);
    }

    public static int showInternalConfirmDialog(final Component parentComponent, final Object message) {
        return javax.swing.JOptionPane.showInternalConfirmDialog(parentComponent, standardizeMessage(message));
    }

    public static int showInternalConfirmDialog(final Component parentComponent, final Object message,
            final String title, final int optionType) {
        return javax.swing.JOptionPane.showInternalConfirmDialog(parentComponent, standardizeMessage(message), title,
                optionType);
    }

    public static int showInternalConfirmDialog(final Component parentComponent, final Object message,
            final String title, final int optionType, final int messageType) {
        return javax.swing.JOptionPane.showInternalConfirmDialog(parentComponent, standardizeMessage(message), title,
                optionType, messageType);
    }

    public static int showInternalConfirmDialog(final Component parentComponent, final Object message,
            final String title, final int optionType, final int messageType, final Icon icon) {
        return javax.swing.JOptionPane.showInternalConfirmDialog(parentComponent, standardizeMessage(message), title,
                optionType, messageType, icon);
    }

    //CHECKSTYLE:OFF
    public static int showInternalOptionDialog(final Component parentComponent, final Object message,
            final String title, final int optionType, final int messageType, final Icon icon, final Object[] options,
            final Object initialValue) {
        //CHECKSTYLE:ON
        return javax.swing.JOptionPane.showInternalOptionDialog(parentComponent, standardizeMessage(message), title,
                optionType, messageType, icon, options, initialValue);
    }

    public static String showInternalInputDialog(final Component parentComponent, final Object message) {
        return javax.swing.JOptionPane.showInternalInputDialog(parentComponent, standardizeMessage(message));
    }

    public static String showInternalInputDialog(final Component parentComponent, final Object message,
            final String title, final int messageType) {
        return javax.swing.JOptionPane.showInternalInputDialog(parentComponent, standardizeMessage(message), title,
                messageType);
    }

    public static Object showInternalInputDialog(final Component parentComponent, final Object message,
            final String title, final int messageType, final Icon icon, final Object[] selectionValues,
            final Object initialSelectionValue) {
        return javax.swing.JOptionPane.showInternalInputDialog(parentComponent, standardizeMessage(message), title,
                messageType, icon, selectionValues, initialSelectionValue);
    }

    /**
     * So that links can be clicked on properly.
     */
    private static Object standardizeMessage(final Object message) {
        Object ret = message;
        if (message instanceof CharSequence) {
            final String messageString = message.toString();
            if (messageString.contains("<html>")) {
                final JEditorPane messagePane = new JEditorPane();
                messagePane.setContentType("text/html");
                messagePane.setText(messageString);
                messagePane.setEditable(false);
                messagePane.setOpaque(false);
                messagePane.addHyperlinkListener(new HyperlinkListener() {
                    @Override
                    public void hyperlinkUpdate(final HyperlinkEvent e) {
                        if (e.getEventType() == EventType.ACTIVATED) {
                            try {
                                Desktop.getDesktop().browse(URIs.asUri(e.getURL()));
                            } catch (final IOException e1) {
                                throw Err.process(e1);
                            }
                        }
                    }
                });
                ret = messagePane;
            }
        }

        if (ret instanceof Component) {
            final Component component = (Component) ret;
            new ComponentStandardizer(component).standardize();
        }

        return ret;
    }
}
