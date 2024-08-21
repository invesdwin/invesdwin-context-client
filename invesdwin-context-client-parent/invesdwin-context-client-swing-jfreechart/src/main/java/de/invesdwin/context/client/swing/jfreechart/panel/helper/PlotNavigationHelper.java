package de.invesdwin.context.client.swing.jfreechart.panel.helper;

import java.awt.Point;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.Timer;

import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYAnnotationEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.basis.CustomCombinedDomainXYPlot;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.icons.PlotIcons;
import de.invesdwin.context.client.swing.jfreechart.plot.XYPlots;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.XYIconAnnotation;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.XYIconAnnotationEntity;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.XYNoteAnnotation;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.XYNoteIconAnnotation;
import de.invesdwin.context.client.swing.jfreechart.plot.axis.Axises;
import de.invesdwin.context.client.swing.jfreechart.plot.renderer.INoteRenderer;
import de.invesdwin.util.collections.factory.ILockCollectionFactory;
import de.invesdwin.util.collections.iterable.buffer.BufferingIterator;
import de.invesdwin.util.collections.list.ListSet;
import de.invesdwin.util.math.Doubles;
import de.invesdwin.util.swing.HiDPI;

@NotThreadSafe
public class PlotNavigationHelper {

    private static final XYAnnotationEntity PLACEHOLDER_ENTITY = new XYAnnotationEntity(
            new Rectangle2D.Double(0, 0, 0, 0), -1, null, null);
    private static final int BUTTON_TIMER_DELAY = 50;
    private static final float INVISIBLE_ALPHA = 0.0f;
    private static final float VISIBLE_ALPHA = 0.6f;
    private static final float HIGHLIGHTED_ALPHA = 1f;

    private final InteractiveChartPanel chartPanel;
    private final XYIconAnnotation panLeft;
    private final XYIconAnnotation zoomOut;
    private final XYIconAnnotation reset;
    private final XYIconAnnotation configure;
    private final XYIconAnnotation zoomIn;
    private final XYIconAnnotation panRight;
    private final XYIconAnnotation panLive;
    private final XYIconAnnotation panLiveBackward;

    private final XYIconAnnotation panLeft_highlighted;
    private final XYIconAnnotation zoomOut_highlighted;
    private final XYIconAnnotation reset_highlighted;
    private final XYIconAnnotation configure_highlighted;
    private final XYIconAnnotation zoomIn_highlighted;
    private final XYIconAnnotation panRight_highlighted;
    private final XYIconAnnotation panLive_highlighted;
    private final XYIconAnnotation panLiveBackward_highlighted;

    private final XYIconAnnotation panLeft_invisible;
    private final XYIconAnnotation zoomOut_invisible;
    private final XYIconAnnotation reset_invisible;
    private final XYIconAnnotation configure_invisible;
    private final XYIconAnnotation zoomIn_invisible;
    private final XYIconAnnotation panRight_invisible;

    private final List<XYIconAnnotation> annotations = new ArrayList<>();

    private final XYIconAnnotation[] visibleCheckAnnotations;

    private XYPlot navShowingOnPlotPlot;
    private XYIconAnnotation navHighlightedAnnotation;
    private final List<Shape> navHighlightingAreas = new ArrayList<>();
    private boolean navHighlighting = false;
    private boolean navVisible = false;
    private Timer navButtonTimer;
    private XYIconAnnotation navButtonTimerAnnotation;

    private XYNoteIconAnnotation noteShowingIconAnnotation;
    private XYPlot noteShowingOnPlot;
    private boolean panLiveVisible = false;
    private boolean panLiveHighlighted = false;
    private boolean panLiveBackwardVisible = false;
    private boolean panLiveBackwardHighlighted = false;

    private final List<XYIconAnnotation> addedAnnotations = new ListSet<XYIconAnnotation>() {
        @Override
        protected Set<XYIconAnnotation> newSet() {
            return ILockCollectionFactory.getInstance(false).newIdentitySet();
        }
    };

    public PlotNavigationHelper(final InteractiveChartPanel chartPanel) {
        this.chartPanel = chartPanel;
        this.panLeft = newIcon(PlotIcons.PAN_LEFT, -60 - 15, VISIBLE_ALPHA);
        this.zoomOut = newIcon(PlotIcons.ZOOM_OUT, -30 - 15, VISIBLE_ALPHA);
        this.reset = newIcon(PlotIcons.RESET, -15, VISIBLE_ALPHA);
        this.configure = newIcon(PlotIcons.CONFIGURE, +15, VISIBLE_ALPHA);
        this.zoomIn = newIcon(PlotIcons.ZOOM_IN, +30 + 15, VISIBLE_ALPHA);
        this.panRight = newIcon(PlotIcons.PAN_RIGHT, +60 + 15, VISIBLE_ALPHA);

        this.panLeft_highlighted = newIcon(PlotIcons.PAN_LEFT, -60 - 15, HIGHLIGHTED_ALPHA);
        this.zoomOut_highlighted = newIcon(PlotIcons.ZOOM_OUT, -30 - 15, HIGHLIGHTED_ALPHA);
        this.reset_highlighted = newIcon(PlotIcons.RESET, -15, HIGHLIGHTED_ALPHA);
        this.configure_highlighted = newIcon(PlotIcons.CONFIGURE, +15, HIGHLIGHTED_ALPHA);
        this.zoomIn_highlighted = newIcon(PlotIcons.ZOOM_IN, +30 + 15, HIGHLIGHTED_ALPHA);
        this.panRight_highlighted = newIcon(PlotIcons.PAN_RIGHT, +60 + 15, HIGHLIGHTED_ALPHA);

        this.panLeft_invisible = newIcon(PlotIcons.PAN_LEFT, -60 - 15, INVISIBLE_ALPHA);
        this.zoomOut_invisible = newIcon(PlotIcons.ZOOM_OUT, -30 - 15, INVISIBLE_ALPHA);
        this.reset_invisible = newIcon(PlotIcons.RESET, -15, INVISIBLE_ALPHA);
        this.configure_invisible = newIcon(PlotIcons.RESET, +15, INVISIBLE_ALPHA);
        this.zoomIn_invisible = newIcon(PlotIcons.ZOOM_IN, +30 + 15, INVISIBLE_ALPHA);
        this.panRight_invisible = newIcon(PlotIcons.PAN_RIGHT, +60 + 15, INVISIBLE_ALPHA);

        this.visibleCheckAnnotations = new XYIconAnnotation[] { panLeft, panLeft_highlighted, panLeft_invisible,
                panRight, panRight_highlighted, panRight_invisible };

        this.panLive = newIcon(PlotIcons.PAN_LIVE, 0.97D, 0.05D, 0, VISIBLE_ALPHA);
        this.panLive_highlighted = newIcon(PlotIcons.PAN_LIVE, 0.97D, 0.05D, 0, HIGHLIGHTED_ALPHA);

        this.panLiveBackward = newIcon(PlotIcons.PAN_LIVE_BACKWARD, 0.97D, 0.05D, 0, VISIBLE_ALPHA);
        this.panLiveBackward_highlighted = newIcon(PlotIcons.PAN_LIVE_BACKWARD, 0.97D, 0.05D, 0, HIGHLIGHTED_ALPHA);
    }

    private XYIconAnnotation newIcon(final PlotIcons icon, final int xModification, final float alpha) {
        final XYIconAnnotation annotation = new XYIconAnnotation(0.5D, 0.05D, icon.newIcon(HiDPI.scale(24), alpha)) {
            @Override
            protected double modifyYInput(final double y) {
                return Doubles.min(y * chartPanel.getCombinedPlot().getSubplots().size(), 0.5D);
            }

            @Override
            protected float modifyXOutput(final float x) {
                return x + HiDPI.scale(xModification);
            }

        };
        annotations.add(annotation);
        return annotation;
    }

    private XYIconAnnotation newIcon(final PlotIcons icon, final double x, final double y, final int xModification,
            final float alpha) {
        final XYIconAnnotation annotation = new XYIconAnnotation(x, y, icon.newIcon(HiDPI.scale(24), alpha)) {
            @Override
            protected double modifyYInput(final double y) {
                return Doubles.min(y * chartPanel.getCombinedPlot().getSubplots().size(), 0.5D);
            }

            @Override
            protected float modifyXOutput(final float x) {
                return x + HiDPI.scale(xModification);
            }
        };
        annotations.add(annotation);
        return annotation;
    }

    public void mouseDragged(final MouseEvent e) {
        //Don't show the navbar when we are panning a plot or MouseDragZoom on an axis
        if (chartPanel.getPlotPanHelper().isPanning() || chartPanel.getPlotZoomHelper().isMouseDragZooming()) {
            return;
        }

        final int mouseX = e.getX();
        final int mouseY = e.getY();
        unhighlight(mouseX, mouseY);
    }

    public void mouseMoved(final MouseEvent e) {
        final int mouseX = e.getX();
        final int mouseY = e.getY();
        final XYNoteIconAnnotation highlightedNoteIconAnnotation = findHighlightedNoteIconAnnotation(mouseX, mouseY);

        if (highlightedNoteIconAnnotation != null && !chartPanel.isDragging()) {
            final XYNoteIconAnnotation noteShowingIconAnnotationCopy = noteShowingIconAnnotation;
            if (noteShowingIconAnnotationCopy == null
                    || noteShowingIconAnnotationCopy != highlightedNoteIconAnnotation) {
                mouseExited();
                final CustomCombinedDomainXYPlot combinedPlot = chartPanel.getCombinedPlot();
                final List<XYPlot> subplots = combinedPlot.getSubplots();
                final int subplotIndex = combinedPlot.getSubplotIndex(mouseX, mouseY);
                noteShowingOnPlot = subplots.get(subplotIndex);
                noteShowingOnPlot.addAnnotation(highlightedNoteIconAnnotation.getNoteAnnotation());
                noteShowingIconAnnotation = highlightedNoteIconAnnotation;
            }

            chartPanel.getChartPanel().setCursor(PlotResizeHelper.DEFAULT_CURSOR);
        } else {
            unhighlight(mouseX, mouseY);
        }
    }

    private void unhighlight(final int mouseX, final int mouseY) {
        hideNote();
        updateNavigationVisibility(mouseX, mouseY);
    }

    private XYNoteIconAnnotation findHighlightedNoteIconAnnotation(final int mouseX, final int mouseY) {
        final CustomCombinedDomainXYPlot combinedPlot = chartPanel.getCombinedPlot();
        final int subplotIndex = combinedPlot.getSubplotIndex(mouseX, mouseY);
        if (subplotIndex == -1) {
            return null;
        }
        final List<XYPlot> subplots = combinedPlot.getSubplots();
        if (subplotIndex >= subplots.size()) {
            return null;
        }
        final Point scaledMouse = new Point(mouseX, mouseY);
        final Point2D unscaledMouse = chartPanel.getChartPanel().translateScreenToJava2D(scaledMouse);
        final XYPlot plot = subplots.get(subplotIndex);
        for (int i = 0; i < plot.getDatasetCount(); i++) {
            final XYItemRenderer renderer = plot.getRenderer(i);
            if (renderer instanceof INoteRenderer) {
                final INoteRenderer cRenderer = (INoteRenderer) renderer;
                for (final XYNoteIconAnnotation noteIcon : cRenderer.getVisibleNoteIcons()) {
                    final XYAnnotationEntity entity = noteIcon.getEntity();
                    if (entity != null && entity.getArea().contains(unscaledMouse)) {
                        return noteIcon;
                    }
                }
            }
        }
        return null;
    }

    private void updateNavigationVisibility(final int mouseX, final int mouseY) {
        final ChartEntity entityForPoint = chartPanel.getChartPanel().getEntityForPoint(mouseX, mouseY);
        final CustomCombinedDomainXYPlot combinedPlot = chartPanel.getCombinedPlot();
        final List<XYPlot> subplots = combinedPlot.getSubplots();
        final int lastSubPlotIndex = subplots.size() - 1;
        final XYPlot lastSubPlot = subplots.get(lastSubPlotIndex);
        final int subplotIndex = combinedPlot.getSubplotIndex(mouseX, mouseY);

        final boolean hasDataset = XYPlots.hasDataset(subplots);

        if (subplotIndex == lastSubPlotIndex) {
            boolean changed = false;
            if (lastSubPlot != navShowingOnPlotPlot) {
                mouseExited();
                navShowingOnPlotPlot = lastSubPlot;
                changed = addAnnotations(false, null, false);
            }
            XYIconAnnotation newHighlightedAnnotation = null;
            if (entityForPoint instanceof XYIconAnnotationEntity) {
                final XYIconAnnotationEntity l = (XYIconAnnotationEntity) entityForPoint;
                newHighlightedAnnotation = getIconAnnotation(l);
            }
            final boolean newVisible = findVisibleEntity(mouseX, mouseY);
            this.navHighlighting = determineHighlighting(mouseX, mouseY);

            if (newHighlightedAnnotation != this.navHighlightedAnnotation || navVisible != newVisible) {
                changed |= addAnnotations(newVisible, newHighlightedAnnotation, hasDataset);
                navHighlightingAreas.clear();
                this.navHighlightedAnnotation = newHighlightedAnnotation;
                this.navVisible = newVisible;
                if (navButtonTimerAnnotation != null && navHighlightedAnnotation != navButtonTimerAnnotation) {
                    stopButtonTimer();
                }
            }
            if (this.navHighlighting) {
                chartPanel.getChartPanel().setCursor(PlotResizeHelper.DEFAULT_CURSOR);
            }
            if (changed) {
                updateAnnotations(navShowingOnPlotPlot);
            }
        } else {
            mouseExited();
        }
    }

    private boolean findVisibleEntity(final int mouseX, final int mouseY) {
        final Rectangle2D.Double scaled = new Rectangle2D.Double(mouseX - 150, mouseY - 100, 300, 200);
        final Rectangle2D area = chartPanel.getChartPanel().unscale(scaled);
        for (int i = 0; i < visibleCheckAnnotations.length; i++) {
            final XYIconAnnotation annotation = visibleCheckAnnotations[i];
            final XYAnnotationEntity entity = annotation.getEntity();
            if (entity != null && entity != PLACEHOLDER_ENTITY) {
                final Rectangle2D entityArea = (Rectangle2D) entity.getArea();
                if (area.intersects(entityArea)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean determineHighlighting(final int mouseX, final int mouseY) {
        final List<Shape> areas = getNavHighlightingAreas();
        if (areas.isEmpty()) {
            return false;
        }

        for (int i = 0; i < areas.size(); i++) {
            if (areas.get(i).contains(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    private List<Shape> getNavHighlightingAreas() {
        if (navHighlightingAreas.isEmpty()) {
            //NavBar/NavPanel
            Double minX = null;
            Double minY = null;
            Double maxX = null;
            Double maxY = null;

            for (int i = 0; i < visibleCheckAnnotations.length; i++) {
                final XYIconAnnotation annotation = visibleCheckAnnotations[i];
                final XYAnnotationEntity entity = annotation.getEntity();
                if (entity != null && entity != PLACEHOLDER_ENTITY) {
                    final Rectangle2D entityArea = (Rectangle2D) entity.getArea();
                    minX = Doubles.min(minX, entityArea.getX());
                    minY = Doubles.min(minY, entityArea.getY());
                    maxX = Doubles.max(maxX, entityArea.getMaxX());
                    maxY = Doubles.max(maxY, entityArea.getMaxY());
                }
            }
            if (minY != null) {
                final Rectangle2D.Double unscaled = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
                final Rectangle2D scaled = chartPanel.getChartPanel().scale(unscaled);
                navHighlightingAreas.add(scaled);
            }

            //PanLive-Icon
            final XYAnnotationEntity panLiveEntity = panLive.getEntity();
            if (panLiveEntity != null && panLiveEntity != PLACEHOLDER_ENTITY && panLiveVisible) {
                navHighlightingAreas.add(panLiveEntity.getArea());
            }
            final XYAnnotationEntity panLiveBackwardEntity = panLiveBackward.getEntity();
            if (panLiveBackwardEntity != null && panLiveBackwardEntity != PLACEHOLDER_ENTITY
                    && panLiveBackwardVisible) {
                navHighlightingAreas.add(panLiveBackwardEntity.getArea());
            }
        }
        return navHighlightingAreas;
    }

    private XYIconAnnotation getIconAnnotation(final XYIconAnnotationEntity l) {
        if (l == null) {
            return null;
        }
        final XYIconAnnotation highlighted;
        final XYIconAnnotation io = l.getIconAnnotation();
        if (io == panLeft || io == panLeft_highlighted) {
            highlighted = panLeft;
        } else if (io == zoomOut || io == zoomOut_highlighted) {
            highlighted = zoomOut;
        } else if (io == reset || io == reset_highlighted) {
            highlighted = reset;
        } else if (io == configure || io == configure_highlighted) {
            highlighted = configure;
        } else if (io == zoomIn || io == zoomIn_highlighted) {
            highlighted = zoomIn;
        } else if (io == panRight || io == panRight_highlighted) {
            highlighted = panRight;
        } else if (io == panLive || io == panLive_highlighted) {
            highlighted = panLive;
        } else if (io == panLiveBackward || io == panLiveBackward_highlighted) {
            highlighted = panLiveBackward;
        } else {
            highlighted = null;
        }
        return highlighted;
    }

    private void updateAnnotations(final XYPlot plot) {
        boolean changed = false;
        final int lastIndex = annotations.size() - 1;
        for (int i = 0; i <= lastIndex; i++) {
            final XYIconAnnotation annotation = annotations.get(i);
            if (addedAnnotations.contains(annotation)) {
                if (annotation.getEntity() == null) {
                    annotation.setEntity(PLACEHOLDER_ENTITY);
                    plot.addAnnotation(annotation, false);
                    changed = true;
                }
            } else {
                if (annotation.getEntity() != null) {
                    annotation.setEntity(null);
                    plot.removeAnnotation(annotation, false);
                    changed = true;
                }
            }
        }
        if (changed) {
            chartPanel.getChart().fireChartChanged();
        }
    }

    private boolean addAnnotations(final boolean visible, final XYIconAnnotation highlighted,
            final boolean hasDataset) {
        //NavBarAnnotations
        boolean changed = addAnnotation(visible, highlighted, hasDataset, false, panLeft_invisible, panLeft,
                panLeft_highlighted);
        changed |= addAnnotation(visible, highlighted, hasDataset, false, zoomOut_invisible, zoomOut,
                zoomOut_highlighted);
        changed |= addAnnotation(visible, highlighted, hasDataset, true, reset_invisible, reset, reset_highlighted);
        changed |= addAnnotation(visible, highlighted, hasDataset, true, configure_invisible, configure,
                configure_highlighted);
        changed |= addAnnotation(visible, highlighted, hasDataset, false, zoomIn_invisible, zoomIn, zoomIn_highlighted);
        changed |= addAnnotation(visible, highlighted, hasDataset, false, panRight_invisible, panRight,
                panRight_highlighted);

        //Other annotations
        changed |= addFreeAnnotations(highlighted); //TODO: funktioniert noch nicht wieder
        return changed;
    }

    private boolean addAnnotation(final boolean visible, final XYIconAnnotation highlighted, final boolean hasDataset,
            final boolean allowMasterDatasetEmpty, final XYIconAnnotation invisibleIcon,
            final XYIconAnnotation visibleIcon, final XYIconAnnotation highlightedIcon) {
        if (visible) {
            if (shouldShowAnnotation(hasDataset, allowMasterDatasetEmpty)) {
                if (highlighted == visibleIcon) {
                    if (addedAnnotations.add(highlightedIcon)) {
                        addedAnnotations.remove(visibleIcon);
                        addedAnnotations.remove(invisibleIcon);
                        return true;
                    }
                } else {
                    if (addedAnnotations.add(visibleIcon)) {
                        addedAnnotations.remove(highlightedIcon);
                        addedAnnotations.remove(invisibleIcon);
                        return true;
                    }
                }
            } else {
                if (addedAnnotations.add(invisibleIcon)) {
                    addedAnnotations.remove(visibleIcon);
                    addedAnnotations.remove(highlightedIcon);
                    return true;
                }
            }
        } else {
            if (addedAnnotations.add(invisibleIcon)) {
                addedAnnotations.remove(visibleIcon);
                addedAnnotations.remove(highlightedIcon);
                return true;
            }
        }
        return false;
    }

    private boolean shouldShowAnnotation(final boolean hasDataset, final boolean allowMasterDatasetEmpty) {
        final boolean isMasterDatasetEmpty = chartPanel.getMasterDataset().getData().isEmpty();
        if (isMasterDatasetEmpty) {
            return allowMasterDatasetEmpty;
        }
        return hasDataset;
    }

    private boolean addFreeAnnotations(final XYIconAnnotation highlighted) {
        if (highlighted == panLive && panLiveVisible) {
            if (addedAnnotations.add(panLive_highlighted)) {
                addedAnnotations.remove(panLive);
                this.panLiveHighlighted = true;
                return true;
            }
        } else if (highlighted != panLive && panLiveVisible) {
            if (addedAnnotations.add(panLive)) {
                addedAnnotations.remove(panLive_highlighted);
                this.panLiveHighlighted = false;
                return true;
            }
        } else if (highlighted == panLiveBackward && panLiveBackwardVisible) {
            if (addedAnnotations.add(panLiveBackward_highlighted)) {
                addedAnnotations.remove(panLiveBackward);
                this.panLiveBackwardHighlighted = true;
                return true;
            }
        } else if (highlighted != panLiveBackward && panLiveBackwardVisible) {
            if (addedAnnotations.add(panLiveBackward)) {
                addedAnnotations.remove(panLiveBackward_highlighted);
                this.panLiveBackwardHighlighted = false;
                return true;
            }
        }
        return false;
    }

    public void mouseExited() {
        if (navShowingOnPlotPlot != null) {
            addedAnnotations.clear();
            updateAnnotations(navShowingOnPlotPlot);
            navShowingOnPlotPlot = null;
        }
        navHighlighting = false;
        stopButtonTimer();
        hideNote();
    }

    private void hideNote() {
        final XYPlot noteShowingOnPlotCopy = noteShowingOnPlot;
        if (noteShowingOnPlotCopy != null) {
            BufferingIterator<XYAnnotation> annotationsToRemove = null;
            final List<XYAnnotation> existingAnnotationsCopy = XYPlots.getAnnotations(noteShowingOnPlotCopy);
            for (int i = 0; i < existingAnnotationsCopy.size(); i++) {
                final XYAnnotation annotation = existingAnnotationsCopy.get(i);
                if (annotation instanceof XYNoteAnnotation) {
                    if (annotationsToRemove == null) {
                        annotationsToRemove = new BufferingIterator<>();
                    }
                    annotationsToRemove.add(annotation);
                }
            }
            if (annotationsToRemove != null) {
                try {
                    while (true) {
                        final XYAnnotation annotationToRemove = annotationsToRemove.next();
                        noteShowingOnPlotCopy.removeAnnotation(annotationToRemove);
                    }
                } catch (final NoSuchElementException e) {
                    //end reached
                }
            }
            noteShowingIconAnnotation = null;
            noteShowingOnPlot = null;
        }
    }

    public void mousePressed(final MouseEvent e) {
        mouseMoved(e); //update when configuration popup becomes invisible
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        final int mouseX = e.getX();
        final int mouseY = e.getY();
        final ChartEntity entityForPoint = chartPanel.getChartPanel().getEntityForPoint(mouseX, mouseY);
        if (entityForPoint instanceof XYIconAnnotationEntity) {
            final XYIconAnnotationEntity l = (XYIconAnnotationEntity) entityForPoint;
            final XYIconAnnotation annotation = getIconAnnotation(l);
            final ActionListener action;
            if (annotation == panLeft) {
                action = new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        chartPanel.getPlotPanHelper().panLeft();
                    }
                };
            } else if (annotation == panRight) {
                action = new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        chartPanel.getPlotPanHelper().panRight();
                    }
                };
            } else if (annotation == zoomIn) {
                action = new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        chartPanel.getPlotZoomHelper().zoomIn();
                    }
                };
            } else if (annotation == zoomOut) {
                action = new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        chartPanel.getPlotZoomHelper().zoomOut();
                    }
                };
            } else {
                action = null;
            }
            if (action != null) {
                startButtonTimer(annotation, action);
            }
        }
    }

    private void startButtonTimer(final XYIconAnnotation annotation, final ActionListener action) {
        /*
         * It happened that we initialized a timer here when the previous one wasn't closed yet. We then lost the
         * reference for a timer which we never closed again and ran into an endless-loop. Therefore we check for
         * already running timers and eventually stop that one.
         */
        stopButtonTimer();

        navButtonTimer = new Timer(BUTTON_TIMER_DELAY, action);
        navButtonTimer.setInitialDelay(0);
        navButtonTimer.start();
        navButtonTimerAnnotation = annotation;
    }

    public void mouseReleased(final MouseEvent e) {
        mouseMoved(e); //update when configuration popup becomes invisible

        if (e.getButton() != MouseEvent.BUTTON1 && e.getButton() != MouseEvent.BUTTON2 || (chartPanel.isDragging())) {
            return;
        }
        if (stopButtonTimer()) {
            return;
        }
        final int mouseX = e.getX();
        final int mouseY = e.getY();
        final ChartEntity entityForPoint = chartPanel.getChartPanel().getEntityForPoint(mouseX, mouseY);
        if (entityForPoint instanceof XYIconAnnotationEntity) {
            final XYIconAnnotationEntity l = (XYIconAnnotationEntity) entityForPoint;
            final XYIconAnnotation iconAnnotation = getIconAnnotation(l);
            if (iconAnnotation == reset) {
                final int initialVisibleItemCount = chartPanel.getInitialVisibleItemCount();
                if (e.isControlDown() || e.isShiftDown() || e.getButton() == MouseEvent.BUTTON2) {
                    if (e.isControlDown() && e.isShiftDown()) {
                        //only reload data, no reset range to the right
                        chartPanel.reloadData();
                    } else {
                        chartPanel.resetRange(initialVisibleItemCount, chartPanel.getDefaultTrailingRangeGapRate(),
                                () -> chartPanel.reloadData());
                        chartPanel.updateUserGapRate();
                    }
                } else {
                    chartPanel.resetRange(initialVisibleItemCount, chartPanel.getDefaultTrailingRangeGapRate());
                    chartPanel.updateUserGapRate();
                }
                Axises.resetAllAutoRanges(chartPanel);
                XYPlots.resetAllRangePannables(chartPanel);
            } else if (iconAnnotation == configure && e.getButton() == MouseEvent.BUTTON1) {
                chartPanel.getPlotConfigurationHelper().displayPopupMenu(e);
            } else if (iconAnnotation == panLive || iconAnnotation == panLiveBackward) {
                chartPanel.getPlotPanHelper().panLive(e);
            }
        }
    }

    private boolean stopButtonTimer() {
        if (navButtonTimer != null) {
            navButtonTimer.stop();
            navButtonTimer = null;
            navButtonTimerAnnotation = null;
            return true;
        }
        return false;
    }

    public boolean isHighlighting() {
        return navHighlighting || noteShowingOnPlot != null || panLiveHighlighted || panLiveBackwardHighlighted;
    }

    public void showPanLiveIcon(final boolean backward) {
        final XYPlot lastSubPlot = getLastSubplot();
        if (backward) {
            panLiveBackwardVisible = true;
            if (panLiveBackwardHighlighted) {
                if (addedAnnotations.add(panLiveBackward_highlighted)) {
                    addedAnnotations.remove(panLiveBackward);
                    updateAnnotations(lastSubPlot);
                }
            } else {
                if (addedAnnotations.add(panLiveBackward)) {
                    addedAnnotations.remove(panLiveBackward_highlighted);
                    updateAnnotations(lastSubPlot);
                }
            }
        } else {
            panLiveVisible = true;
            if (panLiveHighlighted) {
                if (addedAnnotations.add(panLive_highlighted)) {
                    addedAnnotations.remove(panLive);
                    updateAnnotations(lastSubPlot);
                }
            } else {
                if (addedAnnotations.add(panLive)) {
                    addedAnnotations.remove(panLive_highlighted);
                    updateAnnotations(lastSubPlot);
                }
            }
        }

    }

    public void hidePanLiveIcon() {
        panLiveVisible = false;
        panLiveHighlighted = false;
        panLiveBackwardVisible = false;
        panLiveBackwardHighlighted = false;
        boolean changed = addedAnnotations.remove(panLive);
        changed |= addedAnnotations.remove(panLive_highlighted);
        changed |= addedAnnotations.remove(panLiveBackward);
        changed |= addedAnnotations.remove(panLiveBackward_highlighted);
        if (changed) {
            final XYPlot lastSubPlot = getLastSubplot();
            updateAnnotations(lastSubPlot);
        }
        navHighlightingAreas.clear();
    }

    private XYPlot getLastSubplot() {
        final CustomCombinedDomainXYPlot combinedPlot = chartPanel.getCombinedPlot();
        final List<XYPlot> subplots = combinedPlot.getSubplots();
        final int lastSubPlotIndex = subplots.size() - 1;
        final XYPlot lastSubPlot = subplots.get(lastSubPlotIndex);
        return lastSubPlot;
    }

    public XYNoteIconAnnotation getNoteShowingIconAnnotation() {
        return noteShowingIconAnnotation;
    }
}
