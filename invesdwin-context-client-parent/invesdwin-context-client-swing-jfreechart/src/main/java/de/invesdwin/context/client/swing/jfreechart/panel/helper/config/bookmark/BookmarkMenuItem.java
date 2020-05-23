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

@NotThreadSafe
public class BookmarkMenuItem extends JMenu {

    private final PlotConfigurationHelper plotConfigurationHelper;
    private final Bookmark bookmark;
    private final String label;
    private boolean highlighted;
    private boolean pending;

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
                    resetPendingRecursive(getParent());
                    setPending(true);
                }
            }

        });
        final JMenuItem bookmarkRestoreItem = new JMenuItem("Restore");
        bookmarkRestoreItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                plotConfigurationHelper.getChartPanel().setVisibleTimeRangeOrReloadData(bookmark);
                updateLastUsed();
            }
        });
        add(bookmarkRestoreItem);
        final JMenuItem bookmarkRemoveItem = new JMenuItem("Remove");
        bookmarkRemoveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                plotConfigurationHelper.getBookmarkStorage().removeValue(bookmark);
            }
        });
        add(bookmarkRemoveItem);
        final JMenuItem bookmarkLastUsedItem = new JMenuItem("Last Used: " + bookmark.getLastUsed().toString());
        bookmarkLastUsedItem.setEnabled(false);
        add(bookmarkLastUsedItem);
    }

    private void resetPendingRecursive(final Container root) {
        for (final Component component : root.getComponents()) {
            if (component instanceof BookmarkMenuItem) {
                final BookmarkMenuItem cComponent = (BookmarkMenuItem) component;
                cComponent.setPending(false);
            } else if (component instanceof Container) {
                final Container cComponent = (Container) component;
                resetPendingRecursive(cComponent);
            }
        }
    }

    public void setHighlighted(final boolean highlighted) {
        this.highlighted = highlighted;
        updateLabel();
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setPending(final boolean pending) {
        this.pending = pending;
        updateLabel();
    }

    public boolean isPending() {
        return pending;
    }

    private void updateLabel() {
        final String usedLabel;
        if (pending) {
            usedLabel = "*" + label;
        } else {
            usedLabel = label;
        }
        if (highlighted) {
            setText("<html><b>" + usedLabel + "</b>");
        } else {
            setText(usedLabel);
        }
    }

    public void updateLastUsed() {
        plotConfigurationHelper.getBookmarkStorage().addValue(bookmark);
    }
}
