package org.codehaus.mojo.jaxb2.shared.classloader;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.mojo.jaxb2.BufferingLog;
import org.codehaus.mojo.jaxb2.shared.FileSystemUtilities;
import org.codehaus.mojo.jaxb2.shared.JavaVersion;
import org.codehaus.mojo.jaxb2.shared.environment.classloading.ThreadContextClassLoaderBuilder;
import org.codehaus.mojo.jaxb2.shared.environment.classloading.ThreadContextClassLoaderHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
class ThreadContextClassLoaderBuilderTest {
    // Shared state
    private ThreadContextClassLoaderHolder holder;
    private URL extraClassLoaderDirURL;
    private File extraClassLoaderDirFile;
    private BufferingLog log;
    private ClassLoader originalClassLoader;
    private String encoding = "UTF-8";

    @BeforeEach
    void setupSharedState() {

        log = new BufferingLog(BufferingLog.LogLevel.DEBUG);

        final String extraPath = "testdata/shared/classloader";
        extraClassLoaderDirURL = getClass().getClassLoader().getResource(extraPath);
        assertNotNull(extraClassLoaderDirURL);

        extraClassLoaderDirFile = new File(extraClassLoaderDirURL.getPath());
        assertTrue(extraClassLoaderDirFile.exists() && extraClassLoaderDirFile.isDirectory());

        // Stash the original ClassLoader
        originalClassLoader = Thread.currentThread().getContextClassLoader();
    }

    @AfterEach
    void teardownSharedState() {
        if (holder != null) {
            holder.restoreClassLoaderAndReleaseThread();
        } else {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    @Test
    @Disabled
    void validateAddingURLsToThreadContextClassLoader() throws Exception {

        // Assemble
        final int numExpectedResources = JavaVersion.isJdk8OrLower() ? 3 : 6;
        holder = ThreadContextClassLoaderBuilder.createFor(originalClassLoader, log, encoding)
                .addURL(extraClassLoaderDirURL)
                .buildAndSet();

        // Act
        final ClassLoader ctxClassLoader = Thread.currentThread().getContextClassLoader();
        final List<URL> resources = Collections.list(ctxClassLoader.getResources(""));

        for (int i = 0; i < resources.size(); i++) {
            System.out.println(" Resource [" + i + "]: " + resources.get(i).toString());
        }

        /*
         * Expected result for JDK 8:
         * ==========================
         * Resource [0]: file:[pathToJaxbProject]/target/test-classes/
         * Resource [1]: file:[pathToJaxbProject]/target/classes/
         * Resource [2]: file:[pathToJaxbProject]/target/test-classes/testdata/shared/classloader/
         *
         * Expected result for JDK 11 (assuming jakarta.xml.bind-api version 2.3.2):
         * =========================================================================
         * Resource [0]: file:[pathToJaxbProject]/target/test-classes/
         * Resource [1]: file:[pathToJaxbProject]/target/classes/
         * Resource [2]: jar:file:[pathToM2Repo]/jakarta/xml/bind/jakarta.xml.bind-api/2.3.2/jakarta.xml.bind-api-2.3.2.jar!/META-INF/versions/9/
         * Resource [3]: jar:file:[pathToM2Repo]/repository/org/glassfish/jaxb/jaxb-xjc/2.3.2/jaxb-xjc-2.3.2.jar!/META-INF/versions/9/
         * Resource [4]: jar:file:[pathToM2Repo]/repository/org/glassfish/jaxb/jaxb-jxc/2.3.2/jaxb-jxc-2.3.2.jar!/META-INF/versions/9/
         * Resource [5]: file:[pathToJaxbProject]/target/test-classes/testdata/shared/classloader/
         */

        /*
         * [3]: jar:file:/Users/lj/.m2/repository/jakarta/xml/bind/jakarta.xml.bind-api/2.3.2/jakarta.xml.bind-api-2.3.2.jar!/META-INF/versions/9/
         * [5]: jar:file:/Users/lj/.m2/repository/javax/xml/bind/jaxb-api/2.3.0/jaxb-api-2.3.0.jar!/META-INF/versions/9/
         */

        // Assert
        assertEquals(
                numExpectedResources,
                resources.size(),
                "Expected [" + numExpectedResources + "] resources but got [" + resources.size() + "]: "
                        + getNewLineSeparated(resources));
        // validateContains(resources, "target/classes");
        validateContains(resources, "target/test-classes");
        validateContains(resources, "target/test-classes/testdata/shared/classloader");

        for (URL current : resources) {

            if (current.getProtocol().equalsIgnoreCase("file")) {

                final File aFile = new File(current.getPath());
                assertTrue(aFile.exists() && aFile.isDirectory());

            } else if (current.getProtocol().equalsIgnoreCase("jar")) {

                // This happens in JDK 9
                assertTrue(current.toString().contains("!/META-INF/versions/"));
            }
        }
    }

    @Test
    void validateResourceAccessInAugmentedClassLoader() {

        // Assemble
        holder = ThreadContextClassLoaderBuilder.createFor(originalClassLoader, log, encoding)
                .addURL(extraClassLoaderDirURL)
                .buildAndSet();

        // Act
        final ClassLoader ctxClassLoader = Thread.currentThread().getContextClassLoader();

        final URL immediateResource = ctxClassLoader.getResource("ImmediateTestResource.txt");
        final URL subResource = ctxClassLoader.getResource("subdirectory/SubdirectoryTestResource.txt");

        // Assert
        assertNotNull(immediateResource);
        assertNotNull(subResource);

        final File immediateFile = new File(immediateResource.getPath());
        final File subFile = new File(subResource.getPath());

        assertTrue(immediateFile.exists() && immediateFile.isFile());
        assertTrue(subFile.exists() && subFile.isFile());
    }

    @Test
    void validateLoadingResourcesInJars() {

        // Assemble
        final File theJar = new File(extraClassLoaderDirFile, "jarSubDirectory/aJarWithResources.jar");

        holder = ThreadContextClassLoaderBuilder.createFor(originalClassLoader, log, encoding)
                .addPath(FileSystemUtilities.getCanonicalPath(theJar))
                .buildAndSet();

        // Act
        final ClassLoader ctxClassLoader = Thread.currentThread().getContextClassLoader();

        final URL containedTopLevelResource = ctxClassLoader.getResource("ContainedFileResource.txt");
        final URL containedSubLevelResource = ctxClassLoader.getResource("internalSubDir/SubContainedResource.txt");

        // Assert
        assertNotNull(containedTopLevelResource);
        assertNotNull(containedSubLevelResource);
    }

    //
    // Private helpers
    //

    private String getNewLineSeparated(final List<URL> resources) {

        final StringBuilder toReturn = new StringBuilder();

        final AtomicInteger index = new AtomicInteger();
        for (URL current : resources) {
            final String urlIndex = "[" + index.getAndIncrement() + "]: ";
            toReturn.append(urlIndex).append(current.toString()).append("\n");
        }

        // All Done.
        final String fullString = toReturn.toString();
        return fullString.substring(0, fullString.length() - 2);
    }

    private void validateContains(final List<URL> resources, final String snippet) {

        for (URL current : resources) {
            if (current.toString().contains(snippet)) {
                return;
            }
        }

        fail("Snippet [" + snippet + "] was not found within URL resources.");
    }
}
