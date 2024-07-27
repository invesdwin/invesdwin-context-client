package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;

import de.invesdwin.context.client.swing.jfreechart.plot.Annotations;
import de.invesdwin.context.client.swing.jfreechart.plot.Axises;
import de.invesdwin.util.collections.list.BisectSortedList;
import de.invesdwin.util.lang.comparator.AComparator;
import de.invesdwin.util.lang.comparator.IComparator;
import de.invesdwin.util.math.Doubles;
import de.invesdwin.util.math.Integers;

@NotThreadSafe
public class AnnotationRenderingInfo {

    private static final IComparator<Rectangle2D> MIN_Y_COMPARATOR = new AComparator<Rectangle2D>() {
        @Override
        public int compareTypedNotNullSafe(final Rectangle2D o1, final Rectangle2D o2) {
            return Doubles.compare(o1.getMinY(), o2.getMinY());
        }
    };
    private static final IComparator<Rectangle2D> MAX_Y_COMPARATOR = new AComparator<Rectangle2D>() {
        @Override
        public int compareTypedNotNullSafe(final Rectangle2D o1, final Rectangle2D o2) {
            return Doubles.compare(o1.getMaxY(), o2.getMaxY());
        }
    };
    private final BisectSortedList<Rectangle2D> drawnBoundsMinYBottom = new BisectSortedList<>(MIN_Y_COMPARATOR);
    private final BisectSortedList<Rectangle2D> drawnBoundsMaxYTop = new BisectSortedList<>(MAX_Y_COMPARATOR);
    private double heightMultiplierCached = Double.NaN;

    public void beforePlotDraw() {
        if (!drawnBoundsMinYBottom.isEmpty() || !drawnBoundsMinYBottom.isEmpty()) {
            drawnBoundsMinYBottom.clear();
            drawnBoundsMaxYTop.clear();
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
                final double thisHeight = Axises.java2DToLength(rangeAxis, thisBounds.height, dataArea, rangeEdge);
                final double fontHeight = annotation.getFont().getSize() + 2D;
                heightMultiplierCached = fontHeight / thisHeight;
            } finally {
                annotation.setRotationAngle(angleBefore);
            }
        }
        return heightMultiplierCached;
    }

    public void applyCollisionPrevention(final Graphics2D g2, final XYPlot plot, final ValueAxis domainAxis,
            final Rectangle2D dataArea, final RectangleEdge domainEdge, final RectangleEdge rangeEdge,
            final ValueAxis rangeAxis, final LabelVerticalAlignType verticalAlign, final XYTextAnnotation annotation) {

        final double domainLength = domainAxis.getRange().getLength();
        if (domainLength > 1500) {
            return;
        }

        final Shape thisShape = Annotations.calculateShape(g2, plot, domainAxis, dataArea, domainEdge, rangeEdge,
                rangeAxis, annotation);
        final Rectangle2D.Double thisBounds = (Rectangle2D.Double) thisShape.getBounds2D();

        thisBounds.x = domainAxis.java2DToValue(thisBounds.x, dataArea, domainEdge);
        thisBounds.y = rangeAxis.java2DToValue(thisBounds.y, dataArea, rangeEdge);
        thisBounds.width = Axises.java2DToLength(domainAxis, thisBounds.width, dataArea, domainEdge);
        thisBounds.height = Axises.java2DToLength(rangeAxis, thisBounds.height, dataArea, rangeEdge);
        final double thisInitialY = thisBounds.y;
        final boolean bottom = LabelVerticalAlignType.Bottom == verticalAlign;
        if (bottom) {
            if (drawnBoundsMinYBottom.isEmpty() && !drawnBoundsMaxYTop.isEmpty()) {
                drawnBoundsMinYBottom.addAll(drawnBoundsMaxYTop);
            }
            final BisectSortedList<Rectangle2D> drawnBounds = drawnBoundsMinYBottom;
            double stopMaxY = thisBounds.getMaxY();
            final int start = Integers.max(drawnBounds.bisect(thisBounds) - 1, 0);
            for (int i = start; i < drawnBounds.size(); i++) {
                final Rectangle2D otherBounds = drawnBounds.get(i);
                final double itemDistance = Doubles.abs(otherBounds.getCenterX() - thisBounds.getCenterX());
                if (itemDistance <= 3 && otherBounds.intersects(thisBounds)) {
                    final double heightMultiplier = getHeightMultiplier(g2, plot, domainAxis, dataArea, domainEdge,
                            rangeEdge, rangeAxis, verticalAlign, annotation);
                    //draw to the bottom
                    final double updatedY = otherBounds.getY() + otherBounds.getHeight() * heightMultiplier;
                    thisBounds.y = updatedY;
                    stopMaxY = thisBounds.getMaxY();
                } else if (otherBounds.getMinY() > stopMaxY) {
                    //we wont find anything that hits us anymore
                    break;
                }
            }
        } else {
            if (drawnBoundsMaxYTop.isEmpty() && !drawnBoundsMinYBottom.isEmpty()) {
                drawnBoundsMaxYTop.addAll(drawnBoundsMinYBottom);
            }
            final BisectSortedList<Rectangle2D> drawnBounds = drawnBoundsMaxYTop;
            double stopMinY = thisBounds.getMinY();
            final int start = Integers.min(drawnBounds.bisect(thisBounds) + 1, drawnBounds.size() - 1);
            for (int i = start; i >= 0; i--) {
                final Rectangle2D otherBounds = drawnBounds.get(i);
                final double itemDistance = Doubles.abs(otherBounds.getCenterX() - thisBounds.getCenterX());
                if (itemDistance <= 3 && otherBounds.intersects(thisBounds)) {
                    final double heightMultiplier = getHeightMultiplier(g2, plot, domainAxis, dataArea, domainEdge,
                            rangeEdge, rangeAxis, verticalAlign, annotation);
                    //draw upwards
                    final double updatedY = otherBounds.getY() - thisBounds.getHeight() * heightMultiplier;
                    thisBounds.y = updatedY;
                    stopMinY = thisBounds.getMinY();
                } else if (otherBounds.getMaxY() < stopMinY) {
                    //we wont find anything that hits us anymore
                    break;
                }
            }
        }

        if (thisBounds.y != thisInitialY) {
            final double yDifference = thisBounds.y - thisInitialY;
            annotation.setY(annotation.getY() + yDifference);
        }
        if (bottom) {
            drawnBoundsMinYBottom.add(thisBounds);
            if (!drawnBoundsMaxYTop.isEmpty()) {
                drawnBoundsMaxYTop.add(thisBounds);
            }
        } else {
            drawnBoundsMaxYTop.add(thisBounds);
            if (!drawnBoundsMinYBottom.isEmpty()) {
                drawnBoundsMinYBottom.add(thisBounds);
            }
        }
    }

}
