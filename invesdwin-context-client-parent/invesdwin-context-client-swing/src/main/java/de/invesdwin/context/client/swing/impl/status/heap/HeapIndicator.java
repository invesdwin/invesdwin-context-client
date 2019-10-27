package de.invesdwin.context.client.swing.impl.status.heap;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import de.invesdwin.util.math.decimal.scaled.ByteSize;
import de.invesdwin.util.math.decimal.scaled.ByteSizeScale;
import de.invesdwin.util.math.decimal.scaled.Percent;
import de.invesdwin.util.math.decimal.scaled.PercentScale;
import de.invesdwin.util.swing.Components;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.util.time.fdate.FTimeUnit;

/**
 * https://stackoverflow.com/questions/1346105/java-swing-free-memory-component-needed
 * 
 * http://itblog.huber-net.de/2018/02/eine-swing-komponente-jvm-memory-indicator/
 * 
 * @author subes
 *
 */
@NotThreadSafe
public class HeapIndicator extends JProgressBar {

    private static final int TIMER_INTERVAL_IN_MS = new Duration(1, FTimeUnit.SECONDS).intValue(FTimeUnit.MILLISECONDS);

    public HeapIndicator() {
        super(0, 100);
        setStringPainted(true);
        setString("");
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent mev) {
                if (mev.getClickCount() == 2) {
                    System.gc();
                    update();
                }
            }
        });
        final Timer t = new Timer(TIMER_INTERVAL_IN_MS, new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent aev) {
                update();
            }
        });
        t.start();
        update();
    }

    private void update() {
        final Runtime jvmRuntime = Runtime.getRuntime();
        final ByteSize totalMemory = new ByteSize(jvmRuntime.totalMemory(), ByteSizeScale.BYTES);
        final ByteSize maxMemory = new ByteSize(jvmRuntime.maxMemory(), ByteSizeScale.BYTES);
        final ByteSize freeMemory = new ByteSize(jvmRuntime.freeMemory(), ByteSizeScale.BYTES);
        final ByteSize usedMemory = totalMemory.subtract(freeMemory);

        final int usedPercentage = (int) new Percent(usedMemory, totalMemory).getValue(PercentScale.PERCENT);
        final String textToShow = usedPercentage + "% of " + toGigaBytes(totalMemory);
        final String toolTipToShow = "<html>Memory usage is " + toGigaBytes(usedMemory) + " of "
                + toGigaBytes(totalMemory) + " (max: " + toGigaBytes(maxMemory)
                + ")<br>(double-click to run Garbage Collector)";

        setValue(usedPercentage);
        setString(textToShow);
        Components.setToolTipText(this, toolTipToShow);
    }

    private String toGigaBytes(final ByteSize byteSize) {
        return byteSize.toStringBuilder()
                .withScale(ByteSizeScale.GIGABYTES)
                .withDecimalDigits(1)
                .withSymbol(true)
                .toString();
    }
}