package de.invesdwin.context.client.swing.jfreechart.plot.axis;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.Range;

import de.invesdwin.context.client.swing.jfreechart.panel.basis.CustomCombinedDomainXYPlot;
import de.invesdwin.context.client.swing.jfreechart.plot.CustomXYPlot;

@NotThreadSafe
public class CustomNumberAxis extends NumberAxis {

    /**
     * 3% minimum edge distance seems to work fine to avoid overlapping vertical tick labels
     */
    private static final double MIN_TICK_LABEL_VERTICAL_EDGE_DISTANCE_MULTIPLIER = 0.03;

    private final NumberFormat limitedNumberFormatOverride = new NumberFormat() {

        private boolean shouldFormatLabel(final double number) {
            final Range range = getRange();
            final double length = range.getLength();
            final double lowerBound = range.getLowerBound();
            final double upperBound = range.getUpperBound();

            final Plot plot = getPlot();
            if (plot instanceof CustomXYPlot) {
                final CustomXYPlot cPlot = (CustomXYPlot) plot;
                final CustomCombinedDomainXYPlot combinedPlot = cPlot.getCombinedPlot();
                if (combinedPlot.getGap() > 0D) {
                    return true;
                }

                final double reducedLength = length * MIN_TICK_LABEL_VERTICAL_EDGE_DISTANCE_MULTIPLIER;
                final double reducedLowerBound = lowerBound + reducedLength;
                final double reducedUpperBound = upperBound - reducedLength;
                if (!combinedPlot.isSubplotAtTopEdge(cPlot) && number > reducedUpperBound) {
                    return false;
                }
                if (!combinedPlot.isSubplotAtBottomEdge(cPlot) && number < reducedLowerBound) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public StringBuffer format(final double number, final StringBuffer toAppendTo, final FieldPosition pos) {
            if (shouldFormatLabel(number)) {
                final NumberFormat formatter = superGetNumberFormatOverride();
                if (formatter != null) {
                    toAppendTo.append(formatter.format(number));
                } else {
                    toAppendTo.append(getTickUnit().valueToString(number));
                }
            }
            return toAppendTo;
        }

        @Override
        public StringBuffer format(final long number, final StringBuffer toAppendTo, final FieldPosition pos) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Number parse(final String source, final ParsePosition parsePosition) {
            throw new UnsupportedOperationException();
        }

    };

    private boolean limitNumberFormatOverride = false;

    @Override
    public NumberFormat getNumberFormatOverride() {
        if (limitNumberFormatOverride) {
            return limitedNumberFormatOverride;
        } else {
            return superGetNumberFormatOverride();
        }
    }

    private NumberFormat superGetNumberFormatOverride() {
        return super.getNumberFormatOverride();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List refreshTicksVertical(final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge) {
        limitNumberFormatOverride = true;
        try {
            return super.refreshTicksVertical(g2, dataArea, edge);
        } finally {
            limitNumberFormatOverride = false;
        }
    }

}
