package de.invesdwin.context.client.swing.frame.content;

import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

import bibliothek.gui.Dockable;
import bibliothek.gui.dock.common.CContentArea;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.CWorkingArea;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.CommonDockable;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.gui.dock.control.focus.DefaultFocusRequest;
import bibliothek.util.Filter;
import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.client.swing.api.guiservice.ContentPane;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.context.client.swing.api.view.IDockable;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.swing.listener.KeyListenerSupport;

@NotThreadSafe
public class ContentPaneView extends AView<ContentPaneView, JPanel> {

    private CControl control;
    private CWorkingArea workingArea;
    private CContentArea contentArea;
    private boolean controlDown;
    private boolean metaDown;
    private boolean shiftDown;
    private boolean altDown;

    @Override
    protected JPanel initComponent() {
        final SingleFrameApplication app = (SingleFrameApplication) Application.getInstance();
        final JFrame frame = app.getMainFrame();
        this.control = new CControl(frame);
        control.addGlobalKeyListener(new KeyListenerSupport() {
            //only activate once when both pressed and released happened on this window
            private boolean pressed;

            @Override
            public void keyReleased(final KeyEvent e) {
                if (pressed && e.isControlDown() && e.getKeyCode() == KeyEvent.VK_W) {
                    EventDispatchThreadUtil.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            final CDockable focused = control.getFocusedCDockable();
                            final ContentPane contentPane = GuiService.get().getContentPane();
                            if (focused != null && focused.isCloseable() && contentPane.containsDockable(focused)) {
                                focused.setVisible(false); //use default close operation
                            }
                        }
                    });
                }
                pressed = false;
            }

            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_W) {
                    pressed = true;
                }
            }
        });
        final CGrid grid = new CGrid(control);
        this.workingArea = control.createWorkingArea(ContentPaneView.class.getSimpleName());
        grid.add(1, 1, 1, 1, workingArea);
        this.contentArea = control.getContentArea();
        contentArea.deploy(grid);
        control.getController().setTheme(new CustomTheme());

        final JPanel panel = new JPanel();
        panel.setLayout(new GridLayout());
        panel.add(contentArea);

        final KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(final KeyEvent e) {
                switch (e.getID()) {
                case KeyEvent.KEY_PRESSED:
                    updateKeyDown(e, true);
                    break;
                case KeyEvent.KEY_RELEASED:
                    updateKeyDown(e, false);
                    break;
                default:
                    break;
                }
                return false;
            }

            private void updateKeyDown(final KeyEvent e, final boolean state) {
                switch (e.getKeyCode()) {
                case KeyEvent.VK_META:
                    metaDown = state;
                    break;
                case KeyEvent.VK_CONTROL:
                    controlDown = state;
                    break;
                case KeyEvent.VK_SHIFT:
                    shiftDown = state;
                    break;
                case KeyEvent.VK_ALT:
                    altDown = state;
                    break;
                default:
                    break;
                }
            }
        });

        return panel;
    }

    public IDockable addView(final AView<?, ?> view, final WorkingAreaLocation location) {
        if (control == null) {
            Assertions.checkNotNull(getComponent());
        }
        final String uniqueId = DockableIdGenerator.newId(view);
        String title = view.getTitle();
        if (title == null) {
            title = uniqueId;
        }
        final ContentPaneDockable dockable = new ContentPaneDockable(uniqueId, view.getIcon(), title,
                view.getComponent(), location);
        dockable.setTitleToolTip(view.getDescription());
        workingArea.add(dockable);
        if (location == null) {
            dockable.setLocationsAsideFocused();
        } else {
            final boolean found = dockable.setLocationsAside(new Filter<CDockable>() {
                @Override
                public boolean includes(final CDockable d) {
                    if (d != dockable && d instanceof ContentPaneDockable) {
                        final ContentPaneDockable c = (ContentPaneDockable) d;
                        if (c.getLocation() == location) {
                            return true;
                        }
                    }
                    return false;
                }
            });
            if (!found) {
                dockable.setLocation(location.create(CLocation.working(workingArea)));
            }
        }
        dockable.setVisible(true);
        return dockable;
    }

    public boolean removeView(final AView<?, ?> view) {
        if (control == null) {
            return false;
        }
        final boolean removed = control.removeDockable((ContentPaneDockable) view.getDockable());
        if (removed) {
            updateFocusViaHistory();
        }
        return removed;
    }

    private void updateFocusViaHistory() {
        if (control == null) {
            return;
        }
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
        if (control == null) {
            return false;
        }
        return control.getSingleDockable(view.getDockableUniqueId()) != null;
    }

    public CControl getControl() {
        return control;
    }

    public CContentArea getContentArea() {
        return contentArea;
    }

    public CWorkingArea getWorkingArea() {
        return workingArea;
    }

    public boolean isControlDown() {
        return controlDown;
    }

    public boolean isShiftDown() {
        return shiftDown;
    }

    public boolean isAltDown() {
        return altDown;
    }

    public boolean isMetaDown() {
        return metaDown;
    }
}
