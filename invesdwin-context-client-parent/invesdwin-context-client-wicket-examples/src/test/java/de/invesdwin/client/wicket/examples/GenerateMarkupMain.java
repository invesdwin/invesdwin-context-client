package de.invesdwin.client.wicket.examples;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.PlatformInitializerProperties;
import de.invesdwin.context.client.wicket.generated.markup.AnnotatedGeneratedMarkup;

@NotThreadSafe
public final class GenerateMarkupMain {

    private GenerateMarkupMain() {}

    public static void main(final String[] args) {
        PlatformInitializerProperties.setAllowed(false);
        new AnnotatedGeneratedMarkup().generate();
    }

}
