package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations;

import javax.annotation.concurrent.Immutable;

import org.jfree.chart.ui.TextAnchor;

import de.invesdwin.util.error.UnknownArgumentException;

@Immutable
public enum LabelVerticalAlignType {
    Top {
        @Override
        public TextAnchor getTextAnchor(final LabelHorizontalAlignType horizontal) {
            switch (horizontal) {
            case Left:
                return TextAnchor.BOTTOM_LEFT;
            case Center:
                return TextAnchor.BOTTOM_CENTER;
            case Right:
                return TextAnchor.BOTTOM_RIGHT;
            default:
                throw UnknownArgumentException.newInstance(LabelHorizontalAlignType.class, horizontal);
            }
        }
    },
    Center {
        @Override
        public TextAnchor getTextAnchor(final LabelHorizontalAlignType horizontal) {
            switch (horizontal) {
            case Left:
                return TextAnchor.CENTER_LEFT;
            case Center:
                return TextAnchor.CENTER;
            case Right:
                return TextAnchor.CENTER_RIGHT;
            default:
                throw UnknownArgumentException.newInstance(LabelHorizontalAlignType.class, horizontal);
            }
        }
    },
    Bottom {
        @Override
        public TextAnchor getTextAnchor(final LabelHorizontalAlignType horizontal) {
            switch (horizontal) {
            case Left:
                return TextAnchor.TOP_LEFT;
            case Center:
                return TextAnchor.TOP_CENTER;
            case Right:
                return TextAnchor.TOP_RIGHT;
            default:
                throw UnknownArgumentException.newInstance(LabelHorizontalAlignType.class, horizontal);
            }
        }
    };

    public abstract TextAnchor getTextAnchor(LabelHorizontalAlignType horizontal);

}
