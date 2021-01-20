package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.shape;

import java.awt.Shape;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.LineWidthType;

public interface ISeriesShapeFactory {

    Shape newShape(LineWidthType lineWidthType);

}
