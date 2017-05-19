package de.invesdwin.context.client.swing.util;

import java.awt.Component;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.text.JTextComponent;

/**
 * For instance this sets PopupMenus or changes the look to fulfill standards. This should be invoked on any component,
 * before it is displayed.
 * 
 * @author subes
 * 
 */
@NotThreadSafe
public class ComponentStandardizer {

    private final Component rootComponent;

    public ComponentStandardizer(final Component rootComponent) {
        this.rootComponent = rootComponent;
    }

    public void standardize() {
        new AComponentVisitor() {
            @Override
            protected void visit(final Component component) {
                if (component instanceof JTextComponent) {
                    final JTextComponent text = ((JTextComponent) component);
                    new de.invesdwin.context.client.swing.internal.menu.TextFieldPopupMenuView(text).getComponent();
                }
            }
        }.visitAll(rootComponent);
    }

}
