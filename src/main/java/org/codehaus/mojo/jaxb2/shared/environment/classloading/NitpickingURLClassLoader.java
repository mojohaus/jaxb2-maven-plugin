package org.codehaus.mojo.jaxb2.shared.environment.classloading;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.jaxb2.shared.Validate;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * The SchemaGen expects to find a ThreadContext URLClassLoader to do its work properly.
 * SchemaGen also stuffs extra JARs i the ClassLoader to do its work.
 * This clashes with Maven's ClassRealm ClassLoaders, since SchemaGen assumes that it is not executed using ClassWorlds.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NitpickingURLClassLoader extends URLClassLoader {

    // Internal state
    // private static final String RUNNER_CLASS = "com.sun.tools.jxc.SchemaGenerator$Runner";
    private Log log;

    /**
     * <p>Constructs a new NitpickingURLClassLoader for the given URLs.
     * The URLs will be searched in the order specified
     * for classes and resources after first searching in
     * the specified parent class loader.</p>
     * <p>If there is a security manager, this method first
     * calls the security manager's {@code checkCreateClassLoader} method
     * to ensure creation of a class loader is allowed.</p>
     *
     * @param urls   the URLs from which to load classes and resources
     * @param parent the parent class loader for delegation
     * @throws SecurityException    if a security manager exists and its
     *                              {@code checkCreateClassLoader} method doesn't allow
     *                              creation of a class loader.
     * @throws NullPointerException if {@code urls} is {@code null}.
     * @see SecurityManager#checkCreateClassLoader
     */
    public NitpickingURLClassLoader(final URL[] urls,
                                    final ClassLoader parent,
                                    final Log log) {

        // Delegate
        super(urls, parent);

        // Check sanity
        Validate.notNull(log, "log");

        // Assign internal state
        this.log = log;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL[] getURLs() {

        final URL[] toReturn = super.getURLs();

        if(log.isDebugEnabled()) {
            final int size = toReturn.length;
            log.debug(" === NitpickingURLClassLoader [" + size + "] URLs ===");
            for(int i = 0; i < size; i++) {
                log.debug(" [" + (i+1) + "/" + size + "]: " + toReturn[i].toExternalForm());
            }
        }

        // All done
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {

        final StackTraceElement[] elements = new Exception().getStackTrace();
        if(elements.length > 2) {
            log.debug("Loading class [" + name + "] from [" + elements[1] + "]");
        }

        // Delegate
        return super.loadClass(name);
    }
}
