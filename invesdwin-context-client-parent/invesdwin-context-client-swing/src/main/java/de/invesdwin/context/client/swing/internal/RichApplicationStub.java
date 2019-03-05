package de.invesdwin.context.client.swing.internal;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import de.invesdwin.context.client.swing.api.guiservice.ContentPane;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.api.guiservice.StatusBar;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.test.stub.StubSupport;

@ThreadSafe
@Named
public class RichApplicationStub extends StubSupport {

    @Inject
    private StatusBar statusBar;
    @Inject
    private ContentPane contentPane;

    @Override
    public void tearDown(final ATest test, final TestContext ctx) {
        statusBar.reset();
        contentPane.reset();
        GuiService.get().getTaskService().shutdownNow();
    }

}
