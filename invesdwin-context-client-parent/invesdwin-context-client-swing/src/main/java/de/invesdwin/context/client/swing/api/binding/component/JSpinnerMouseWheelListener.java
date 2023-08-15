package de.invesdwin.context.client.swing.api.binding.component;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.annotation.concurrent.Immutable;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

@Immutable
public class JSpinnerMouseWheelListener implements MouseWheelListener {

    private final JSpinner spinner;

    public JSpinnerMouseWheelListener(final JSpinner spinner) {
        super();
        this.spinner = spinner;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
        final Object value = e.getWheelRotation() < 0 ? this.spinner.getNextValue() : this.spinner.getPreviousValue();
        if (value != null) {
            final SpinnerModel model = spinner.getModel();
            if (model instanceof SpinnerNumberModel && value instanceof Comparable) {
                final SpinnerNumberModel cModel = (SpinnerNumberModel) model;
                final Comparable<Object> maximum = (Comparable<Object>) cModel.getMaximum();
                final Comparable<Object> minimum = (Comparable<Object>) cModel.getMinimum();
                final Comparable<Object> cValue = (Comparable<Object>) value;
                if (maximum != null && cValue.compareTo(maximum) > 0
                        || minimum != null && cValue.compareTo(minimum) < 0) {
                    return;
                }
            }
            this.spinner.setValue(value);
        }
    }
}
