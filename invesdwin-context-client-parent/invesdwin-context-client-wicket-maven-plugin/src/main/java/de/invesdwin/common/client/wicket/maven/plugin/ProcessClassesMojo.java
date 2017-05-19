package de.invesdwin.common.client.wicket.maven.plugin;

import java.io.File;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * @phase process-classes
 * @goal process-classes
 * @threadSafe true
 * @requiresDependencyResolution test
 */
@NotThreadSafe(/* Threadsafe for maven execution with multiple instances, but not a threadsafe instance */)
public class ProcessClassesMojo extends AGeneratedMarkupMojo {

    @Override
    protected File getJavaDirectory() {
        return new File("src/main/java");
    }

    @Override
    protected File getResourcesDirectory() {
        return new File("src/main/resources");
    }

}
