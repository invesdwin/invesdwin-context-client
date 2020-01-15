package org.swingexplorer.internal;

import java.io.Closeable;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JFrame;

@NotThreadSafe
public class SwingExplorerApplication extends Application implements Closeable {

    @Override
    public void close() {
        if (frmMain != null) {
            final JFrame frmMainCopy = frmMain;
            new Thread(SwingExplorerApplication.class.getSimpleName() + "_DISPOSE") {
                @Override
                public void run() {
                    //prevent deadlock on shutdown
                    frmMainCopy.dispose();
                }
            }.start();
            frmMain = null;
        }
    }

}
