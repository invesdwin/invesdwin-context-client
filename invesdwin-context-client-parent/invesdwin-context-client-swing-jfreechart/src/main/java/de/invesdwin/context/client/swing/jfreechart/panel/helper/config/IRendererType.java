package de.invesdwin.context.client.swing.jfreechart.panel.helper.config;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.legend.HighlightedLegendInfo;

public interface IRendererType {

    SeriesRendererType getSeriesRendererType();

    boolean isSeriesRendererTypeConfigurable();

    boolean isLineStyleConfigurable();

    boolean isLineWidthConfigurable();

    /**
     * When this returns true, the renderer needs to implements IUpDownColorRenderer
     */
    boolean isUpColorConfigurable();

    /**
     * When this returns true, the renderer needs to implements IUpDownColorRenderer
     */
    boolean isDownColorConfigurable();

    boolean isSeriesColorConfigurable();

    boolean isPriceLineConfigurable();

    void reset(HighlightedLegendInfo highlighted, SeriesInitialSettings initialSettings);

    default String getSeriesColorName() {
        return "Series";
    }

    default String getUpColorName() {
        return "Up";
    }

    default String getDownColorName() {
        return "Down";
    }

}
