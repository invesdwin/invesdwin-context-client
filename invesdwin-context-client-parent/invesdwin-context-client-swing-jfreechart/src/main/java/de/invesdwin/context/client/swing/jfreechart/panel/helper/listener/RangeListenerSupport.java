package de.invesdwin.context.client.swing.jfreechart.panel.helper.listener;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jfree.data.Range;

@Immutable
public class RangeListenerSupport implements IRangeListener {

    @Override
    public Range beforeLimitRange(final Range range, final MutableBoolean rangeChanged) {
        return range;
    }

    @Override
    public Range afterLimitRange(final Range range, final MutableBoolean rangeChanged) {
        return range;
    }

    @Override
    public void onRangeChanged(final Range range) {}

}
