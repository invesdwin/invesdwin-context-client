package org.swingexplorer.internal;

import java.io.Closeable;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class SwingExplorerApplication extends Application implements Closeable {

    @Override
    public void close() {
        if (frmMain != null) {
            frmMain.dispose();
            frmMain = null;
        }
    }

}
