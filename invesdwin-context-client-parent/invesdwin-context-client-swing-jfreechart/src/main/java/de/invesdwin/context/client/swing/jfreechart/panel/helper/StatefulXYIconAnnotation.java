package de.invesdwin.context.client.swing.jfreechart.panel.helper;

import java.util.List;
import java.util.function.BooleanSupplier;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.swing.jfreechart.panel.InteractiveChartPanel;
import de.invesdwin.context.client.swing.jfreechart.plot.annotation.XYIconAnnotation;

@NotThreadSafe
public class StatefulXYIconAnnotation {
    private final InteractiveChartPanel chartPanel;
    private final XYIconAnnotation invisible;
    private final XYIconAnnotation disabled;
    private final XYIconAnnotation visible;
    private final XYIconAnnotation highlighted;
    private final XYIconAnnotation[] array;
    private boolean allowMasterDatasetEmpty = false;
    private BooleanSupplier disabledCheck = () -> false;
    private XYIconAnnotation renderedState;
    private XYIconAnnotation state;

    public StatefulXYIconAnnotation(final InteractiveChartPanel chartPanel, final XYIconAnnotation invisible,
            final XYIconAnnotation disabled, final XYIconAnnotation visible, final XYIconAnnotation highlighted) {
        this.chartPanel = chartPanel;
        this.invisible = invisible;
        this.disabled = disabled;
        this.visible = visible;
        this.highlighted = highlighted;
        this.array = new XYIconAnnotation[] { invisible, visible, highlighted };
    }

    public StatefulXYIconAnnotation setAllowMasterDatasetEmpty(final boolean allowMasterDatasetEmpty) {
        this.allowMasterDatasetEmpty = allowMasterDatasetEmpty;
        return this;
    }

    public boolean isAllowMasterDatasetEmpty() {
        return allowMasterDatasetEmpty;
    }

    public BooleanSupplier getDisabledCheck() {
        return disabledCheck;
    }

    public StatefulXYIconAnnotation setDisabledCheck(final BooleanSupplier disabledCheck) {
        this.disabledCheck = disabledCheck;
        return this;
    }

    public XYIconAnnotation[] asArray() {
        return array;
    }

    public boolean updateState(final boolean navigationVisible, final boolean hasDataset,
            final StatefulXYIconAnnotation highlightedAnnotation) {
        if (navigationVisible && shouldShowNavigationAnnotation(hasDataset)) {
            final boolean iconDisabled = disabledCheck.getAsBoolean();
            if (iconDisabled) {
                state = disabled;
            } else if (highlightedAnnotation == this) {
                state = highlighted;
            } else {
                state = visible;
            }
        } else {
            state = invisible;
        }
        return state != renderedState;
    }

    private boolean shouldShowNavigationAnnotation(final boolean hasDataset) {
        final boolean isMasterDatasetEmpty = chartPanel.getMasterDataset().getData().isEmpty();
        if (isMasterDatasetEmpty) {
            return allowMasterDatasetEmpty;
        }
        return hasDataset;
    }

    public boolean render(final List<XYIconAnnotation> addedAnnotations) {
        if (renderedState == state) {
            return false;
        }
        renderedState = state;

        if (addedAnnotations.add(state)) {
            for (int i = 0; i < array.length; i++) {
                final XYIconAnnotation annotation = array[i];
                if (annotation != state) {
                    addedAnnotations.remove(annotation);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public void reset() {
        renderedState = null;
    }

    public XYIconAnnotation getInvisible() {
        return invisible;
    }

    public XYIconAnnotation getDisabled() {
        return disabled;
    }

    public XYIconAnnotation getVisible() {
        return visible;
    }

    public XYIconAnnotation getHighlighted() {
        return highlighted;
    }
}
