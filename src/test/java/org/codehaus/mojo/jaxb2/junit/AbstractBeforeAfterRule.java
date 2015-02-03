package org.codehaus.mojo.jaxb2.junit;

import org.codehaus.mojo.jaxb2.shared.Validate;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;

/**
 * Abstract implementation of the BeforeAfterRule jUnit rule lifecycle specification.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractBeforeAfterRule implements BeforeAfterRule {

    /**
     * The basedir property name.
     */
    public static final String BASEDIR = "basedir";

    // Internal state
    private String basedir;

    /**
     * Default constructor which does not assign a pre-defined basedir value.
     */
    public AbstractBeforeAfterRule() {
    }

    /**
     * Compound constructor creating an AbstractBeforeAfterRule with the supplied custom basedir property.
     *
     * @param basedir A non-empty path to a directory to be used as basedir.
     */
    public AbstractBeforeAfterRule(final String basedir) {

        // Check sanity
        Validate.notEmpty(basedir, "Cannot handle null or empty basedir argument.");

        // Assign internal state
        this.basedir = basedir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {

                try {
                    // Pass the description to the before method to
                    // cater for finding the standard Plexus configuration.
                    before(description);

                    // Evaluate the test itself.
                    base.evaluate();

                } finally {

                    // Always clean up the PlexusContainer.
                    after(description);
                }
            }
        };
    }

    /**
     * @return The base directory of the test project where the test is being run.
     */
    public String getBasedir() {

        if (basedir != null) {
            return basedir;
        }

        // Use the system property if available.
        String toReturn = System.getProperty(BASEDIR);
        if (toReturn == null) {
            toReturn = new File("").getAbsolutePath();
        }
        return toReturn;
    }
}
