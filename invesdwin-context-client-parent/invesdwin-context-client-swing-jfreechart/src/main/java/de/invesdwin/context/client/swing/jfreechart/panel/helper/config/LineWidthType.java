package de.invesdwin.context.client.swing.jfreechart.panel.helper.config;

import java.awt.BasicStroke;
import java.awt.Stroke;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.swing.HiDPI;

@Immutable
public enum LineWidthType {
    _1(1f),
    _2(2f),
    _3(3f),
    _4(4f);

    private final float width;

    LineWidthType(final float width) {
        this.width = width;
    }

    public float getWidth() {
        return width;
    }

    @Override
    public String toString() {
        return String.valueOf(width);
    }

    public static LineWidthType valueOf(final Stroke stroke) {
        if (stroke instanceof BasicStroke) {
            final BasicStroke cStroke = (BasicStroke) stroke;
            return valueOf(HiDPI.descale(cStroke.getLineWidth()));
        }
        throw UnknownArgumentException.newInstance(Stroke.class, stroke);
    }

    public static LineWidthType valueOf(final Number width) {
        return valueOf(width.intValue());
    }

    public static LineWidthType valueOf(final int width) {
        switch (width) {
        case 1:
            return _1;
        case 2:
            return _2;
        case 3:
            return _3;
        case 4:
            return _4;
        default:
            throw UnknownArgumentException.newInstance(Integer.class, width);
        }
    }

    public Stroke getStroke(final Stroke stroke) {
        final LineStyleType lineStyleType = LineStyleType.valueOf(stroke);
        return lineStyleType.getStroke(this);
    }
}
