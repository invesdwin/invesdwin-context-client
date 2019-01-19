package de.invesdwin.context.client.swing.internal.content;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import javax.swing.JFrame;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

import bibliothek.gui.Dockable;
import bibliothek.gui.dock.common.CContentArea;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.CWorkingArea;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.CommonDockable;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.gui.dock.control.focus.DefaultFocusRequest;
import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.api.DockableContent;
import de.invesdwin.context.client.swing.api.guiservice.ContentPane;

@NotThreadSafe
public class ContentPaneView extends AView<ContentPaneView, CContentArea> {

    @Inject
    private ContentPane contentPane;

    private CControl control;
    private CWorkingArea workingArea;

    @Override
    protected CContentArea initComponent() {
        final SingleFrameApplication app = (SingleFrameApplication) Application.getInstance();
        final JFrame frame = app.getMainFrame();
        this.control = new CControl(frame);
        control.addGlobalKeyListener(new KeyListener() {
            @Override
            public void keyTyped(final KeyEvent e) {}

            @Override
            public void keyReleased(final KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_W) {
                    EventDispatchThreadUtil.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            final CDockable focused = control.getFocusedCDockable();
                            if (focused != null && focused.isCloseable() && contentPane.containsDockable(focused)) {
                                contentPane.removeDockable(focused);
                            }
                        }
                    });
                }
            }

            @Override
            public void keyPressed(final KeyEvent e) {}
        });
        final CGrid grid = new CGrid(control);
        this.workingArea = control.createWorkingArea("workingArea");
        grid.add(1, 1, 1, 1, workingArea);
        final CContentArea contentArea = control.getContentArea();
        contentArea.deploy(grid);

        control.getController().setTheme(new CustomTheme());
        return contentArea;
    }

    public DockableContent addView(final ContentPane contentPane, final AView<?, ?> view) {
        final String uniqueId = DockableContentIdGenerator.newId(view);
        final DockableContent dockable = new DockableContent(uniqueId, view.getIcon(), view.getTitle(),
                view.getComponent());
        dockable.setTitleToolTip(view.getDescription());
        workingArea.show(dockable);
        return dockable;
    }

    public boolean removeView(final AView<?, ?> view) {
        final boolean removed = control.removeDockable(view.getDockable());
        if (removed) {
            updateFocusViaHistory();
        }
        return removed;
    }

    private void updateFocusViaHistory() {
        final Dockable[] history = control.getController().getFocusHistory().getHistory();
        for (int i = history.length - 1; i >= 0; i--) {
            final Dockable next = history[i];
            if (next instanceof CommonDockable) {
                final CDockable cdockable = ((CommonDockable) next).getDockable();
                if (cdockable.getExtendedMode() != ExtendedMode.MINIMIZED) {
                    control.getController()
                            .setFocusedDockable(
                                    new DefaultFocusRequest(cdockable.intern(), cdockable.getFocusComponent(), true));
                    break;
                }
            }
        }
    }

    public boolean containsView(final AView<?, ?> view) {
        return getComponent().getControl().getSingleDockable(view.getDockableUniqueId()) != null;
    }
}
