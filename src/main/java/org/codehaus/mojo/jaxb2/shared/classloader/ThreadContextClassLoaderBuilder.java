package org.codehaus.mojo.jaxb2.shared.classloader;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.jaxb2.shared.Validate;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Utility class which assists in synthesizing a URLClassLoader for use as a ThreadLocal ClassLoader.
 * Typical use:</p>
 * <pre>
 *     <code>
 *         // Create and set the ThreadContext ClassLoader
 *         ThreadContextClassLoaderHolder holder = null;
 *
 *         try {
 *
 *          holder = ThreadContextClassLoaderBuilder.createFor(getClass())
 *              .addPath("some/path")
 *              .addURL(someURL)
 *              .addPaths(aPathList)
 *              .buildAndSet();
 *
 *          // ... perform operations using the newly set ThreadContext ClassLoader...
 *
 *         } finally {
 *          // Restore the original ClassLoader
 *          holder.restoreClassLoaderAndReleaseThread();
 *         }
 *     </code>
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public class ThreadContextClassLoaderBuilder {

    // Internal state
    private ClassLoader originalClassLoader;
    private List<URL> urlList;
    private Log log;

    private ThreadContextClassLoaderBuilder(final ClassLoader classLoader, final Log aLog) {
        log = aLog;
        originalClassLoader = classLoader;
        urlList = new ArrayList<URL>();
    }

    /**
     * Adds the supplied anURL to the list of internal URLs which should be used to build an URLClassLoader.
     * Will only add an URL once, and warns about trying to re-add an URL.
     *
     * @param anURL The URL to add.
     * @return This ThreadContextClassLoaderBuilder, for builder pattern chaining.
     */
    public ThreadContextClassLoaderBuilder addURL(final URL anURL) {

        // Check sanity
        Validate.notNull(anURL, "anURL");

        // Add the segment unless already added.
        for (URL current : urlList) {
            if (current.toString().equalsIgnoreCase(anURL.toString())) {

                if (log.isWarnEnabled()) {
                    log.warn("Not adding URL [" + anURL.toString() + "] twice. Check your plugin configuration.");
                }

                // Don't re-add the supplied URL.
                return this;
            }
        }

        // Add the supplied URL to the urlList
        if (log.isDebugEnabled()) {
            log.debug("Adding URL [" + anURL.toString() + "]");
        }

        //
        // According to the URLClassLoader's documentation:
        // "Any URL that ends with a '/' is assumed to refer to a directory.
        // Otherwise, the URL is assumed to refer to a JAR file which will be downloaded and opened as needed."
        //
        // ... uhm ... instead of using the 'protocol' property of the URL itself?
        //
        // So ... we need to ensure that any file-protocol URLs which point to directories are actually
        // terminated with a '/'. Otherwise the URLClassLoader treats those URLs as JARs - and hence ignores them.
        //
        urlList.add(addSlashToDirectoryUrlIfRequired(anURL));

        return this;
    }

    /**
     * Converts the supplied path to an URL and adds it to this ThreadContextClassLoaderBuilder.
     *
     * @param path A path to convert to an URL and add.
     * @return This ThreadContextClassLoaderBuilder, for builder pattern chaining.
     * @see #addURL(java.net.URL)
     */
    public ThreadContextClassLoaderBuilder addPath(final String path) {

        // Check sanity
        Validate.notEmpty(path, "path");

        // Convert to an URL, and delegate.
        final URL anUrl;
        try {
            anUrl = new File(path).toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not convert path [" + path + "] to an URL.", e);
        }

        // Delegate
        return addURL(anUrl);
    }

    /**
     * Converts the supplied path to an URL and adds it to this ThreadContextClassLoaderBuilder.
     *
     * @param paths A List of path to convert to URLs and add.
     * @return This ThreadContextClassLoaderBuilder, for builder pattern chaining.
     * @see #addPath(String)
     */
    public ThreadContextClassLoaderBuilder addPaths(final List<String> paths) {

        // Check sanity
        Validate.notNull(paths, "paths");

        // Delegate
        for (String path : paths) {
            addPath(path);
        }

        return this;
    }

    /**
     * <p>This method performs 2 things in order:</p>
     * <ol>
     * <li>Builds a ThreadContext ClassLoader from the URLs supplied to this Builder, and assigns the newly built
     * ClassLoader to the current Thread.</li>
     * <li></li>
     * </ol>
     * References to the original ThreadContext
     * ClassLoader and the currentThread are stored within the returned ThreadContextClassLoaderHolder,
     * and can be restored by a call to {@code ThreadContextClassLoaderHolder.restoreClassLoaderAndReleaseThread()}.
     *
     * @return A fully set up ThreadContextClassLoaderHolder which
     */
    public ThreadContextClassLoaderHolder buildAndSet() {

        // Create the URLClassLoader from the supplied URLs
        final URL[] allURLs = new URL[urlList.size()];
        urlList.toArray(allURLs);
        final URLClassLoader classLoader = new URLClassLoader(allURLs, originalClassLoader);

        // Assign the ThreadContext ClassLoader
        final Thread currentThread = Thread.currentThread();
        currentThread.setContextClassLoader(classLoader);

        // Build the classpath argument
        StringBuilder builder = new StringBuilder();
        try {
            for (URL current : Collections.list(classLoader.getResources(""))) {

                final String toAppend = getClassPathElement(current);
                if (toAppend != null) {
                    builder.append(toAppend).append(File.pathSeparator);
                }
            }
        } catch (IOException e) {

            // Restore the original ClassLoader to the active thread before failing.
            currentThread.setContextClassLoader(originalClassLoader);
            throw new IllegalStateException("Could not synthesize classPath from original ClassLoader.", e);
        }

        final String classPathString = builder.length() > 0
                ? builder.toString().substring(0, builder.length() - File.pathSeparator.length())
                : "";

        // All done.
        return new DefaultHolder(currentThread, this.originalClassLoader, classPathString);
    }

    /**
     * Creates a new ThreadContextClassLoaderBuilder using the supplied original classLoader, as well
     * as the supplied Maven Log.
     *
     * @param classLoader The original ClassLoader which should be used as the parent for the ThreadContext
     *                    ClassLoader produced by the ThreadContextClassLoaderBuilder generated by this builder method.
     *                    Cannot be null.
     * @param log         The active Maven Log. Cannot be null.
     * @return A ThreadContextClassLoaderBuilder wrapping the supplied members.
     */
    public static ThreadContextClassLoaderBuilder createFor(final ClassLoader classLoader, final Log log) {

        // Check sanity
        Validate.notNull(classLoader, "classLoader");
        Validate.notNull(log, "log");

        // All done.
        return new ThreadContextClassLoaderBuilder(classLoader, log);
    }

    /**
     * Acquires all URLs corresponding to the resource path "" within the supplied ClassLoader.
     *
     * @param classLoader A non-null ClassLoader.
     * @return all URLs corresponding to the resource path "" within the supplied ClassLoader. This corresponds to
     * the classPath of the supplied ClassLoader.
     * @throws java.lang.IllegalStateException if the {@code classLoader.getResources("")} call fails.
     */
    public static List<URL> getRootResources(final ClassLoader classLoader) throws IllegalStateException {

        // Check sanity
        Validate.notNull(classLoader, "classLoader");

        final List<URL> toReturn = new ArrayList<URL>();

        try {
            toReturn.addAll(Collections.list(classLoader.getResources("")));
        } catch (IOException e) {
            throw new IllegalStateException("Could not synthesize classPath from original ClassLoader.", e);
        }

        // All done.
        return toReturn;
    }

    /**
     * Creates a new ThreadContextClassLoaderBuilder using the original ClassLoader from the supplied Class, as well
     * as the given Maven Log.
     *
     * @param aClass A non-null class from which to extract the original ClassLoader.
     * @param log    The active Maven Log. Cannot be null.
     * @return A ThreadContextClassLoaderBuilder wrapping the supplied members.
     */
    public static ThreadContextClassLoaderBuilder createFor(final Class<?> aClass, final Log log) {

        // Check sanity
        Validate.notNull(aClass, "aClass");

        // Delegate
        return createFor(aClass.getClassLoader(), log);
    }

    /**
     * Converts the supplied URL to a class path element.
     *
     * @param anURL The non-null URL for which to acquire a classPath element.
     * @return The full (i.e. non-chopped) classpath element corresponding to the supplied URL.
     * @throws java.lang.IllegalArgumentException if the supplied URL had an unknown protocol.
     */
    public static String getClassPathElement(final URL anURL) throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(anURL, "anURL");

        final String protocol = anURL.getProtocol();
        String toReturn = null;

        if ("file".equalsIgnoreCase(protocol)) {
            toReturn = anURL.getPath();
        } else if ("jar".equalsIgnoreCase(protocol)) {
            toReturn = anURL.getPath();
        } else if ("http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol)) {
            toReturn = anURL.toString();
        } else {
            throw new IllegalArgumentException("Unknown protocol [" + protocol + "]; could not handle URL ["
                    + anURL + "]");
        }

        return toReturn;
    }

    //
    // Private helpers
    //

    private URL addSlashToDirectoryUrlIfRequired(final URL anURL) {

        // Check sanity
        Validate.notNull(anURL, "anURL");

        URL toReturn = anURL;
        if ("file".equalsIgnoreCase(anURL.getProtocol())) {

            final File theFile = new File(anURL.getPath());
            if (theFile.isDirectory()) {
                try {

                    // This ensures that an URL pointing to a File directory
                    // actually is terminated by a '/', which is required by
                    // the URLClassLoader to operate properly.
                    toReturn = theFile.toURI().toURL();
                } catch (MalformedURLException e) {
                    // This should never happen
                    throw new IllegalArgumentException("Could not convert a File to an URL", e);
                }
            }
        }

        // All done.
        return toReturn;
    }

    /**
     * Default implementation of the ThreadContextClassLoaderCleaner specification,
     * with added finalizer to ensure we release the Thread reference no matter
     * what happens with any DefaultCleaner objects.
     */
    class DefaultHolder implements ThreadContextClassLoaderHolder {

        // Internal state
        private Thread affectedThread;
        private ClassLoader originalClassLoader;
        private String classPathArgument;

        public DefaultHolder(final Thread affectedThread,
                             final ClassLoader originalClassLoader,
                             final String classPathArgument) {

            // Check sanity
            Validate.notNull(affectedThread, "affectedThread");
            Validate.notNull(originalClassLoader, "originalClassLoader");
            Validate.notNull(classPathArgument, "classPathArgument");

            // Assign internal state
            this.affectedThread = affectedThread;
            this.originalClassLoader = originalClassLoader;
            this.classPathArgument = classPathArgument;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void restoreClassLoaderAndReleaseThread() {
            if (affectedThread != null) {

                // Restore original state
                affectedThread.setContextClassLoader(originalClassLoader);

                // Null out the internal state
                affectedThread = null;
                originalClassLoader = null;
                classPathArgument = null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getClassPathAsArgument() {
            return classPathArgument;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void finalize() throws Throwable {
            try {
                // First, release all resources held by this object.
                restoreClassLoaderAndReleaseThread();
            } finally {
                // Now, perform standard finalization.
                super.finalize();
            }
        }
    }
}
