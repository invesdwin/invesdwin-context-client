package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.annotations;

import javax.annotation.concurrent.Immutable;

import org.jfree.chart.ui.TextAnchor;

@Immutable
public enum LabelHorizontalAlignType {
    Left,
    Center,
    Right;

    public TextAnchor getTextAnchor(final LabelVerticalAlignType vertical) {
        return vertical.getTextAnchor(this);
    }

}
