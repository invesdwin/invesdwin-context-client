package de.invesdwin.context.client.swing.internal.content;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JTabbedPane;

import bibliothek.gui.dock.station.stack.StackDockComponent;
import bibliothek.gui.dock.station.stack.StackDockComponentFactory;
import bibliothek.gui.dock.station.stack.StackDockComponentParent;
import bibliothek.gui.dock.themes.BasicTheme;
import bibliothek.gui.dock.themes.basic.BasicStackDockComponent;
import bibliothek.gui.dock.util.Priority;

// CHECKSTYLE:OFF
@NotThreadSafe
public class CustomTheme extends BasicTheme {

    public CustomTheme() {
        setStackDockComponentFactory(new StackDockComponentFactory() {
            @Override
            public StackDockComponent create(final StackDockComponentParent station) {
                final BasicStackDockComponent dock = new BasicStackDockComponent(station);
                dock.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
                return dock;
            }
        }, Priority.DEFAULT);
    }

}
