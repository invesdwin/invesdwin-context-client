package de.invesdwin.common.client.wicket.internal;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;

import org.apache.wicket.markup.html.basic.Label;
import org.springframework.beans.factory.annotation.Configurable;

import de.invesdwin.util.assertions.Assertions;

@Configurable
@NotThreadSafe
public class SimpleHomePage extends org.apache.wicket.markup.html.WebPage {

    public static final String SUCCESS_MESSAGE = "If you can read this message, Wicket is configured correctly!";

    private static final long serialVersionUID = 1L;

    @Inject
    private transient WicketTestBean bean;

    /**
     * Constructor that is invoked when page is invoked without a session.
     */
    public SimpleHomePage() {
        add(new Label("message", SUCCESS_MESSAGE));
    }

    @Override
    protected void onBeforeRender() {
        testDependencyInjection();
        super.onBeforeRender();
    }

    public void testDependencyInjection() {
        Assertions.assertThat(bean).isNotNull();
    }
}
