package org.codehaus.mojo.jaxb2.junit;

import org.codehaus.mojo.jaxb2.shared.FileSystemUtilities;
import org.codehaus.mojo.jaxb2.shared.Validate;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.junit.runner.Description;

import java.io.File;
import java.net.URL;

/**
 * The PlexusContainerRule is a rewritten version of the standard Plexus TestCase, with
 * jUnit4+ mechanics to reuse the Plexus container integration.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see <a href="https://github.com/junit-team/junit/wiki/Rules">jUnit Wiki on Rules</a>
 * @since 2.0
 */
public class PlexusContainerRule extends AbstractBeforeAfterRule {

    /**
     * Property containing the plexus home directory.
     */
    public static final String PLEXUS_HOME = "plexus.home";

    /**
     * Default resource path to the plexus home directory.
     */
    public static final String DEFAULT_PLEXUS_HOME = "target/plexus-home";

    // Internal state
    private PlexusContainer container;
    private ContainerConfiguration config;
    private URL configurationURL;

    /**
     * Default constructor, which creates a PlexusContainerRule which reads configuration from the default location,
     * and acquires a standard basedir using the {@code getBasedir()} method.
     */
    public PlexusContainerRule() {
    }

    /**
     * Compound constructor creating a PlexusContainerRule with the supplied basedir
     * property and PlexucContainer configurationURL.
     *
     * @param basedir          A non-empty path to a directory to be used as basedir.
     * @param configurationURL A non-null Plexus Configuration URL.
     */
    public PlexusContainerRule(final String basedir, final URL configurationURL) {

        // Delegate
        super(basedir);

        // Check sanity
        Validate.notNull(configurationURL, "Cannot handle null configurationURL argument.");

        // Assign internal state
        this.configurationURL = configurationURL;
    }

    /**
     * <p>Creates the PlexusContainer instance and initializes it using a custom/constructor-provided
     * configuration resource path or the default one, retrieved by a call to {@code getDefaultConfigurationURL()}.</p>
     * <p>A default configuration file is found by the resource path to the (non-inner) class, followed by ".xml".
     * The table below shows some examples:</p>
     * <p/>
     * <table>
     * <tr>
     * <th>Class</th>
     * <th>Default Plexus Configuration Resource Path</th>
     * </tr>
     * <tr>
     * <td>com.foo.Bar</td>
     * <td>com/foo/Bar.xml</td>
     * </tr>
     * <tr>
     * <td>NoPackageClass</td>
     * <td>NoPackageClass.xml</td>
     * </tr>
     * <tr>
     * <td>com.foo.Gnat$Blah</td>
     * <td>com/foo/Gnat.xml</td>
     * </tr>
     * </table>
     *
     * @param description The test rule Description.
     */
    public void before(final Description description) throws Throwable {

        // Check sanity
        final Class<?> testClass = description.getTestClass();
        Validate.notNull(testClass, "Cannot handle null 'description.getTestClass()' value.");

        // Configure and create the Container
        final URL configURL = this.configurationURL == null
                ? getDefaultConfigurationURL(testClass)
                : this.configurationURL;
        config = new DefaultContainerConfiguration();
        config.setContainerConfigurationURL(configURL);

        container = new DefaultPlexusContainer(config);
        container.getContext().put(BASEDIR, getBasedir());

        // Configure the Plexus context
        final Context context = container.getContext();
        if (!context.contains(PLEXUS_HOME)) {
            File plexusHomeDir = FileSystemUtilities.getCanonicalFile(new File(getBasedir(), DEFAULT_PLEXUS_HOME));
            if (!plexusHomeDir.isDirectory()) {
                plexusHomeDir.mkdirs();
            }

            context.put(PLEXUS_HOME, plexusHomeDir.getAbsolutePath());
        }

        // Initialize and start the Plexus Container.
        // container.initialize();
        // container.start();
    }

    /**
     * Disposes and closes the container after a test.
     *
     * @param description The test rule Description.
     */
    public void after(final Description description) {

        container.dispose();
        container = null;
    }

    /**
     * @return The active PlexusContainer.
     */
    public PlexusContainer getContainer() {
        return container;
    }

    //
    // Private helpers
    //

    private URL getDefaultConfigurationURL(final Class<?> testClass) {

        // Find the configuration resource, which is the same as the
        // resource path to the non-inner class + ".xml"
        final String tmp = testClass.getName().replace('.', '/');
        final String configPath = tmp.substring(0, tmp.indexOf("$")) + ".xml";

        // All done.
        return testClass.getClassLoader().getResource(configPath);
    }
}
