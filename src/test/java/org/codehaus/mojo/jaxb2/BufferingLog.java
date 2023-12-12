package org.codehaus.mojo.jaxb2;

import java.text.NumberFormat;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.jaxb2.shared.Validate;

/**
 * Trivial Maven Log implementation which stores all logged messages
 * within a SortedMap for later retrieval.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class BufferingLog implements Log {

    public enum LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR,
        NONE
    }

    // Internal state
    private static final NumberFormat INTEGER_FORMAT = NumberFormat.getIntegerInstance();
    private LogLevel minLevel;
    private final Object lock = new Object();
    private SortedMap<String, Throwable> logBuffer;

    static {
        setMinimumIntegerDigits(3);
    }

    public BufferingLog() {
        this(LogLevel.INFO);
    }

    public BufferingLog(final LogLevel minLevel) {

        // Check sanity
        Validate.notNull(minLevel, "minLevel");

        // Assign internal state
        logBuffer = new TreeMap<String, Throwable>();
        this.minLevel = minLevel;
    }

    /**
     * @return The minimum LogLevel for which this BufferingLog will record log events.
     */
    public LogLevel getMinimumLogLevel() {
        return minLevel;
    }

    /**
     * @return The LogBuffer holding all log messages, and their corresponding (optional) Throwable.
     */
    public SortedMap<String, Throwable> getLogBuffer() {
        return logBuffer;
    }

    /**
     * Retrieves the current LogBuffer, and resets the internal state of this BufferingLog.
     *
     * @return The LogBuffer holding all log messages, and their corresponding (optional) Throwable.
     */
    public SortedMap<String, Throwable> getAndResetLogBuffer() {

        final SortedMap<String, Throwable> toReturn = logBuffer;
        synchronized (lock) {
            this.logBuffer = new TreeMap<String, Throwable>();
        }

        return toReturn;
    }

    /**
     * Assigns the minimum number of Integer digits in the Log format.
     *
     * @param minimumIntegerDigits the minimum number of Integer digits in the Log format.
     */
    public static void setMinimumIntegerDigits(final int minimumIntegerDigits) {
        INTEGER_FORMAT.setMinimumIntegerDigits(minimumIntegerDigits);
    }

    /**
     * @return A Pretty-printed version of the log buffer.
     */
    public String getPrettyPrintedLog() {

        final StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Throwable> current : logBuffer.entrySet()) {
            builder.append("\n").append(current.getKey());

            final Throwable error = current.getValue();
            if (error != null) {
                builder.append(
                        " [" + error.getMessage() + "]: " + error.getClass().getSimpleName());
            }
        }

        // All done.
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    public void debug(CharSequence content, Throwable error) {
        addLogEntry(LogLevel.DEBUG, content, error);
    }

    /**
     * {@inheritDoc}
     */
    public void debug(CharSequence content) {
        debug(content, null);
    }

    /**
     * {@inheritDoc}
     */
    public void debug(Throwable error) {
        debug("", error);
    }

    /**
     * {@inheritDoc}
     */
    public void info(CharSequence content, Throwable error) {
        addLogEntry(LogLevel.INFO, content, error);
    }

    /**
     * {@inheritDoc}
     */
    public void info(CharSequence content) {
        info(content, null);
    }

    /**
     * {@inheritDoc}
     */
    public void info(Throwable error) {
        info("", error);
    }

    /**
     * {@inheritDoc}
     */
    public void warn(CharSequence content, Throwable error) {
        addLogEntry(LogLevel.WARN, content, error);
    }

    /**
     * {@inheritDoc}
     */
    public void warn(CharSequence content) {
        warn(content, null);
    }

    /**
     * {@inheritDoc}
     */
    public void warn(Throwable error) {
        warn("", error);
    }

    /**
     * {@inheritDoc}
     */
    public void error(final CharSequence content, final Throwable error) {
        addLogEntry(LogLevel.ERROR, content, error);
    }

    /**
     * {@inheritDoc}
     */
    public void error(CharSequence content) {
        error(content, null);
    }

    /**
     * {@inheritDoc}
     */
    public void error(final Throwable error) {
        error("", error);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDebugEnabled() {
        return isLogEnabledFor(LogLevel.DEBUG);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInfoEnabled() {
        return isLogEnabledFor(LogLevel.INFO);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWarnEnabled() {
        return isLogEnabledFor(LogLevel.WARN);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isErrorEnabled() {
        return isLogEnabledFor(LogLevel.ERROR);
    }

    //
    // Private helpers
    //

    private void addLogEntry(final LogLevel level, final CharSequence message, final Throwable throwable) {

        synchronized (lock) {
            if (isLogEnabledFor(level)) {

                final int index = logBuffer.size();
                final String logMessage = "" + INTEGER_FORMAT.format(index) + ": (" + level + ") " + message;
                logBuffer.put(logMessage, throwable);
            }
        }
    }

    private boolean isLogEnabledFor(final LogLevel level) {
        return level.compareTo(minLevel) >= 0;
    }
}
