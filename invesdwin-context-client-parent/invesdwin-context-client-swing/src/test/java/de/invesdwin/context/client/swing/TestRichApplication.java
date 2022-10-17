package de.invesdwin.context.client.swing;

import java.awt.Dimension;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.client.swing.api.RichApplicationSupport;
import de.invesdwin.util.swing.HiDPI;

@Immutable
public class TestRichApplication extends RichApplicationSupport {

    @Override
    public Dimension getInitialFrameSize() {
        return HiDPI.scale(new Dimension(1024, 768));
    }

}
