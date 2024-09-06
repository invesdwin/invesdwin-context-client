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
import java.util.function.BooleanSupplier;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.Timer;

import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYAnnotationEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;

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
import de.invesdwin.context.jfreechart.dataset.TimeRangedOHLCDataItem;
import de.invesdwin.util.collections.factory.ILockCollectionFactory;
import de.invesdwin.util.collections.iterable.buffer.BufferingIterator;
import de.invesdwin.util.collections.list.ListSet;
import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.math.Doubles;
import de.invesdwin.util.swing.Components;
import de.invesdwin.util.swing.HiDPI;

@NotThreadSafe
public class PlotNavigationHelper {

    private static final XYAnnotationEntity PLACEHOLDER_ENTITY = new XYAnnotationEntity(
            new Rectangle2D.Double(0, 0, 0, 0), -1, null, null);
    private static final int BUTTON_TIMER_DELAY = 50;
    private static final float INVISIBLE_ALPHA = 0.0f;
    private static final float DISABLED_ALPHA = 0.2f;
    private static final float VISIBLE_ALPHA = 0.6f;
    private static final float HIGHLIGHTED_ALPHA = 1f;

    private final InteractiveChartPanel chartPanel;
    private final StatefulXYIconAnnotation panLeft;
    private final StatefulXYIconAnnotation zoomOut;
    private final StatefulXYIconAnnotation reset;
    private final StatefulXYIconAnnotation configure;
    private final StatefulXYIconAnnotation zoomIn;
    private final StatefulXYIconAnnotation panRight;

    private final StatefulXYIconAnnotation panLiveForward;
    private final StatefulXYIconAnnotation panLiveBackward;

    private final StatefulXYIconAnnotation[] navVisibleCheckAnnotations;
    private final StatefulXYIconAnnotation[] navAnnotations;
    private final StatefulXYIconAnnotation[] allAnnotations;

    private XYPlot navShowingOnPlotPlot;
    private StatefulXYIconAnnotation highlightedAnnotation;
    private final List<Shape> highlightingAreas = new ArrayList<>();
    private boolean highlighting = false;
    private boolean navigationVisible = false;
    private Timer buttonTimer;
    private StatefulXYIconAnnotation buttonTimerAnnotation;

    private XYNoteIconAnnotation noteShowingIconAnnotation;
    private XYPlot noteShowingOnPlot;
    private PanLiveIconState panLiveIconState = PanLiveIconState.Invisible;
    private PanLiveIconState renderedPanLiveIconState = PanLiveIconState.Invisible;

    private final List<XYIconAnnotation> allAnnotationIcons = new ArrayList<>();
    private final List<XYIconAnnotation> addedAnnotationIcons = new ListSet<XYIconAnnotation>() {
        @Override
        protected Set<XYIconAnnotation> newSet() {
            return ILockCollectionFactory.getInstance(false).newIdentitySet();
        }
    };

    public PlotNavigationHelper(final InteractiveChartPanel chartPanel) {
        this.chartPanel = chartPanel;
        this.panLeft = newIcons(PlotIcons.PAN_LEFT, -60 - 15).setDisabledCheck(new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                final Range domainAxisRange = chartPanel.getDomainAxis().getRange();
                final double domainAxisLength = domainAxisRange.getLength();
                final List<? extends TimeRangedOHLCDataItem> data = chartPanel.getMasterDataset().getData();
                final int allowedGap = chartPanel.getAllowedMaximumRangeGap(domainAxisLength);
                final double minLowerBound = chartPanel.getPlotZoomHelper().getMinLowerBoundWithGap(data, allowedGap);

                return domainAxisRange.getLowerBound() == minLowerBound;
            }
        });
        this.zoomOut = newIcons(PlotIcons.ZOOM_OUT, -30 - 15).setDisabledCheck(new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                return chartPanel.getPlotZoomHelper().isMaxZoomOut(chartPanel.getDomainAxis().getRange());
            }
        });
        this.reset = newIcons(PlotIcons.RESET, -15).setAllowMasterDatasetEmpty(true);
        this.configure = newIcons(PlotIcons.CONFIGURE, +15).setAllowMasterDatasetEmpty(true);
        this.zoomIn = newIcons(PlotIcons.ZOOM_IN, +30 + 15).setDisabledCheck(new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                return chartPanel.getDomainAxis().getRange().getLength() <= PlotZoomHelper.MIN_ZOOM_ITEM_COUNT;
            }
        });
        this.panRight = newIcons(PlotIcons.PAN_RIGHT, +60 + 15).setDisabledCheck(new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                final Range domainAxisRange = chartPanel.getDomainAxis().getRange();
                final double domainAxisLength = domainAxisRange.getLength();
                final List<? extends TimeRangedOHLCDataItem> data = chartPanel.getMasterDataset().getData();
                final int allowedGap = chartPanel.getAllowedMaximumRangeGap(domainAxisLength);
                final double maxUpperBound = chartPanel.getPlotZoomHelper().getMaxUpperBoundWithGap(data, allowedGap);

                return domainAxisRange.getUpperBound() == maxUpperBound;
            }
        });

        this.navVisibleCheckAnnotations = new StatefulXYIconAnnotation[] { panLeft, panRight };

        this.panLiveForward = newIcons(PlotIcons.PAN_LIVE_FORWARD, 0, 0.97D, 0.05D);
        this.panLiveBackward = newIcons(PlotIcons.PAN_LIVE_BACKWARD, 0, 0.97D, 0.05D);

        this.navAnnotations = new StatefulXYIconAnnotation[] { panLeft, zoomOut, reset, configure, zoomIn, panRight };
        this.allAnnotations = new StatefulXYIconAnnotation[] { panLeft, zoomOut, reset, configure, zoomIn, panRight,
                panLiveForward, panLiveBackward };
    }

    private StatefulXYIconAnnotation newIcons(final PlotIcons icon, final int xModification) {
        return newIcons(icon, xModification, 0.5D, 0.05D);
    }

    private StatefulXYIconAnnotation newIcons(final PlotIcons icon, final int xModification, final double x,
            final double y) {
        final XYIconAnnotation invisible = newIcon(icon, xModification, x, y, INVISIBLE_ALPHA);
        final XYIconAnnotation disabled = newIcon(icon, xModification, x, y, DISABLED_ALPHA);
        final XYIconAnnotation visible = newIcon(icon, xModification, x, y, VISIBLE_ALPHA);
        final XYIconAnnotation highlighted = newIcon(icon, xModification, x, y, HIGHLIGHTED_ALPHA);
        return new StatefulXYIconAnnotation(chartPanel, invisible, disabled, visible, highlighted);
    }

    private XYIconAnnotation newIcon(final PlotIcons icon, final int xModification, final double x, final double y,
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
        allAnnotationIcons.add(annotation);
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
        updateNavigation(mouseX, mouseY);
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

    public void updateNavigation() {
        final Point mouseLocation = Components.getMouseLocationOnComponent(chartPanel.getChartPanel());
        final int mouseX;
        final int mouseY;
        if (mouseLocation != null) {
            mouseX = mouseLocation.x;
            mouseY = mouseLocation.y;
        } else {
            mouseX = -1;
            mouseY = -1;
        }
        updateNavigation(mouseX, mouseY);
    }

    private void updateNavigation(final int mouseX, final int mouseY) {
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
                updateNavAnnotationsState(false, null, false);
                changed = addAnnotations(null);
            }
            StatefulXYIconAnnotation newHighlightedAnnotation = null;
            if (entityForPoint instanceof XYIconAnnotationEntity) {
                final XYIconAnnotationEntity l = (XYIconAnnotationEntity) entityForPoint;
                newHighlightedAnnotation = getIconAnnotation(l);
            }
            final boolean newNavigationVisible = determineNavigationVisible(mouseX, mouseY);
            this.highlighting = determineHighlighting(mouseX, mouseY);

            final boolean navStateChanged = updateNavAnnotationsState(hasDataset, newHighlightedAnnotation,
                    newNavigationVisible);
            if (newHighlightedAnnotation != this.highlightedAnnotation || navigationVisible != newNavigationVisible
                    || panLiveIconState != renderedPanLiveIconState || navStateChanged) {
                changed |= addAnnotations(newHighlightedAnnotation);
                highlightingAreas.clear();
                this.highlightedAnnotation = newHighlightedAnnotation;
                this.navigationVisible = newNavigationVisible;
                if (buttonTimerAnnotation != null && highlightedAnnotation != buttonTimerAnnotation) {
                    stopButtonTimer();
                }
            }
            if (this.highlighting) {
                chartPanel.getChartPanel().setCursor(PlotResizeHelper.DEFAULT_CURSOR);
            }
            if (changed) {
                updateAnnotations(navShowingOnPlotPlot);
            }
        } else {
            mouseExited();
        }
    }

    protected boolean updateNavAnnotationsState(final boolean hasDataset,
            final StatefulXYIconAnnotation highlightedAnnotation, final boolean newNavigationVisible) {
        boolean stateChanged = false;
        for (int i = 0; i < navAnnotations.length; i++) {
            final StatefulXYIconAnnotation annotation = navAnnotations[i];
            if (annotation.updateState(newNavigationVisible, hasDataset, highlightedAnnotation)) {
                stateChanged = true;
            }
        }
        return stateChanged;
    }

    private boolean determineNavigationVisible(final int mouseX, final int mouseY) {
        if (mouseX < 0 || mouseY < 0) {
            return false;
        }
        final Rectangle2D.Double scaled = new Rectangle2D.Double(mouseX - 150, mouseY - 100, 300, 200);
        final Rectangle2D area = chartPanel.getChartPanel().unscale(scaled);
        for (int i = 0; i < navVisibleCheckAnnotations.length; i++) {
            final XYIconAnnotation[] annotations = navVisibleCheckAnnotations[i].asArray();
            for (int j = 0; j < annotations.length; j++) {
                final XYIconAnnotation annotation = annotations[j];
                final XYAnnotationEntity entity = annotation.getEntity();
                if (entity != null && entity != PLACEHOLDER_ENTITY) {
                    final Rectangle2D entityArea = (Rectangle2D) entity.getArea();
                    if (area.intersects(entityArea)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean determineHighlighting(final int mouseX, final int mouseY) {
        if (mouseX < 0 || mouseY < 0) {
            return false;
        }
        final List<Shape> areas = getHighlightingAreas();
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

    private List<Shape> getHighlightingAreas() {
        if (highlightingAreas.isEmpty()) {
            //NavBar/NavPanel
            Double minX = null;
            Double minY = null;
            Double maxX = null;
            Double maxY = null;

            for (int i = 0; i < navVisibleCheckAnnotations.length; i++) {
                final XYIconAnnotation[] annotations = navVisibleCheckAnnotations[i].asArray();
                for (int j = 0; j < annotations.length; j++) {
                    final XYIconAnnotation annotation = annotations[j];
                    final XYAnnotationEntity entity = annotation.getEntity();
                    if (entity != null && entity != PLACEHOLDER_ENTITY) {
                        final Rectangle2D entityArea = (Rectangle2D) entity.getArea();
                        minX = Doubles.min(minX, entityArea.getX());
                        minY = Doubles.min(minY, entityArea.getY());
                        maxX = Doubles.max(maxX, entityArea.getMaxX());
                        maxY = Doubles.max(maxY, entityArea.getMaxY());
                    }
                }
            }
            if (minY != null) {
                final Rectangle2D.Double unscaled = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
                final Rectangle2D scaled = chartPanel.getChartPanel().scale(unscaled);
                highlightingAreas.add(scaled);
            }

            //PanLive-Icon
            final XYIconAnnotation panLiveIcon = getPanLiveIconAnnotation();
            if (panLiveIcon != null) {
                final XYAnnotationEntity panLiveIconEntity = panLiveIcon.getEntity();
                if (panLiveIconEntity != null && panLiveIconEntity != PLACEHOLDER_ENTITY) {
                    highlightingAreas.add(panLiveIconEntity.getArea());
                }
            }
        }
        return highlightingAreas;
    }

    private XYIconAnnotation getPanLiveIconAnnotation() {
        final PanLiveIconState state = panLiveIconState;
        switch (state) {
        case Invisible:
            return null;
        case PanLiveForwardVisible:
            return panLiveForward.getVisible();
        case PanLiveForwardHighlighted:
            return panLiveForward.getHighlighted();
        case PanLiveBackwardVisible:
            return panLiveBackward.getVisible();
        case PanLiveBackwardHighlighted:
            return panLiveBackward.getHighlighted();
        default:
            throw UnknownArgumentException.newInstance(PanLiveIconState.class, state);
        }
    }

    private StatefulXYIconAnnotation getIconAnnotation(final XYIconAnnotationEntity entity) {
        if (entity == null) {
            return null;
        }
        final XYIconAnnotation icon = entity.getIconAnnotation();
        for (int i = 0; i < allAnnotations.length; i++) {
            final StatefulXYIconAnnotation annotation = allAnnotations[i];
            if (icon == annotation.getVisible() || icon == annotation.getHighlighted()) {
                return annotation;
            }
        }
        return null;
    }

    private void updateAnnotations(final XYPlot plot) {
        boolean changed = false;
        for (int i = 0; i < allAnnotationIcons.size(); i++) {
            final XYIconAnnotation annotation = allAnnotationIcons.get(i);
            if (addedAnnotationIcons.contains(annotation)) {
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

    private boolean addAnnotations(final StatefulXYIconAnnotation highlighted) {
        boolean changed = addNavigationAnnotations();
        changed |= addPanLiveAnnotations(highlighted);
        return changed;
    }

    private boolean addNavigationAnnotations() {
        boolean changed = false;
        for (int i = 0; i < navAnnotations.length; i++) {
            final StatefulXYIconAnnotation annotation = navAnnotations[i];
            if (annotation.render(addedAnnotationIcons)) {
                changed = true;
            }
        }
        return changed;
    }

    private boolean addPanLiveAnnotations(final StatefulXYIconAnnotation highlighted) {
        if (highlighted == panLiveForward && panLiveIconState.isForwardVisible()) {
            if (addedAnnotationIcons.add(panLiveForward.getHighlighted())) {
                addedAnnotationIcons.remove(panLiveForward.getVisible());
                addedAnnotationIcons.remove(panLiveBackward.getVisible());
                addedAnnotationIcons.remove(panLiveBackward.getHighlighted());
                renderedPanLiveIconState = PanLiveIconState.PanLiveForwardHighlighted;
                panLiveIconState = renderedPanLiveIconState;
                return true;
            } else {
                return false;
            }
        } else if (highlighted != panLiveForward && panLiveIconState.isForwardVisible()) {
            if (addedAnnotationIcons.add(panLiveForward.getVisible())) {
                addedAnnotationIcons.remove(panLiveForward.getHighlighted());
                addedAnnotationIcons.remove(panLiveBackward.getVisible());
                addedAnnotationIcons.remove(panLiveBackward.getHighlighted());
                renderedPanLiveIconState = PanLiveIconState.PanLiveForwardVisible;
                panLiveIconState = renderedPanLiveIconState;
                return true;
            } else {
                return false;
            }
        } else if (highlighted == panLiveBackward && panLiveIconState.isBackwardVisible()) {
            if (addedAnnotationIcons.add(panLiveBackward.getHighlighted())) {
                addedAnnotationIcons.remove(panLiveBackward.getVisible());
                addedAnnotationIcons.remove(panLiveForward.getVisible());
                addedAnnotationIcons.remove(panLiveForward.getHighlighted());
                renderedPanLiveIconState = PanLiveIconState.PanLiveBackwardHighlighted;
                panLiveIconState = renderedPanLiveIconState;
                return true;
            } else {
                return false;
            }
        } else if (highlighted != panLiveBackward && panLiveIconState.isBackwardVisible()) {
            if (addedAnnotationIcons.add(panLiveBackward.getVisible())) {
                addedAnnotationIcons.remove(panLiveBackward.getHighlighted());
                addedAnnotationIcons.remove(panLiveForward.getVisible());
                addedAnnotationIcons.remove(panLiveForward.getHighlighted());
                renderedPanLiveIconState = PanLiveIconState.PanLiveBackwardVisible;
                panLiveIconState = renderedPanLiveIconState;
                return true;
            } else {
                return false;
            }
        } else if (panLiveIconState == PanLiveIconState.Invisible
                && renderedPanLiveIconState != PanLiveIconState.Invisible) {
            boolean changed = addedAnnotationIcons.remove(panLiveBackward.getVisible());
            changed |= addedAnnotationIcons.remove(panLiveBackward.getHighlighted());
            changed |= addedAnnotationIcons.remove(panLiveForward.getVisible());
            changed |= addedAnnotationIcons.remove(panLiveForward.getHighlighted());
            renderedPanLiveIconState = PanLiveIconState.Invisible;
            panLiveIconState = renderedPanLiveIconState;
            return changed;
        }
        return false;
    }

    public void mouseExited() {
        if (navShowingOnPlotPlot != null) {
            if (!addedAnnotationIcons.isEmpty()) {
                addedAnnotationIcons.clear();
                for (int i = 0; i < navAnnotations.length; i++) {
                    final StatefulXYIconAnnotation annotation = navAnnotations[i];
                    annotation.reset();
                }
                updateAnnotations(navShowingOnPlotPlot);
            }
            navShowingOnPlotPlot = null;
        }
        highlighting = false;
        stopButtonTimer();
        hideNote();
        updatePanLiveAnnotations();
    }

    private void updatePanLiveAnnotations() {
        if (addPanLiveAnnotations(null)) {
            final XYPlot lastSubplot = chartPanel.getCombinedPlot().getLastSubplot();
            if (lastSubplot != null) {
                updateAnnotations(lastSubplot);
            }
        }
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
            final StatefulXYIconAnnotation annotation = getIconAnnotation(l);
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

    private void startButtonTimer(final StatefulXYIconAnnotation annotation, final ActionListener action) {
        /*
         * It happened that we initialized a timer here when the previous one wasn't closed yet. We then lost the
         * reference for a timer which we never closed again and ran into an endless-loop. Therefore we check for
         * already running timers and eventually stop that one.
         */
        stopButtonTimer();

        buttonTimer = new Timer(BUTTON_TIMER_DELAY, action);
        buttonTimer.setInitialDelay(0);
        buttonTimer.start();
        buttonTimerAnnotation = annotation;
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
            final StatefulXYIconAnnotation iconAnnotation = getIconAnnotation(l);
            final boolean leftMouseClick = e.getButton() == MouseEvent.BUTTON1;
            final boolean rightMouseClick = e.getButton() == MouseEvent.BUTTON2;
            if (iconAnnotation == reset && (leftMouseClick || rightMouseClick)) {
                final int initialVisibleItemCount = chartPanel.getInitialVisibleItemCount();
                if (e.isControlDown() || e.isShiftDown() || rightMouseClick) {
                    if (e.isControlDown() && e.isShiftDown()) {
                        //only reload data, no reset range to the right
                        chartPanel.reloadData();
                    } else {
                        chartPanel.resetRange(initialVisibleItemCount, chartPanel.getDefaultTrailingRangeGapRate(),
                                () -> chartPanel.reloadData());
                        chartPanel.updateUserGapRateRight();
                    }
                } else {
                    chartPanel.resetRange(initialVisibleItemCount, chartPanel.getDefaultTrailingRangeGapRate());
                    chartPanel.updateUserGapRateRight();
                }
                Axises.resetAllAutoRanges(chartPanel);
                XYPlots.resetAllRangePannables(chartPanel);
            } else if (iconAnnotation == configure && leftMouseClick) {
                chartPanel.getPlotConfigurationHelper().displayPopupMenu(e);
            } else if ((iconAnnotation == panLiveForward || iconAnnotation == panLiveBackward) && leftMouseClick) {
                chartPanel.getPlotPanHelper().panLive(e);
            }
        }
    }

    private boolean stopButtonTimer() {
        if (buttonTimer != null) {
            buttonTimer.stop();
            buttonTimer = null;
            buttonTimerAnnotation = null;
            return true;
        }
        return false;
    }

    public boolean isHighlighting() {
        return highlighting || noteShowingOnPlot != null || panLiveIconState.isHighlighted();
    }

    /**
     * Return's if Nav-Icons have been changed/updated.
     */
    public boolean showPanLiveIcon(final boolean backward) {
        if (backward) {
            if (!panLiveIconState.isBackwardVisible()) {
                panLiveIconState = PanLiveIconState.PanLiveBackwardVisible;
                updateNavigation();
                return true;
            }
        } else {
            if (!panLiveIconState.isForwardVisible()) {
                panLiveIconState = PanLiveIconState.PanLiveForwardVisible;
                updateNavigation();
                return true;
            }
        }
        return false;
    }

    public boolean hidePanLiveIcon() {
        if (panLiveIconState != PanLiveIconState.Invisible) {
            panLiveIconState = PanLiveIconState.Invisible;
            updateNavigation();
            return true;
        }
        return false;
    }

    public XYNoteIconAnnotation getNoteShowingIconAnnotation() {
        return noteShowingIconAnnotation;
    }

}
