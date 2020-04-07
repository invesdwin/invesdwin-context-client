package de.invesdwin.context.client.swing;

import java.awt.Dimension;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.client.swing.api.RichApplicationSupport;

@Immutable
public class TestRichApplication extends RichApplicationSupport {

    @Override
    public Dimension getInitialFrameSize() {
        return new Dimension(1024, 768);
    }

}
