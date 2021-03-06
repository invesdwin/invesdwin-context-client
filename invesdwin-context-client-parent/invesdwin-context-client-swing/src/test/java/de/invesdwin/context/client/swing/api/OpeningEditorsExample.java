
package de.invesdwin.context.client.swing.api;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.CWorkingArea;
import bibliothek.gui.dock.common.DefaultMultipleCDockable;
import bibliothek.gui.dock.common.DefaultSingleCDockable;

// CHECKSTYLE:OFF
@NotThreadSafe
public class OpeningEditorsExample {
    public static void main(final String[] args) {
        /*
         * A common task is to open yet another CDockable. When opening a CDockable we want that dockable to show up
         * close to the currently focused dockable. CDockable offers several methods to do that, all have a name like
         * "CDockable.setLocationsAside...".
         * 
         * If opening CDockables on a CWorkingArea we can also make use of the "show" method, which not only sets the
         * location, but also registers the Dockable at the CControl.
         */

        final JFrame frame = new JFrame();
        final CControl control = new CControl(frame);

        frame.add(control.getContentArea());

        final CWorkingArea work = control.createWorkingArea("work");

        final CGrid grid = new CGrid(control);
        grid.add(1, 1, 3, 3, work);
        grid.add(0, 0, 1, 4, new DefaultSingleCDockable("Outline"));
        grid.add(1, 3, 3, 1, new DefaultSingleCDockable("Console"));
        control.getContentArea().deploy(grid);

        final JMenuBar menubar = new JMenuBar();
        final JMenu menu = new JMenu("Editors");
        menubar.add(menu);
        final JMenuItem openEditor = new JMenuItem("Open new editor");
        menu.add(openEditor);
        frame.setJMenuBar(menubar);

        openEditor.addActionListener(new ActionListener() {
            private int count = 0;

            @Override
            public void actionPerformed(final ActionEvent e) {
                final DefaultMultipleCDockable editor = new DefaultMultipleCDockable(null);
                editor.setTitleText("Editor " + (count++));
                editor.setCloseable(true);

                /* All that is needed to show "editor" aside the currently focused CDockable, is calling "show". */
                work.show(editor);
                editor.toFront();
            }
        });

        frame.setVisible(true);
    }
}