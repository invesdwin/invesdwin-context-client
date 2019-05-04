package de.invesdwin.context.client.swing.jfreechart.panel.helper;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jfree.data.Range;

public interface ILimitRangeListener {

    Range beforeLimitRange(Range range, MutableBoolean rangeChanged);

}
