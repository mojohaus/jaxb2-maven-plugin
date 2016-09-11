package org.codehaus.mojo.jaxb2.shared.filters;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.jaxb2.AbstractJaxbMojo;
import org.codehaus.mojo.jaxb2.shared.Validate;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Filter implementation which handles separating {@code null} candidate values from non-null
 * ones, and delegates processing to concrete subclass implementations. Also, this AbstractFilter
 * implementation provides a standard for emitting Filter debug statements (i.e. for toString() calls).
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public abstract class AbstractFilter<T> implements Filter<T> {

    /**
     * Initial-line indent for human-readable toString() rendering.
     */
    protected static final String TOSTRING_INDENT = "|        ";

    class DelayedLogMessage {
        String message;
        String logLevel;

        public DelayedLogMessage(final String logLevel, final String message) {
            this.logLevel = logLevel;
            this.message = message;
        }
    }

    // Internal state
    protected Log log;
    private List<DelayedLogMessage> delayedLogMessages;
    private boolean processNullValues;

    /**
     * Convenience constructor which creates an AbstractFilter which <strong>does not</strong> process null values.
     */
    protected AbstractFilter() {

        // Assign internal state
        delayedLogMessages = new ArrayList<DelayedLogMessage>();
        setProcessNullValues(false);
    }

    /**
     * Validator method which should be called in a DI setter method to ensure that this AbstractFilter has
     * not yet been initialized.
     *
     * @param setterPropertyName The name of the property to inject.
     */
    protected final void validateDiSetterCalledBeforeInitialization(final String setterPropertyName) {
        Validate.isTrue(log == null, "DI Setters should only be called before initializing. Stray call: ["
                + setterPropertyName + "]");
    }

    /**
     * Adds a log message to be emitted when this AbstractFilter is initialized (and the Log is made available to it).
     *
     * @param logLevel The logLevel of the message to emit.
     * @param message  The message to emit.
     */
    protected final void addDelayedLogMessage(final String logLevel, final String message) {
        this.delayedLogMessages.add(new DelayedLogMessage(logLevel, message));
    }

    /**
     * Assigns the {@code processNullValues} property which indicates if this AbstractFilter
     * should process null values or not.
     *
     * @param processNullValues {@code true} to indicate that this AbstractFilter should process null values.
     */
    protected final void setProcessNullValues(final boolean processNullValues) {

        validateDiSetterCalledBeforeInitialization("processNullValues");
        this.processNullValues = processNullValues;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void initialize(final Log log) {

        // Check sanity
        Validate.notNull(log, "log");

        // Assign internal state
        this.log = log;

        if (delayedLogMessages.size() > 0) {
            for (DelayedLogMessage current : delayedLogMessages) {
                if (current.logLevel.equalsIgnoreCase("warn") && log.isWarnEnabled()) {
                    log.warn(current.message);
                } else if (current.logLevel.equals("info") && log.isInfoEnabled()) {
                    log.info(current.message);
                } else if (log.isDebugEnabled()) {
                    log.debug(current.message);
                }
            }

            delayedLogMessages.clear();
        }

        // Delegate
        onInitialize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInitialized() {
        return log != null;
    }

    /**
     * Override this method to perform some custom action after standard initialization is complete.
     * Default implementation does nothing, but the log is non-null and ready for use.
     */
    protected void onInitialize() {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean accept(final T candidate) throws IllegalStateException {

        // Check sanity
        if (log == null) {
            throw new IllegalStateException("Filter [" + getClass().getSimpleName() + "] not initialized before use.");
        }

        // Should we process null values?
        if (candidate == null) {

            boolean toReturn = false;
            if (processNullValues) {
                toReturn = onNullCandidate();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Received null candidate, and Filter [" + getClass().getSimpleName()
                            + "] is configured not to match nulls.");
                }
            }

            // All done.
            return toReturn;
        }

        // Delegate processing.
        return onCandidate(candidate);
    }

    /**
     * Override this to perform an action other than debug logging whenever this AbstractFilter
     * receives a null candidate. You also need to override this implementation if you want to return a value other
     * than {@code false}. There is no point in calling {@code super.onNullCandidate()} whenever this method is
     * overridden.
     *
     * @return {@code false} - implying that AbstractFilters will not matchAtLeastOnce {@code null}s by default.
     */
    protected boolean onNullCandidate() {
        if (log.isDebugEnabled()) {
            log.debug("Filter [" + getClass().getSimpleName()
                    + "] is configured to matchAtLeastOnce processing null candidate "
                    + "values, but no implementation is supplied.");
        }

        // Don't matchAtLeastOnce null values by default.
        return false;
    }

    /**
     * <p>Method that is invoked to determine if a candidate instance should be accepted or not.</p>
     *
     * @param nonNullCandidate The candidate that should be tested for acceptance by this Filter. Never null.
     * @return {@code true} if the candidate is accepted by this Filter and {@code false} otherwise.
     */
    protected abstract boolean onCandidate(final T nonNullCandidate);

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Filter [" + getClass().getSimpleName() + "]" + AbstractJaxbMojo.NEWLINE
                + TOSTRING_INDENT + "Processes nulls: [" + processNullValues + "]" + AbstractJaxbMojo.NEWLINE;
    }
}
