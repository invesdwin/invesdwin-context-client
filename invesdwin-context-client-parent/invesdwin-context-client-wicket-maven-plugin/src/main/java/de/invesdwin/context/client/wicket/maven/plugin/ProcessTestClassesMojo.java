package de.invesdwin.context.client.wicket.maven.plugin;

import java.io.File;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * @phase process-test-classes
 * @goal process-test-classes
 * @threadSafe true
 * @requiresDependencyResolution test
 */
@NotThreadSafe(/* Threadsafe for maven execution with multiple instances, but not a threadsafe instance */)
public class ProcessTestClassesMojo extends AGeneratedMarkupMojo {

    @Override
    protected File getJavaDirectory() {
        return new File("src/test/java");
    }

    @Override
    protected File getResourcesDirectory() {
        return new File("src/test/resources");
    }

}
