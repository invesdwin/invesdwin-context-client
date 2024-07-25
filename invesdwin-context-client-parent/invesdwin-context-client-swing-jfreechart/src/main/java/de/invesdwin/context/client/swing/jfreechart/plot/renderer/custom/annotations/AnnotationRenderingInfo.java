package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations;

import java.awt.Shape;

import javax.annotation.concurrent.NotThreadSafe;

import org.jfree.chart.annotations.XYTextAnnotation;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

@NotThreadSafe
public class AnnotationRenderingInfo {

    private static final int OVERLAP_Y_GAP = 2;

    private final Int2IntOpenHashMap hashCode_count = new Int2IntOpenHashMap();

    public void beforePlotDraw() {
        if (!hashCode_count.isEmpty()) {
            hashCode_count.clear();
        }
    }

    public void applyCollisionPrevention(final LabelVerticalAlignType verticalAlign, final XYTextAnnotation annotation,
            final Shape annotationShape) {
        //TODO:
        //- nur y modifizieren
        //- alle shapes die sich gemerkt wurden durch iterieren und checken ob eine kollision stattfindet
        //  - falls kolission gefunden, eins nach unten/oben schieben abhängig von verticalAlign
        //  - schleife von vorne starten und bei erneuter kolission wiederholen
        //  - ende wenn keine kollision stattgefunden hat und letzte shape
        // modifizierte shape in die collection hinzufügen

        //danach im sampler schauen wenn komplett rausgezoomed über mehrere jahre die collision detection auffällig ist, evtl in quadranten separieren mit 2-step colilision detection
        //nicht gegen rectangle2d, sondern gegen die einzelnen koordinaten checken und diese in der schleife modifizieren
        //shape.intersects(annotationShape.getBounds2D());
        //        final int hashCode = hashCode(dataArea, verticalAlign.ordinal(), shape);
        //        final int prevCount = hashCode_count.addTo(hashCode, 1);
        //        return prevCount;
    }

    private int calculateOverlapYAdd(final LabelVerticalAlignType verticalAlign, final XYTextAnnotation annotation,
            final int prevCount) {
        if (prevCount > 0) {
            final int overlapYAdd = annotation.getFont().getSize() + OVERLAP_Y_GAP;
            final int yAdd = overlapYAdd * prevCount;
            if (LabelVerticalAlignType.Bottom.equals(verticalAlign)) {
                return yAdd;
            } else {
                return -yAdd;
            }
        }
        return 0;
    }

    //    public int getAndIncrementLineOverlap(final Rectangle2D dataArea, final LabelHorizontalAlignType horizontalAlign,
    //            final LabelVerticalAlignType verticalAlign, final int startIndex, final int endIndex, final double y1,
    //            final double y2) {
    //        final int hashCode = hashCodeLine(dataArea, horizontalAlign, verticalAlign, startIndex, endIndex, y1, y2);
    //        final int prevCount = hashCode_count.addTo(hashCode, 1);
    //        return prevCount;
    //    }
    //
    //    private int hashCode(final Rectangle2D dataArea, final double x, final double y, final int verticalAlign) {
    //        final double step = rangeAxis.getRange().getLength() * PRICE_TO_STEP_FRACTION;
    //        final double roundedPrice = Doubles.roundToStep(y, step);
    //        final int prime = 31;
    //        int result = 1;
    //        result = prime * result + (Integer.hashCode(verticalAlign));
    //        result = prime * result + (Integer.hashCode(itemIndex));
    //        result = prime * result + (Double.hashCode(roundedPrice));
    //        return result;
    //    }

}
