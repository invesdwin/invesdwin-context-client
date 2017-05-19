package de.invesdwin.common.client.swing;

import java.awt.Dimension;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.common.client.swing.api.ARichApplication;

@Immutable
public class TestRichApplication extends ARichApplication {

    @Override
    public Dimension getInitialFrameSize() {
        return new Dimension(1024, 768);
    }

}
