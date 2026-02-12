package de.invesdwin.context.client.wicket.maven.plugin;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import de.invesdwin.context.PlatformInitializerProperties;

@NotThreadSafe(/* Threadsafe for maven execution with multiple instances, but not a threadsafe instance */)
public abstract class AGeneratedMarkupMojo extends AbstractMojo {

    /**
     * If true, html files will be generated to src/main/resources and src/test/resources instead of src/main/java and
     * src/test/java respectively.
     * 
     * @parameter default-value="false"
     * @required
     */
    private boolean htmlFilesResideInResourcesDirectory;
    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final ClassLoader contextClassloader = createContextClassloader();
        final File inputDirectory = getJavaDirectory();
        final File outputDirectory;
        if (htmlFilesResideInResourcesDirectory) {
            outputDirectory = getResourcesDirectory();
        } else {
            outputDirectory = inputDirectory;
        }
        try {
            skipInvesdwinIinit();
            final Class<?> clazz = Class.forName(
                    "de.invesdwin.context.client.wicket.generated.markup.AnnotatedGeneratedMarkup", true,
                    contextClassloader);
            final Method method = clazz.getMethod("generate", File.class, File.class);
            method.invoke(null, inputDirectory, outputDirectory);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void skipInvesdwinIinit() {
        //CHECKSTYLE:OFF property
        /*
         * only referencing the constant here, since this will be inlined in bytecode and the class providing the
         * constant will not be needed anymore; it won't be available anyway then here
         */
        System.setProperty(PlatformInitializerProperties.KEY_ALLOWED, Boolean.FALSE.toString());
        //CHECKSTYLE:ON
    }

    /**
     * http://stackoverflow.com/questions/2659048/add-maven-build-classpath-to-plugin-execution-classpath
     * 
     * @return
     */
    private ClassLoader createContextClassloader() {
        try {
            //CHECKSTYLE:OFF
            final Set<String> urls = new HashSet<String>();
            //CHECKSTYLE:ON
            final Set<String> elements = getClasspathElements();
            for (final String element : elements) {
                urls.add(new File(element).toURI().toString());
            }

            final URL[] urlsArray = new URL[urls.size()];
            int i = 0;
            for (final String url : urls) {
                //CHECKSTYLE:OFF
                urlsArray[i] = new URL(url);
                //CHECKSTYLE:ON
                i++;
            }
            final ClassLoader contextClassLoader = URLClassLoader.newInstance(urlsArray,
                    ClassLoader.getSystemClassLoader());

            return contextClassLoader;
        } catch (final DependencyResolutionRequiredException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> getClasspathElements() throws DependencyResolutionRequiredException {
        //CHECKSTYLE:OFF
        final Set<String> classpathElements = new LinkedHashSet<String>();
        //CHECKSTYLE:ON
        classpathElements.addAll(project.getCompileClasspathElements());
        classpathElements.addAll(project.getRuntimeClasspathElements());
        classpathElements.addAll(project.getTestClasspathElements());
        return classpathElements;
    }

    protected abstract File getJavaDirectory();

    protected abstract File getResourcesDirectory();

}
