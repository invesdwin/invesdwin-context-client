package de.invesdwin.context.client.swing.layout;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import de.invesdwin.context.client.swing.api.guiservice.ContentPane;
import de.invesdwin.context.client.swing.api.guiservice.PersistentLayoutManager;
import de.invesdwin.context.client.swing.frame.RichApplicationProperties;
import de.invesdwin.context.client.swing.frame.content.WorkingAreaLocation;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.util.lang.Files;
import jakarta.inject.Inject;

@ThreadSafe
public class InteractiveTestSaveRestoreLayoutRichApplication extends ATest {

    @Inject
    private ContentPane contentPane;

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.activateBean(TestSaveRestoreLayoutRichApplication.class);
        final File storageDirectory = RichApplicationProperties.getStorageDirectory();
        Files.forceMkdir(storageDirectory);
        IOUtils.copy(getClass().getResource(PersistentLayoutManager.LAYOUT_FILE_NAME),
                new FileOutputStream(new File(storageDirectory, PersistentLayoutManager.LAYOUT_FILE_NAME)));
    }

    @Override
    public void setUpOnce() throws Exception {
        super.setUpOnce();

        contentPane.showView(new TestSaveRestoreLayoutModelView(new TestSaveRestoreLayoutModel("ChartView")),
                WorkingAreaLocation.Center);
        contentPane.showView(new TestSaveRestoreLayoutModelView(new TestSaveRestoreLayoutModel("POI-Orders")),
                WorkingAreaLocation.East);
        contentPane.showView(new TestSaveRestoreLayoutModelView(new TestSaveRestoreLayoutModel("POI")),
                WorkingAreaLocation.East);
        contentPane.showView(new TestSaveRestoreLayoutModelView(new TestSaveRestoreLayoutModel("PositionManager")),
                WorkingAreaLocation.South);
        contentPane.showView(new TestSaveRestoreLayoutModelView(new TestSaveRestoreLayoutModel("TradeAlerts")),
                WorkingAreaLocation.South);

    }

    @Test
    public void testRichApplication() throws InterruptedException {
        TimeUnit.DAYS.sleep(Long.MAX_VALUE);
    }

}
