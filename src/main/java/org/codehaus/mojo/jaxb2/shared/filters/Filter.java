package org.codehaus.mojo.jaxb2.shared.filters;

import org.apache.maven.plugin.logging.Log;

/**
 * Generic Filter specification, whose implementations define if candidate objects should be accepted or not.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public interface Filter<T> {

    /**
     * Initializes this Filter, and assigns the supplied Log for use by this Filter.
     *
     * @param log The non-null Log which should be used by this Filter to emit log messages.
     */
    void initialize(Log log);

    /**
     * <p>Method that is invoked to determine if a candidate instance should be accepted or not.
     * Implementing classes should be prepared to handle {@code null} candidate objects.</p>
     *
     * @param candidate The candidate that should be tested for acceptance by this Filter.
     * @return {@code true} if the candidate is accepted by this Filter and {@code false} otherwise.
     * @throws java.lang.IllegalStateException if this Filter is not initialized by a call to the
     *                                         initialize method before calling this matchAtLeastOnce method.
     */
    boolean accept(T candidate) throws IllegalStateException;
}
