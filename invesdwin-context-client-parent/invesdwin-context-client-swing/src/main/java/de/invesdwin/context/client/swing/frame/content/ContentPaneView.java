package de.invesdwin.context.client.swing.frame.content;

import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JFrame;
import javax.swing.JPanel;

import bibliothek.gui.Dockable;
import bibliothek.gui.dock.common.CContentArea;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.CWorkingArea;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.CommonDockable;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.gui.dock.control.focus.DefaultFocusRequest;
import bibliothek.gui.dock.event.DockableFocusEvent;
import bibliothek.gui.dock.event.DockableFocusListener;
import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.client.swing.api.guiservice.ContentPane;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.context.client.swing.api.view.IDockable;
import de.invesdwin.context.client.swing.frame.app.DelegateRichApplication;
import de.invesdwin.context.client.swing.util.Views;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.swing.listener.KeyListenerSupport;
import de.invesdwin.util.swing.listener.MouseListenerSupport;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.util.time.date.FDates;

@NotThreadSafe
public class ContentPaneView extends AView<ContentPaneView, JPanel> {

    private CControl control;
    private CWorkingArea workingArea;
    private CContentArea contentArea;
    private long lastMouseClickTime = FDates.MIN_DATE.millisValue();
    @SuppressWarnings("deprecation")
    private long lastMouseClickInstant = Instant.DUMMY.nanosValue();

    @Override
    protected JPanel initComponent() {
        final DelegateRichApplication app = DelegateRichApplication.getInstance();
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

        control.getController().getFocusController().addDockableFocusListener(new DockableFocusListener() {
            private int prevDockableHashCode = Integer.MIN_VALUE;

            @Override
            public void dockableFocused(final DockableFocusEvent paramDockableFocusEvent) {
                final Dockable newDockable = paramDockableFocusEvent.getNewFocusOwner();
                if (newDockable == null) {
                    return;
                }
                final int newDockableHashCode = newDockable.hashCode();
                if (prevDockableHashCode != newDockableHashCode) {
                    //invoke later so that the componend is really focused
                    EventDispatchThreadUtil.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            final AView<?, ?> view = Views.getViewAt(newDockable);
                            Views.triggerOnShowing(view);
                        }
                    });
                    prevDockableHashCode = newDockableHashCode;
                }
            }
        });

        control.getController().getGlobalMouseDispatcher().addMouseListener(new MouseListenerSupport() {
            @Override
            public void mouseReleased(final MouseEvent e) {
                lastMouseClickTime = System.currentTimeMillis();
                lastMouseClickInstant = System.nanoTime();
            }
        });

        return panel;
    }

    @Override
    protected void onOpen() {
        ContentPaneKeyEventDispatcher.INSTANCE.register();
    }

    @Override
    protected void onClose() {
        ContentPaneKeyEventDispatcher.INSTANCE.unregister();
    }

    public CDockable getFocusedDockable() {
        return control.getFocusedCDockable();
    }

    public IDockable addView(final AView<?, ?> view, final IWorkingAreaLocation location) {
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
        setLocation(dockable, location);
        dockable.setVisible(true);
        return dockable;
    }

    private void setLocation(final ContentPaneDockable dockable, final IWorkingAreaLocation location) {
        if (location == null) {
            dockable.setLocationsAsideFocused();
        } else {
            location.setLocation(dockable, workingArea);
        }
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

    public FDate getLastMouseClickTime() {
        return new FDate(lastMouseClickTime);
    }

    public Instant getLastMouseClickInstant() {
        return new Instant(lastMouseClickInstant);
    }

    public boolean isControlDown() {
        return ContentPaneKeyEventDispatcher.INSTANCE.isControlDown();
    }

    public boolean isShiftDown() {
        return ContentPaneKeyEventDispatcher.INSTANCE.isShiftDown();
    }

    public boolean isAltDown() {
        return ContentPaneKeyEventDispatcher.INSTANCE.isAltDown();
    }

    public boolean isAltGraphDown() {
        return ContentPaneKeyEventDispatcher.INSTANCE.isAltGraphDown();
    }

    public boolean isMetaDown() {
        return ContentPaneKeyEventDispatcher.INSTANCE.isMetaDown();
    }

    public boolean isModifierDown() {
        return ContentPaneKeyEventDispatcher.INSTANCE.isModifierDown();
    }

}
