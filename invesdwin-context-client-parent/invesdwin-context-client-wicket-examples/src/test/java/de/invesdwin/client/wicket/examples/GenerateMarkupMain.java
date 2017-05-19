package de.invesdwin.client.wicket.examples;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.common.client.wicket.generated.markup.AnnotatedGeneratedMarkup;
import de.invesdwin.context.PlatformInitializerProperties;

@NotThreadSafe
public final class GenerateMarkupMain {

    private GenerateMarkupMain() {}

    public static void main(final String[] args) {
        PlatformInitializerProperties.setAllowed(false);
        new AnnotatedGeneratedMarkup().generate();
    }

}
