package de.invesdwin.common.client.swing;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.assertj.swing.fixture.JTextComponentFixture;
import org.junit.Test;

import de.invesdwin.common.client.swing.test.RichApplicationStub;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.util.assertions.Assertions;

@ThreadSafe
public class TestViewTest extends ATest {

    @Inject
    private TestView view;
    @Inject
    private ContentPane contentPane;
    @Inject
    private RichApplicationStub richApplicationStub;

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.activate(TestView.class);
        ctx.activate(TestRichApplication.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        contentPane.addView(view);
    }

    @Test
    public void testBeanBinding() {
        final JTextComponentFixture tf_name = richApplicationStub.getFrameFixture().textBox("name");
        Assertions.assertThat(tf_name.text()).isEqualTo(view.getModel().getName());
        view.getModel().setName("newName");
        Assertions.assertThat(tf_name.text()).isEqualTo(view.getModel().getName());
    }

    @Test
    public void testBeanBindingAgain() {
        final JTextComponentFixture tf_name = richApplicationStub.getFrameFixture().textBox("name");
        Assertions.assertThat(tf_name.text()).isEqualTo(view.getModel().getName());
        view.getModel().setName("newName2");
        Assertions.assertThat(tf_name.text()).isEqualTo(view.getModel().getName());
    }

}
