package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;

import de.invesdwin.context.client.swing.jfreechart.plot.Annotations;
import de.invesdwin.util.collections.list.HighLowSortedList;
import de.invesdwin.util.lang.comparator.AComparator;
import de.invesdwin.util.lang.comparator.IComparator;
import de.invesdwin.util.math.Doubles;
import de.invesdwin.util.math.Integers;

@NotThreadSafe
public class AnnotationRenderingInfo {

    private static final IComparator<Rectangle2D> COMPARATOR = new AComparator<Rectangle2D>() {
        @Override
        public int compareTypedNotNullSafe(final Rectangle2D o1, final Rectangle2D o2) {
            return Doubles.compare(o1.getY(), o2.getY());
        }
    };
    private final List<Rectangle2D> drawnAnnotationBounds = new HighLowSortedList<>(COMPARATOR);
    private double heightMultiplierCached = Double.NaN;

    public void beforePlotDraw() {
        if (!drawnAnnotationBounds.isEmpty()) {
            drawnAnnotationBounds.clear();
            heightMultiplierCached = Double.NaN;
        }
    }

    private double getHeightMultiplier(final Graphics2D g2, final XYPlot plot, final ValueAxis domainAxis,
            final Rectangle2D dataArea, final RectangleEdge domainEdge, final RectangleEdge rangeEdge,
            final ValueAxis rangeAxis, final LabelVerticalAlignType verticalAlign, final XYTextAnnotation annotation) {
        if (Doubles.isNaN(heightMultiplierCached)) {
            annotation.setNotify(false);
            final double angleBefore = annotation.getRotationAngle();
            try {
                annotation.setRotationAngle(0D);
                final Shape thisShape = Annotations.calculateShape(g2, plot, domainAxis, dataArea, domainEdge,
                        rangeEdge, rangeAxis, annotation);
                final Rectangle2D.Double thisBounds = (Rectangle2D.Double) thisShape.getBounds2D();
                final double thisHeight = java2DToLength(rangeAxis, thisBounds.height, dataArea, rangeEdge);
                final double fontHeight = annotation.getFont().getSize() + 2D;
                heightMultiplierCached = fontHeight / thisHeight;
            } finally {
                annotation.setRotationAngle(angleBefore);
            }
        }
        return heightMultiplierCached;
    }

    //TODO: move this into lists?
    protected int bisect(final Rectangle2D x) {
        int lo = 0;
        int hi = drawnAnnotationBounds.size();
        while (lo < hi) {
            final int mid = (lo + hi) / 2;
            //if (x < list.get(mid)) {
            if (COMPARATOR.compareTyped(drawnAnnotationBounds.get(mid), x) > 0) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return lo;
    }

    public void applyCollisionPrevention(final Graphics2D g2, final XYPlot plot, final ValueAxis domainAxis,
            final Rectangle2D dataArea, final RectangleEdge domainEdge, final RectangleEdge rangeEdge,
            final ValueAxis rangeAxis, final LabelVerticalAlignType verticalAlign, final XYTextAnnotation annotation) {

        final double domainLength = domainAxis.getRange().getLength();
        System.out.println(domainLength);
        if (domainLength > 1500) {
            System.out.println("skip");
            return;
        }

        final Shape thisShape = Annotations.calculateShape(g2, plot, domainAxis, dataArea, domainEdge, rangeEdge,
                rangeAxis, annotation);
        final Rectangle2D.Double thisBounds = (Rectangle2D.Double) thisShape.getBounds2D();

        if (drawnAnnotationBounds.size() == 3) {
            System.out.println("blaaa");
        }

        thisBounds.x = domainAxis.java2DToValue(thisBounds.x, dataArea, domainEdge);
        thisBounds.y = rangeAxis.java2DToValue(thisBounds.y, dataArea, rangeEdge);
        thisBounds.width = java2DToLength(domainAxis, thisBounds.width, dataArea, domainEdge);
        thisBounds.height = java2DToLength(rangeAxis, thisBounds.height, dataArea, rangeEdge);
        final double thisInitialY = thisBounds.y;
        final boolean bottom = LabelVerticalAlignType.Bottom == verticalAlign;
        if (bottom) {
            final int start = Integers.max(bisect(thisBounds) - 1, 0);
            for (int i = start; i < drawnAnnotationBounds.size(); i++) {
                final Rectangle2D otherBounds = drawnAnnotationBounds.get(i);
                final double itemDistance = Doubles.abs(otherBounds.getCenterX() - thisBounds.getCenterX());
                if (itemDistance <= 3 && otherBounds.intersects(thisBounds)) {
                    final double heightMultiplier = getHeightMultiplier(g2, plot, domainAxis, dataArea, domainEdge,
                            rangeEdge, rangeAxis, verticalAlign, annotation);
                    System.out.println(i + ": " + heightMultiplier + " " + verticalAlign);
                    //draw to the bottom
                    final double updatedY = otherBounds.getY() + otherBounds.getHeight() * heightMultiplier;
                    thisBounds.y = updatedY;
                }
            }
        } else {
            final int start = Integers.min(bisect(thisBounds) + 1, drawnAnnotationBounds.size() - 1);
            for (int i = start; i >= 0; i--) {
                final Rectangle2D otherBounds = drawnAnnotationBounds.get(i);
                final double itemDistance = Doubles.abs(otherBounds.getCenterX() - thisBounds.getCenterX());
                if (itemDistance <= 3 && otherBounds.intersects(thisBounds)) {
                    final double heightMultiplier = getHeightMultiplier(g2, plot, domainAxis, dataArea, domainEdge,
                            rangeEdge, rangeAxis, verticalAlign, annotation);
                    System.out.println(i + ": " + heightMultiplier + " " + verticalAlign);
                    //draw upwards
                    final double updatedY = otherBounds.getY() - thisBounds.getHeight() * heightMultiplier;
                    thisBounds.y = updatedY;
                }
            }
        }

        if (thisBounds.y != thisInitialY) {
            final double yDifference = thisBounds.y - thisInitialY;
            annotation.setY(annotation.getY() + yDifference);
        }
        drawnAnnotationBounds.add(thisBounds);
    }

    public double java2DToLength(final ValueAxis axis, final double length, final Rectangle2D area,
            final RectangleEdge edge) {
        final double zero = axis.java2DToValue(0.0, area, edge);
        final double l = axis.java2DToValue(length, area, edge);
        return Math.abs(l - zero);
    }
}
