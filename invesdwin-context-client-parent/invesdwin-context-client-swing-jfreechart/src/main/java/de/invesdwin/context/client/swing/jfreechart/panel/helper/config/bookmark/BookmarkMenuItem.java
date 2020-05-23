package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.bookmark;

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

    public static final int MAX_RECENTLY_USED = 10;

    private final PlotConfigurationHelper plotConfigurationHelper;
    private final Bookmark bookmark;
    private final String label;
    private boolean highlighted;

    public BookmarkMenuItem(final PlotConfigurationHelper plotConfigurationHelper, final Bookmark bookmark) {
        this.plotConfigurationHelper = plotConfigurationHelper;
        this.bookmark = bookmark;
        this.label = bookmark.toString();
        setText(label);
        init();
    }

    private void init() {
        addMouseListener(new MouseListenerSupport() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    plotConfigurationHelper.getChartPanel().setVisibleTimeRangeOrReloadData(bookmark);
                    plotConfigurationHelper.getBookmarkStorage().addValue(bookmark);
                }
            }
        });
        final JMenuItem bookmarkRestoreItem = new JMenuItem("Restore");
        bookmarkRestoreItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                plotConfigurationHelper.getChartPanel().setVisibleTimeRangeOrReloadData(bookmark);
                plotConfigurationHelper.getBookmarkStorage().addValue(bookmark);
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
    }

    public void setHighlighted(final boolean highlighted) {
        this.highlighted = highlighted;
        if (highlighted) {
            setText("<html><b>" + label + "</b>");
        } else {
            setText(label);
        }
    }

    public boolean isHighlighted() {
        return highlighted;
    }

}
