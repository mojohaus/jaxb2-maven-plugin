package org.codehaus.mojo.jaxb2.shared.classloader;

import org.codehaus.mojo.jaxb2.BufferingLog;
import org.codehaus.mojo.jaxb2.shared.FileSystemUtilities;
import org.codehaus.mojo.jaxb2.shared.JavaVersion;
import org.codehaus.mojo.jaxb2.shared.environment.classloading.ThreadContextClassLoaderBuilder;
import org.codehaus.mojo.jaxb2.shared.environment.classloading.ThreadContextClassLoaderHolder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ThreadContextClassLoaderBuilderTest {

    // Shared state
    private ThreadContextClassLoaderHolder holder;
    private URL extraClassLoaderDirURL;
    private File extraClassLoaderDirFile;
    private BufferingLog log;
    private ClassLoader originalClassLoader;
    private String encoding = "UTF-8";

    @Before
    public void setupSharedState() {

        log = new BufferingLog(BufferingLog.LogLevel.DEBUG);

        final String extraPath = "testdata/shared/classloader";
        extraClassLoaderDirURL = getClass().getClassLoader().getResource(extraPath);
        Assert.assertNotNull(extraClassLoaderDirURL);

        extraClassLoaderDirFile = new File(extraClassLoaderDirURL.getPath());
        Assert.assertTrue(extraClassLoaderDirFile.exists() && extraClassLoaderDirFile.isDirectory());

        // Stash the original ClassLoader
        originalClassLoader = Thread.currentThread().getContextClassLoader();
    }

    @After
    public void teardownSharedState() {
        if (holder != null) {
            holder.restoreClassLoaderAndReleaseThread();
        } else {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    @Test
    public void validateAddingURLsToThreadContextClassLoader() throws Exception {

        // Assemble
        final int numExpectedResources = JavaVersion.isJdk8OrLower() ? 3 : 6;
        holder = ThreadContextClassLoaderBuilder
                .createFor(originalClassLoader, log, encoding)
                .addURL(extraClassLoaderDirURL)
                .buildAndSet();

        // Act
        final ClassLoader ctxClassLoader = Thread.currentThread().getContextClassLoader();
        final List<URL> resources = Collections.list(ctxClassLoader.getResources(""));

        for(int i = 0; i < resources.size(); i++) {
            System.out.println(" Resource [" + i + "]: " + resources.get(i).toString());
        }

        // Assert
        Assert.assertEquals("Expected [" + numExpectedResources + "] resources but got ["
                        + resources.size() + "]: " + getCommaSeparated(resources),
                numExpectedResources, resources.size());
        // validateContains(resources, "target/classes");
        validateContains(resources, "target/test-classes");
        validateContains(resources, "target/test-classes/testdata/shared/classloader");

        for (URL current : resources) {

            if(current.getProtocol().equalsIgnoreCase("file")) {

                final File aFile = new File(current.getPath());
                Assert.assertTrue(aFile.exists() && aFile.isDirectory());

            } else if (current.getProtocol().equalsIgnoreCase("jar")) {

                // This happens in JDK 9
                Assert.assertTrue(current.toString().contains("!/META-INF/versions/"));
            }
        }
    }

    @Test
    public void validateResourceAccessInAugmentedClassLoader() {

        // Assemble
        holder = ThreadContextClassLoaderBuilder
                .createFor(originalClassLoader, log, encoding)
                .addURL(extraClassLoaderDirURL)
                .buildAndSet();

        // Act
        final ClassLoader ctxClassLoader = Thread.currentThread().getContextClassLoader();

        final URL immediateResource = ctxClassLoader.getResource("ImmediateTestResource.txt");
        final URL subResource = ctxClassLoader.getResource("subdirectory/SubdirectoryTestResource.txt");

        // Assert
        Assert.assertNotNull(immediateResource);
        Assert.assertNotNull(subResource);

        final File immediateFile = new File(immediateResource.getPath());
        final File subFile = new File(subResource.getPath());

        Assert.assertTrue(immediateFile.exists() && immediateFile.isFile());
        Assert.assertTrue(subFile.exists() && subFile.isFile());
    }

    @Test
    public void validateLoadingResourcesInJars() {

        // Assemble
        final File theJar = new File(extraClassLoaderDirFile, "jarSubDirectory/aJarWithResources.jar");

        holder = ThreadContextClassLoaderBuilder
                .createFor(originalClassLoader, log, encoding)
                .addPath(FileSystemUtilities.getCanonicalPath(theJar))
                .buildAndSet();

        // Act
        final ClassLoader ctxClassLoader = Thread.currentThread().getContextClassLoader();

        final URL containedTopLevelResource = ctxClassLoader.getResource("ContainedFileResource.txt");
        final URL containedSubLevelResource = ctxClassLoader.getResource("internalSubDir/SubContainedResource.txt");

        // Assert
        Assert.assertNotNull(containedTopLevelResource);
        Assert.assertNotNull(containedSubLevelResource);
    }

    //
    // Private helpers
    //

    private String getCommaSeparated(final List<URL> resources) {

        final StringBuilder toReturn = new StringBuilder();

        for(URL current : resources) {
            toReturn.append(current.toString()).append(", ");
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

        Assert.fail("Snippet [" + snippet + "] was not found within URL resources.");
    }
}
