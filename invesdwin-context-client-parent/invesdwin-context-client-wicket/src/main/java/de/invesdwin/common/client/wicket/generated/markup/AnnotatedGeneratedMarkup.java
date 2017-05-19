package de.invesdwin.common.client.wicket.generated.markup;

import java.io.File;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.ContextProperties;
import de.invesdwin.nowicket.generated.markup.AAnnotatedGeneratedMarkup;

@NotThreadSafe
public class AnnotatedGeneratedMarkup extends AAnnotatedGeneratedMarkup {

    public AnnotatedGeneratedMarkup(final File... possibleDirectories) {
        super(possibleDirectories);
    }

    public AnnotatedGeneratedMarkup() {
        super();
    }

    /**
     * Utility method that can easily be executed via reflection in maven plugin.
     */
    public static void generate(final File inputDirectory, final File outputDirectory) {
        new AnnotatedGeneratedMarkup(inputDirectory) {
            @Override
            protected File redirectDestination(final File sourceDirectory) {
                return outputDirectory;
            }
        }.generate();
    }

    @Override
    protected Set<String> getClasspathBasePackages() {
        return ContextProperties.getBasePackages();
    }
}
