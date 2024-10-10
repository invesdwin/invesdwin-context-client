package de.invesdwin.context.client.swing.jfreechart.plot.renderer.custom.marker;

import javax.annotation.concurrent.Immutable;

import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;

@Immutable
public class CustomValueMarker extends ValueMarker {

    private double axisValue;
    private String axisLabel;
    private XYPlot currentPlot;

    public CustomValueMarker(final double value) {
        super(value);
        this.axisValue = value;
    }

    public double getAxisValue() {
        return axisValue;
    }

    public void setAxisValue(final double axisValue) {
        this.axisValue = axisValue;
    }

    public String getAxisLabel() {
        return axisLabel;
    }

    public void setAxisLabel(final String axisLabel) {
        this.axisLabel = axisLabel;
    }

    public XYPlot getCurrentPlot() {
        return currentPlot;
    }

    public void setCurrentPlot(final XYPlot currentPlot) {
        this.currentPlot = currentPlot;
    }
}
