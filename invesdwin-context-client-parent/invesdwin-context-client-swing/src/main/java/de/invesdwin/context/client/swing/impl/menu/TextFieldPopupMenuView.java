package de.invesdwin.context.client.swing.impl.menu;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import de.invesdwin.context.client.swing.RichApplicationContextLocation;
import de.invesdwin.context.client.swing.api.AView;

/**
 * To make the ProxyActions work properly, this PopupMenu cannot be set via textComponent.setComponentPopupMenu().
 * Instead during initialization of this View, the MouseListener gets bount to the compnent, which gets the focus for
 * the compoennt before the PopupMenu is made visible. Only with this, the Actions work on the proper component, because
 * that is determined by the current FocusOwner.
 * 
 * @author subes
 * 
 */
@SuppressWarnings("serial")
@NotThreadSafe
public class TextFieldPopupMenuView extends AView<TextFieldPopupMenuView, JPopupMenu> {

    private final JTextComponent textComponent;

    public TextFieldPopupMenuView(final JTextComponent textComponent) {
        this.textComponent = textComponent;
    }

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    protected JPopupMenu initComponent() {
        final JPopupMenu popupMenu = new JPopupMenu();

        if (isAdditionalActionsSupported()) {
            final JMenuItem mntmUndo = new JMenuItem("Undo");
            mntmUndo.setName("undo");
            popupMenu.add(mntmUndo);

            final JMenuItem mntmRedo = new JMenuItem("Redo");
            mntmRedo.setName("redo");
            popupMenu.add(mntmRedo);

            final JSeparator separator = new JSeparator();
            popupMenu.add(separator);
        }

        final JMenuItem mntmCut = new JMenuItem("Cut");
        mntmCut.setName("cut");
        popupMenu.add(mntmCut);

        final JMenuItem mntmCopy = new JMenuItem("Copy");
        mntmCopy.setName("copy");
        popupMenu.add(mntmCopy);

        final JMenuItem mntmPaste = new JMenuItem("Insert");
        mntmPaste.setName("paste");
        popupMenu.add(mntmPaste);

        final JMenuItem mntmDelete = new JMenuItem("Delete");
        mntmDelete.setName("delete");
        popupMenu.add(mntmDelete);

        if (isAdditionalActionsSupported()) {
            final JSeparator separator_1 = new JSeparator();
            popupMenu.add(separator_1);

            final JMenuItem mntmSelectAll = new JMenuItem("Select All");
            mntmSelectAll.setName("select-all");
            popupMenu.add(mntmSelectAll);
        }

        //FocusOwner workaround
        if (textComponent != null) {
            textComponent.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(final MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        textComponent.requestFocusInWindow();
                        popupMenu.show(textComponent, e.getX(), e.getY());
                    }
                }
            });
        }

        return popupMenu;
    }

    protected boolean isAdditionalActionsSupported() {
        return RichApplicationContextLocation.isActivated();
    }
}
