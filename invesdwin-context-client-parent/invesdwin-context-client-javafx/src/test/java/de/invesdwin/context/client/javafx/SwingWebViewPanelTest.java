package de.invesdwin.context.client.javafx;

import java.awt.Dimension;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JFrame;

import org.junit.Test;

import com.sun.javafx.application.PlatformImpl;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.client.javafx.component.swing.SwingWebViewPanel;
import de.invesdwin.context.test.ATest;
import de.invesdwin.util.time.fdate.FTimeUnit;

@NotThreadSafe
public class SwingWebViewPanelTest extends ATest {

    @Test
    public void test() throws InterruptedException {
        EventDispatchThreadUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JFrame frame = new JFrame();

                final SwingWebViewPanel panel = new SwingWebViewPanel();
                PlatformImpl.runAndWait(new Runnable() {
                    @Override
                    public void run() {
                        panel.getWebEngine().load("https://google.com");
                    }
                });
                frame.getContentPane().add(panel);

                frame.setMinimumSize(new Dimension(640, 480));
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
        FTimeUnit.YEARS.sleep(1);
    }

}