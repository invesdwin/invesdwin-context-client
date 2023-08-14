package de.invesdwin.context.client.swing.jfreechart.panel.helper.config;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartUtils;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.basis.CustomChartTransferable;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.PlotResizeHelper;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.bookmark.Bookmark;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.bookmark.BookmarkMenuItem;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.bookmark.HeapBookmarkStorage;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.bookmark.IBookmarkStorage;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.dialog.SettingsDialog;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.AddSeriesDialog;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.expression.IExpressionSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.indicator.IIndicatorSeriesProvider;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.legend.HighlightedLegendInfo;
import de.invesdwin.context.client.swing.jfreechart.plot.Axises;
import de.invesdwin.context.client.swing.jfreechart.plot.dataset.IPlotSourceDataset;
import de.invesdwin.util.lang.string.Strings;
import de.invesdwin.util.swing.Dialogs;
import de.invesdwin.util.swing.listener.PopupMenuListenerSupport;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.util.time.range.TimeRange;

@NotThreadSafe
public class PlotConfigurationHelper {

    /**
     * Default is a shared instance in memory.
     */
    private static final HeapBookmarkStorage DEFAULT_BOOKMARK_STORAGE = new HeapBookmarkStorage();

    private final InteractiveChartPanel chartPanel;

    private final PriceInitialSettings priceInitialSettings;
    private final Map<String, SeriesInitialSettings> seriesId_initialSettings = new HashMap<>();

    private JPopupMenu popupMenu;
    private JMenuItem titleItem;
    private HighlightedLegendInfo highlighted;

    private JMenuItem configureSeriesItem;
    private JMenuItem removeSeriesItem;
    private JMenuItem removeAllSeriesItem;
    private JMenuItem showSeriesItem;
    private JMenuItem hideSeriesItem;

    private JMenu bookmarksItem;

    private JMenuItem addSeriesItem;
    private JMenuItem copyToClipboardItem;
    private JMenuItem saveAsPNGItem;
    private JMenuItem helpItem;

    private JMenuItem rangeAxisIdItem;
    private JCheckBoxMenuItem autoRangeItem;

    private final Map<String, IIndicatorSeriesProvider> indicatorSeriesProviders = new TreeMap<>();
    private IExpressionSeriesProvider expressionSeriesProvider;
    private IPlotPopupMenuConfig plotPopupMenuConfig;
    private Point2D mousePositionOnPopupMenu;

    private IBookmarkStorage bookmarkStorage = DEFAULT_BOOKMARK_STORAGE;

    public PlotConfigurationHelper(final InteractiveChartPanel chartPanel) {
        this.chartPanel = chartPanel;
        initPopupMenu();

        priceInitialSettings = new PriceInitialSettings(this);
    }

    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }

    public InteractiveChartPanel getChartPanel() {
        return chartPanel;
    }

    public PriceInitialSettings getPriceInitialSettings() {
        return priceInitialSettings;
    }

    private void initPopupMenu() {

        titleItem = new JMenuItem("");
        titleItem.setEnabled(false);

        initRangeAxisItems();
        initSeriesVisibilityItems();
        initAddSeriesItem();
        initBookmarkItems();
        initExportItems();
        initHelpItem();

        popupMenu = new JPopupMenu();
        popupMenu.addPopupMenuListener(new PopupMenuListenerSupport() {

            @Override
            public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
                popupMenu.removeAll();
                highlighted = chartPanel.getPlotLegendHelper().getHighlightedLegendInfo();
                if (highlighted != null) {
                    if (!highlighted.isDatasetVisible()) {
                        popupMenu.add(titleItem);
                        if (highlighted.isRemovable()) {
                            popupMenu.add(removeSeriesItem);
                            popupMenu.add(removeAllSeriesItem);
                        }
                        popupMenu.add(showSeriesItem);
                    } else {
                        addSeriesConfigMenuItems();
                    }
                } else {
                    if (Axises.isRangeAxisArea(chartPanel, mousePositionOnPopupMenu)) {
                        final ValueAxis rangeAxis = Axises.getRangeAxis(chartPanel, mousePositionOnPopupMenu);
                        rangeAxisIdItem.setText(Axises.getRangeAxisId(rangeAxis));
                        popupMenu.add(rangeAxisIdItem);

                        autoRangeItem.setSelected(rangeAxis.isAutoRange());
                        popupMenu.add(autoRangeItem);
                        popupMenu.addSeparator();
                    }

                    boolean addSeparator = false;
                    if (!indicatorSeriesProviders.isEmpty() || expressionSeriesProvider != null) {
                        popupMenu.add(addSeriesItem);
                        addSeparator = true;
                    }
                    if (plotPopupMenuConfig != null) {
                        final List<JMenuItem> addMenuItems = plotPopupMenuConfig.getAddMenuItems();
                        for (int i = 0; i < addMenuItems.size(); i++) {
                            popupMenu.add(addMenuItems.get(i));
                        }
                    }
                    if (bookmarkStorage != null) {
                        updateBookmarksItems();
                        popupMenu.add(bookmarksItem);
                        addSeparator = true;
                    }
                    if (addSeparator) {
                        popupMenu.addSeparator();
                    }
                    popupMenu.add(copyToClipboardItem);
                    popupMenu.add(saveAsPNGItem);
                    popupMenu.addSeparator();
                    popupMenu.add(helpItem);
                }

                chartPanel.getPlotNavigationHelper().mouseExited();
            }

            private void updateBookmarksItems() {
                bookmarksItem.removeAll();
                final List<Bookmark> bookmarks = bookmarkStorage.getValues();
                final TimeRange visibleTimeRange = chartPanel.getVisibleTimeRange();
                boolean visibleTimeRangeExists = false;
                if (!bookmarks.isEmpty()) {
                    for (final Bookmark bookmark : bookmarks) {
                        final BookmarkMenuItem bookmarkItem = new BookmarkMenuItem(PlotConfigurationHelper.this,
                                bookmark);
                        final boolean highlight = visibleTimeRange != null && visibleTimeRange.equals(bookmark);
                        if (highlight) {
                            bookmarkItem.setHighlighted(highlight);
                            visibleTimeRangeExists = true;
                        }
                        bookmarksItem.add(bookmarkItem);
                    }
                    bookmarksItem.addSeparator();
                }
                if (visibleTimeRange != null && !visibleTimeRangeExists) {
                    final JMenuItem bookmarksRememberItem = new JMenuItem(
                            "<html>Remember: <b>" + visibleTimeRange + "</b>");
                    bookmarksRememberItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(final ActionEvent e) {
                            bookmarkStorage.putValue(new Bookmark(visibleTimeRange, new FDate()));
                            chartPanel.setVisibleTimeRange(visibleTimeRange);
                        }
                    });
                    bookmarksItem.add(bookmarksRememberItem);
                }
                if (!bookmarks.isEmpty()) {
                    final JMenuItem bookmarksRemoveAllItem = new JMenuItem("Remove All");
                    bookmarksRemoveAllItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(final ActionEvent e) {
                            bookmarkStorage.clear();
                        }
                    });
                    bookmarksItem.add(bookmarksRemoveAllItem);
                    final JMenuItem bookmarksCancelItem = new JMenuItem("Cancel");
                    bookmarksCancelItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(final ActionEvent e) {
                            for (final Component component : bookmarksItem.getMenuComponents()) {
                                if (component instanceof BookmarkMenuItem) {
                                    final BookmarkMenuItem cComponent = (BookmarkMenuItem) component;
                                    cComponent.setPendingRestore(false);
                                }
                            }
                            getChartPanel().setVisibleTimeRangeOrReloadData(visibleTimeRange);
                        }
                    });
                    bookmarksItem.add(bookmarksCancelItem);
                }
            }

            private void addSeriesConfigMenuItems() {
                if (highlighted.isPriceSeries()) {
                    titleItem.setText(
                            Strings.abbreviate(chartPanel.getMasterDataset().getSeriesTitle(), 50) + " - Series");
                } else {
                    titleItem.setText(Strings.abbreviate(highlighted.getSeriesTitle(), 50) + " - Series");
                }
                popupMenu.add(titleItem);
                popupMenu.addSeparator();
                popupMenu.add(configureSeriesItem);
                popupMenu.addSeparator();
                if (highlighted.isRemovable()) {
                    popupMenu.add(removeSeriesItem);
                    popupMenu.add(removeAllSeriesItem);
                }
                popupMenu.add(hideSeriesItem);
            }

            @Override
            public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
                chartPanel.getPlotLegendHelper().disableHighlighting();
            }

            @Override
            public void popupMenuCanceled(final PopupMenuEvent e) {
                highlighted = null;
                //only the first popup should have the crosshair visible
                chartPanel.mouseExited();

                BookmarkMenuItem.runPendingActionsRecursive(bookmarksItem, true);
            }
        });

    }

    private void initRangeAxisItems() {
        this.rangeAxisIdItem = new JMenuItem();
        this.rangeAxisIdItem.setEnabled(false);

        this.autoRangeItem = new JCheckBoxMenuItem("Auto-Range");
        this.autoRangeItem.setSelected(true);

        this.autoRangeItem.addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(final MouseEvent e) {
                if (e.isControlDown()) {
                    Axises.resetAllAutoRanges(chartPanel);
                } else {
                    final ValueAxis rangeAxis = Axises.getRangeAxis(chartPanel, mousePositionOnPopupMenu);
                    //Should never be null. Safety first though.
                    if (rangeAxis != null) {
                        rangeAxis.setAutoRange(autoRangeItem.isSelected());
                        final XYPlot subplot = (XYPlot) rangeAxis.getPlot();
                        subplot.setRangePannable(!Axises.isEveryAxisAutoRange(subplot));
                    }
                }
            }

            @Override
            public void mousePressed(final MouseEvent e) {
                //noop
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                //noop
            }

            @Override
            public void mouseEntered(final MouseEvent e) {
                //noop
            }

            @Override
            public void mouseClicked(final MouseEvent e) {
                //noop
            }
        });
    }

    private void initBookmarkItems() {
        bookmarksItem = new JMenu("Bookmarks");
    }

    public SeriesInitialSettings getOrCreateSeriesInitialSettings(final HighlightedLegendInfo highlighted) {
        SeriesInitialSettings seriesInitialSettings = seriesId_initialSettings.get(highlighted.getSeriesId());
        if (seriesInitialSettings == null) {
            seriesInitialSettings = new SeriesInitialSettings(highlighted.getRenderer());
            seriesId_initialSettings.put(highlighted.getSeriesId(), seriesInitialSettings);
        }
        return seriesInitialSettings;
    }

    public SeriesInitialSettings getSeriesInitialSettings(final HighlightedLegendInfo highlighted) {
        return seriesId_initialSettings.get(highlighted.getSeriesId());
    }

    private void initSeriesVisibilityItems() {
        configureSeriesItem = new JMenuItem("Configure");
        configureSeriesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final SettingsDialog dialog = new SettingsDialog(PlotConfigurationHelper.this, highlighted);
                dialog.setVisible(true);
            }
        });

        removeSeriesItem = new JMenuItem("Remove");
        removeSeriesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                highlighted.removeSeries();
            }
        });

        removeAllSeriesItem = new JMenuItem("Remove All");
        removeAllSeriesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                highlighted.removeAllSeries();
            }
        });

        showSeriesItem = new JMenuItem("Show");
        showSeriesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                highlighted.setDatasetVisible(true);
            }
        });

        hideSeriesItem = new JMenuItem("Hide");
        hideSeriesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                highlighted.setDatasetVisible(false);
            }
        });
    }

    private void initAddSeriesItem() {
        addSeriesItem = new JMenuItem("Add Series");
        addSeriesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final AddSeriesDialog dialog = new AddSeriesDialog(PlotConfigurationHelper.this);
                dialog.setVisible(true);
            }
        });
    }

    private void initExportItems() {
        copyToClipboardItem = new JMenuItem("Copy To Clipboard");
        copyToClipboardItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                copyToClipboard();
            }
        });

        saveAsPNGItem = new JMenuItem("Save As PNG...");
        saveAsPNGItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                saveAsPNG();
            }
        });
    }

    private void initHelpItem() {
        helpItem = new JMenuItem("Help");
        helpItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final StringBuilder sb = new StringBuilder();
                sb.append("<html><h2>Usage:</h2>");
                sb.append("<ul>");
                sb.append(
                        "<li><b>Move Series</b>: Drag and drop a series legend to move it to a different plot pane. The green pane will add a new plot. "
                                + "<br>On removable series the red trash pane will remove it. You can combine multiple series into one plot pane."
                                + "<br>Empty plots will be removed automatically which is displayed in red as well.</li>");
                sb.append(
                        "<li><b>Show/Hide Series</b>: Left click a series legend to show/hide that series. Or you can also use the series context menu for that.</li>");
                sb.append(
                        "<li><b>Remove Series</b>: Middle click a series legend to remove that series. Or you can also use the series context menu for that.</li>");
                sb.append(
                        "<li><b>Series Context Menu</b>: Right click a series legend to get a context menu to modify its display settings."
                                + "<br>You can modify the series type, line style, line width and colors. The settings differ depending on the selected series type."
                                + "<br>The series style settings can also be reset to their initial values. If the series is removable, you can remove it here too."
                                + "<br>You can also show/hide the series using the context menu, though only non hidden series can be modified in style.</li>");
                sb.append(
                        "<li><b>Plot Pane Resizing</b>: Drag and drop a divider between two plot panes to change the size of those.</li>");
                sb.append(
                        "<li><b>Crosshair</b>: Move the mouse around with see the selected series values displayed in the series legends."
                                + "<br>If there is no crosshair visible, the latest values will be shown in the series legends.</li>");
                sb.append(
                        "<li><b>Navigation</b>: Move the mouse to the bottom center of the chart to show navigation buttons."
                                + "<br>This allows you to pan, zoom and reset the view. The navigation also works by hotkeys and mouse:");
                sb.append("<ul>");
                sb.append(
                        "<li><b>Panning</b>: Use left click and drag the mouse anywhere on the chart or use left/right arrow keys on your keyboard to pan the data."
                                + "<br>You can also use horizontal scrolling of your mouse or the shift key combined with your scroll wheel."
                                + "<br>By holding down the control key on your keyboard you can make the panning faster.</li>");
                sb.append(
                        "<li><b>Zooming</b>: Use your scroll wheel or +/- keys to zoom. When using the mouse to scroll, the mouse pointer will be used as an anchor.</li>");
                sb.append(
                        "<li><b>Resetting</b>: Click the reset button to revert the zoom and pan to the latest chart data."
                                + "<br>CTRL+Click to force a reload of the currently visible chart data.</li>");
                sb.append(
                        "<li><b>Y-Axis</b>: By default you can only pan on the X-Axis and the Y-Axis auto-ranges automatically depending on the data displayed."
                                + "<br>You can zoom on the Y-Axis of each series by Mouse-Scrolling or Mouse-Dragging on each seperate indicator as well."
                                + "<br>When you manually zoomed in or out on any of the series you are able to also pan on the Y-Axis of all series."
                                + "<br>The zoom for each Y-Axis can be resetted by Double-Clicking the corresponding axis."
                                + "<br>Ctrl + DoubleClick on any series-Y-axis will reset the Y-axis-zoom for all series of the plot."
                                + "<br>When every series Y-Axis is in Auto-Range-Mode (it automatically fits the displayed data): panning on the Y-Axis is disabled.</li>");
                sb.append("</ul></li>");
                sb.append("</ul></li>");
                sb.append(
                        "<li><b>Exporting</b>: Right click anywhere on the chart to get a context menu that allows you to export the chart image to clipboard or to a file."
                                + "<br>Right click multiple times to remove the crosshair or just use the settings navigation button on the bottom of the chart.</li>");

                if (!getIndicatorSeriesProviders().isEmpty() || getExpressionSeriesProvider() != null) {
                    sb.append(
                            "<li><b>Add Series</b>: Right clicking anywhere also allows you to add indicators or expressions as new series to the chart. "
                                    + "<br>The series settings context menu will also allow you to modify its calculation settings.</li>");
                }

                if (getBookmarkStorage() != null) {
                    sb.append("<li><b>Bookmarks</b>: Right clicking anywhere makes bookmarked ranges visible. "
                            + "They are shown in descending order by last usage."
                            + "<br>The current range is shown in <b>bold</b>. Left click a range to preview the *restoral, middle click to mark it for <strike>removal</strike>."
                            + "<br>Click on cancel to undo. Click anywhere else or one of the explicit restore/remove buttons to confirm your changes.</li>");
                }

                if (chartPanel.getPlotDetailsHelper().getCoordinateListener() != null) {
                    sb.append(
                            "<li><b>Pin Marker</b>: Ctrl-LeftClick anywhere on the chart to put a visual-marker on a timestamp. "
                                    + "<br>Extensive information's (Order's , Bar-Information's, Strategy-Decision's) for this timestamp are displayed in the PointsOfInterestView."
                                    + "<br>The marker can be removed with Ctrl-LeftClick on the already pinned marker or via button on the PointsOfInterestView.");
                }

                sb.append("</ul>");
                sb.append("</html>");
                Dialogs.showMessageDialog(chartPanel, sb.toString(), "Help", Dialogs.PLAIN_MESSAGE);
            }
        });
    }

    public void displayPopupMenu(final MouseEvent e) {
        chartPanel.getChartPanel().setCursor(PlotResizeHelper.DEFAULT_CURSOR);
        this.mousePositionOnPopupMenu = this.chartPanel.getChartPanel().translateScreenToJava2D(e.getPoint());
        this.popupMenu.show(chartPanel, e.getX(), e.getY());
    }

    public void copyToClipboard() {
        final Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final Insets insets = chartPanel.getChartPanel().getInsets();
        final int w = chartPanel.getChartPanel().getWidth() - insets.left - insets.right;
        final int h = chartPanel.getChartPanel().getHeight() - insets.top - insets.bottom;
        final CustomChartTransferable selection = new CustomChartTransferable(chartPanel.getChart(), w, h,
                chartPanel.getChartPanel().getMinimumDrawWidth(), chartPanel.getChartPanel().getMinimumDrawHeight(),
                chartPanel.getChartPanel().getMaximumDrawWidth(), chartPanel.getChartPanel().getMaximumDrawHeight());
        systemClipboard.setContents(selection, null);
    }

    public void saveAsPNG() {
        final JFileChooser fileChooser = new JFileChooser();
        final FileNameExtensionFilter filter = new FileNameExtensionFilter(".png", "png");
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setFileFilter(filter);

        final int option = fileChooser.showSaveDialog(chartPanel);
        if (option == JFileChooser.APPROVE_OPTION) {
            String filename = fileChooser.getSelectedFile().getPath();
            if (!Strings.endsWithIgnoreCase(filename, ".png")) {
                filename = filename + ".png";
            }
            try {
                ChartUtils.saveChartAsPNG(new File(filename), chartPanel.getChart(),
                        chartPanel.getChartPanel().getWidth(), chartPanel.getChartPanel().getHeight());
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void mousePressed(final MouseEvent e) {
        mouseReleased(e);
    }

    public void mouseReleased(final MouseEvent e) {
        if (e.isPopupTrigger()) {
            this.mousePositionOnPopupMenu = this.chartPanel.getChartPanel().translateScreenToJava2D(e.getPoint());
            displayPopupMenu(e);
        }
    }

    public boolean isShowing() {
        return popupMenu.isShowing();
    }

    public void removeInitialSeriesSettings(final String seriesId) {
        seriesId_initialSettings.remove(seriesId);
    }

    public void putIndicatorSeriesProvider(final IIndicatorSeriesProvider indicatorSeriesProvider) {
        indicatorSeriesProviders.put(indicatorSeriesProvider.getName(), indicatorSeriesProvider);
    }

    public Collection<IIndicatorSeriesProvider> getIndicatorSeriesProviders() {
        return indicatorSeriesProviders.values();
    }

    public IIndicatorSeriesProvider getIndicatorSeriesProvider(final String name) {
        return indicatorSeriesProviders.get(name);
    }

    public IExpressionSeriesProvider getExpressionSeriesProvider() {
        return expressionSeriesProvider;
    }

    public void setExpressionSeriesProvider(final IExpressionSeriesProvider expressionSeriesProvider) {
        this.expressionSeriesProvider = expressionSeriesProvider;
    }

    public void setBookmarkStorage(final IBookmarkStorage bookmarkStorage) {
        this.bookmarkStorage = bookmarkStorage;
    }

    public IBookmarkStorage getBookmarkStorage() {
        return bookmarkStorage;
    }

    public Set<String> getRangeAxisIds() {
        final Set<String> rangeAxisIds = new TreeSet<>();
        addRangeAxisId(rangeAxisIds, getPriceInitialSettings().getRangeAxisId());
        for (final SeriesInitialSettings series : seriesId_initialSettings.values()) {
            addRangeAxisId(rangeAxisIds, series.getRangeAxisId());
        }
        final List<XYPlot> plots = chartPanel.getCombinedPlot().getSubplots();
        for (int i = 0; i < plots.size(); i++) {
            final XYPlot plot = plots.get(i);
            for (int datasetIndex = 0; datasetIndex < plot.getDatasetCount(); datasetIndex++) {
                final IPlotSourceDataset dataset = (IPlotSourceDataset) plot.getDataset(datasetIndex);
                if (dataset != null) {
                    addRangeAxisId(rangeAxisIds, dataset.getRangeAxisId());
                }
            }
        }
        return rangeAxisIds;
    }

    private void addRangeAxisId(final Set<String> rangeAxisIds, final String rangeAxisId) {
        if (rangeAxisId != null) {
            rangeAxisIds.add(rangeAxisId);
        }
    }

    public IPlotPopupMenuConfig getPlotPopupMenuConfig() {
        return plotPopupMenuConfig;
    }

    public void setPlotPopupMenuConfig(final IPlotPopupMenuConfig plotPopupMenuConfig) {
        this.plotPopupMenuConfig = plotPopupMenuConfig;
    }
}
