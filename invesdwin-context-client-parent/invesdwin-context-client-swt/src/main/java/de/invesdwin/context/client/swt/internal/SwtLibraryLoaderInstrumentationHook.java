package de.invesdwin.context.client.swt.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.commons.io.FileUtils;

import de.invesdwin.context.beans.hook.IInstrumentationHook;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.instrument.DynamicInstrumentationProperties;
import de.invesdwin.instrument.DynamicInstrumentationReflections;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.Reflections;

/**
 * https://github.com/jendap/multiplatform-swt
 * 
 * @author subes
 *
 */
@NotThreadSafe
public class SwtLibraryLoaderInstrumentationHook implements IInstrumentationHook {

    private static final String SWT_CLASS = "org.eclipse.swt.SWT";

    @Override
    public void instrument(final Instrumentation instrumentation) {
        if (!Reflections.classExists(SWT_CLASS)) {
            final String jarName = getSwtPlatformDependentJarPath();
            final File destination = new File(DynamicInstrumentationProperties.TEMP_DIRECTORY, jarName);
            try (InputStream source = SwtLibraryLoaderInstrumentationHook.class.getResourceAsStream(jarName)) {
                FileUtils.copyInputStreamToFile(source, destination);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            DynamicInstrumentationReflections.addPathToSystemClassLoader(destination);
            Assertions.checkTrue(Reflections.classExists(SWT_CLASS));
        }
    }

    private static String getSwtPlatformDependentJarPath() {
        try {
            final String platformString = getSwtFileNameOsSuffix() + getSwtFileArchSuffix();
            return "/swt/org.eclipse.swt" + platformString + ".jar";
        } catch (final Exception e) {
            Err.process(new RuntimeException("Unable to determine platform dependent SWT jar file name.", e));
            return null;
        }
    }

    private static String getSwtFileArchSuffix() {
        //CHECKSTYLE:OFF
        final String osName = System.getProperty("os.name").toLowerCase();
        final String osArch = System.getProperty("os.arch").toLowerCase();
        //CHECKSTYLE:ON

        final String swtFileNameArchSuffix;
        if (osArch.contains("64")) {
            swtFileNameArchSuffix = ".x86_64";
        } else {
            swtFileNameArchSuffix = osName.contains("mac") ? "" : ".x86";
        }
        return swtFileNameArchSuffix;
    }

    private static String getSwtFileNameOsSuffix() {
        //CHECKSTYLE:OFF
        final String osName = System.getProperty("os.name").toLowerCase();
        //CHECKSTYLE:ON
        final String swtFileNameOsPart;
        if (osName.contains("win")) {
            swtFileNameOsPart = ".win32.win32";
        } else if (osName.contains("mac")) {
            swtFileNameOsPart = ".cocoa.macosx";
        } else if (osName.contains("linux") || osName.contains("nix")) {
            swtFileNameOsPart = ".gtk.linux";
        } else {
            throw new RuntimeException("Unsupported OS name: " + osName);
        }
        return swtFileNameOsPart;
    }

}
