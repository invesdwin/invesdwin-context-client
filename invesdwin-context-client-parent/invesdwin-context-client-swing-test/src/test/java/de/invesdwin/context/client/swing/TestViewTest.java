package de.invesdwin.context.client.swing;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.assertj.swing.fixture.JTextComponentFixture;
import org.junit.Test;

import de.invesdwin.context.client.swing.api.guiservice.ContentPane;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.test.FrameFixtureStub;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.util.assertions.Assertions;

@ThreadSafe
public class TestViewTest extends ATest {

    private TestView view;
    @Inject
    private ContentPane contentPane;
    @Inject
    private FrameFixtureStub frameFixtureTestStub;

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.activate(TestRichApplication.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        view = new TestView();
        contentPane.showView(view);
    }

    @Test
    public void testBeanBinding() {
        final JTextComponentFixture tf_name = frameFixtureTestStub.getFrameFixture().textBox("name");
        Assertions.assertThat(tf_name.text()).isEqualTo(view.getModel().getName());
        view.getModel().setName("newName");
        GuiService.get().updateAllViews(view);
        Assertions.assertThat(tf_name.text()).isEqualTo(view.getModel().getName());
    }

    @Test
    public void testBeanBindingAgain() {
        final JTextComponentFixture tf_name = frameFixtureTestStub.getFrameFixture().textBox("name");
        Assertions.assertThat(tf_name.text()).isEqualTo(view.getModel().getName());
        view.getModel().setName("newName2");
        GuiService.get().updateAllViews(view);
        Assertions.assertThat(tf_name.text()).isEqualTo(view.getModel().getName());
    }

}
