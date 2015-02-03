package org.codehaus.mojo.jaxb2.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;

/**
 * Specification for jUnit rules requiring actions to be taken before the tests start, as
 * well as after the tests have completed.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface BeforeAfterRule extends TestRule {

    /**
     * Lifecycle method that is invoked before the tests are being executed.
     *
     * @param description The jUnit rule Description.
     * @throws Throwable if anything went exceptionally wrong during setup.
     */
    public void before(final Description description) throws Throwable;

    /**
     * Lifecycle method that is invoked after the tests have been executed.
     *
     * @param description The jUnit rule Description.
     */
    public void after(final Description description);
}
