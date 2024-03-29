package de.invesdwin.context.client.swing.jfreechart.panel.helper.listener;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jfree.data.Range;

public interface IRangeListener {

    IRangeListener[] EMPTY_ARRAY = new IRangeListener[0];

    Range beforeLimitRange(Range range, MutableBoolean rangeChanged);

    Range afterLimitRange(Range range, MutableBoolean rangeChanged);

    void onRangeChanged(Range range);

}
