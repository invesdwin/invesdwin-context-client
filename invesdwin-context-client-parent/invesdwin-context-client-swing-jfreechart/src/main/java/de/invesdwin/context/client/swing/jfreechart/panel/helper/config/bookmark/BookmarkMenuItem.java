package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.bookmark;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.PlotConfigurationHelper;
import de.invesdwin.util.swing.listener.MouseListenerSupport;
import de.invesdwin.util.time.fdate.FDate;

@NotThreadSafe
public class BookmarkMenuItem extends JMenu {

    private final PlotConfigurationHelper plotConfigurationHelper;
    private final Bookmark bookmark;
    private final String label;
    private boolean highlighted;
    private boolean pendingRestore;
    private boolean pendingRemove;

    public BookmarkMenuItem(final PlotConfigurationHelper plotConfigurationHelper, final Bookmark bookmark) {
        this.plotConfigurationHelper = plotConfigurationHelper;
        this.bookmark = bookmark;
        this.label = bookmark.toString();
        init();
        updateLabel();
    }

    private void init() {
        addMouseListener(new MouseListenerSupport() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    plotConfigurationHelper.getChartPanel().setVisibleTimeRangeOrReloadData(bookmark);
                    resetPendingRestoreRecursive(getParent());
                    setPendingRestore(true);
                } else if (e.getButton() == MouseEvent.BUTTON2) {
                    setPendingRemove(!isPendingRemove());
                }
            }

        });
        final JMenuItem bookmarkRestoreItem = new JMenuItem("Restore");
        bookmarkRestoreItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                plotConfigurationHelper.getChartPanel().setVisibleTimeRangeOrReloadData(bookmark);
                updateLastUsed();
                runPendingActionsRecursive(getParent(), false);
            }
        });
        add(bookmarkRestoreItem);
        final JMenuItem bookmarkRemoveItem = new JMenuItem("Remove");
        bookmarkRemoveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                setPendingRemove(true);
                runPendingActionsRecursive(getParent(), false);
            }
        });
        add(bookmarkRemoveItem);
        final JMenuItem bookmarkLastUsedItem = new JMenuItem("Last Used: " + bookmark.getLastUsed().toString());
        bookmarkLastUsedItem.setEnabled(false);
        add(bookmarkLastUsedItem);
    }

    public void setHighlighted(final boolean highlighted) {
        this.highlighted = highlighted;
        updateLabel();
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setPendingRestore(final boolean pending) {
        this.pendingRestore = pending;
        updateLabel();
    }

    public boolean isPendingRestore() {
        return pendingRestore;
    }

    public void setPendingRemove(final boolean pendingRemove) {
        this.pendingRemove = pendingRemove;
        updateLabel();
    }

    public boolean isPendingRemove() {
        return pendingRemove;
    }

    private void updateLabel() {
        final StringBuilder sb = new StringBuilder("<html>");
        if (pendingRestore) {
            sb.append("*");
        }
        if (highlighted) {
            sb.append("<b>");
        }
        if (pendingRemove) {
            sb.append("<strike>");
        }
        sb.append(label);
        if (pendingRemove) {
            sb.append("</strike>");
        }
        if (highlighted) {
            sb.append("</b>");
        }
        setText(sb.toString());
    }

    public void updateLastUsed() {
        plotConfigurationHelper.getBookmarkStorage().putValue(new Bookmark(bookmark, new FDate()));
    }

    public void remove() {
        plotConfigurationHelper.getBookmarkStorage().removeValue(bookmark);
    }

    public static void runPendingActionsRecursive(final Container root, final boolean allowRestore) {
        final Component[] components = root.getComponents();
        runPendingActionsRecursive(components, allowRestore);
        if (root instanceof JMenu) {
            final JMenu cRoot = (JMenu) root;
            final Component[] menuComponents = cRoot.getMenuComponents();
            runPendingActionsRecursive(menuComponents, allowRestore);
        }
    }

    private static void runPendingActionsRecursive(final Component[] components, final boolean allowRestore) {
        for (final Component component : components) {
            if (component instanceof BookmarkMenuItem) {
                final BookmarkMenuItem cComponent = (BookmarkMenuItem) component;
                if (cComponent.isPendingRemove()) {
                    cComponent.remove();
                } else if (cComponent.isPendingRestore()) {
                    cComponent.updateLastUsed();
                }
            } else if (component instanceof Container) {
                final Container cComponent = (Container) component;
                runPendingActionsRecursive(cComponent, allowRestore);
            }
        }
    }

    private static void resetPendingRestoreRecursive(final Container root) {
        final Component[] components = root.getComponents();
        resetPendingRestoreRecursive(components);
        if (root instanceof JMenu) {
            final JMenu cRoot = (JMenu) root;
            final Component[] menuComponents = cRoot.getMenuComponents();
            resetPendingRestoreRecursive(menuComponents);
        }
    }

    private static void resetPendingRestoreRecursive(final Component[] components) {
        for (final Component component : components) {
            if (component instanceof BookmarkMenuItem) {
                final BookmarkMenuItem cComponent = (BookmarkMenuItem) component;
                cComponent.setPendingRestore(false);
            } else if (component instanceof Container) {
                final Container cComponent = (Container) component;
                resetPendingRestoreRecursive(cComponent);
            }
        }
    }
}
