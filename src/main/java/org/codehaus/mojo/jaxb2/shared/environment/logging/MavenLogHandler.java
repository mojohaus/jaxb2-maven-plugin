package org.codehaus.mojo.jaxb2.shared.environment.logging;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.jaxb2.shared.Validate;

/**
 * Handler implementation which delegates its actual logging to an internal Maven log.
 * This is required to capture logging statements from tools that use the Java Util Logging
 * system internally - such as the JDK SchemaGen tool.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public class MavenLogHandler extends Handler {

    // Internal state
    private Log log;
    private String prefix;

    /**
     * Creates a new MavenLogHandler which adapts a {@link Handler} to emit log messages onto a Maven Log.
     *
     * @param log                       The Maven Log to emit log messages to.
     * @param prefix                    An optional prefix used to prefix any log message.
     * @param encoding                  The encoding which should be used.
     * @param acceptedLogRecordPrefixes A non-null list of prefixes holding LogRecord logger names for
     *                                  permitted/accepted LogRecords.
     */
    public MavenLogHandler(
            final Log log, final String prefix, final String encoding, final String[] acceptedLogRecordPrefixes) {

        // Check sanity
        Validate.notNull(log, "log");
        Validate.notNull(prefix, "prefix");
        Validate.notEmpty(encoding, "encoding");

        // Assign internal state
        this.log = log;
        this.prefix = prefix.isEmpty() ? "" : "[" + prefix + "]: ";

        setFormatter(new SimpleFormatter());
        setLevel(getJavaUtilLoggingLevelFor(log));
        try {
            setEncoding(encoding);
        } catch (UnsupportedEncodingException e) {
            log.error("Could not use encoding '" + encoding + "'", e);
        }

        if (acceptedLogRecordPrefixes != null && acceptedLogRecordPrefixes.length > 0) {
            setFilter(getLoggingFilter(acceptedLogRecordPrefixes));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(final LogRecord logRecord) {

        if (this.isLoggable(logRecord)) {

            final Level level = logRecord.getLevel();
            final String message = prefix + getFormatter().format(logRecord);

            if (Level.SEVERE.equals(level)) {
                log.error(message);
            } else if (Level.WARNING.equals(level)) {
                log.warn(message);
            } else if (Level.INFO.equals(level)) {
                log.info(message);
            } else {
                log.debug(message);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws SecurityException {
        // Do nothing.
    }

    /**
     * Retrieves the JUL Level matching the supplied Maven Log.
     *
     * @param mavenLog A non-null Maven Log.
     * @return The Corresponding JUL Level.
     */
    public static Level getJavaUtilLoggingLevelFor(final Log mavenLog) {

        // Check sanity
        Validate.notNull(mavenLog, "mavenLog");

        Level toReturn = Level.SEVERE;

        if (mavenLog.isDebugEnabled()) {
            toReturn = Level.FINER;
        } else if (mavenLog.isInfoEnabled()) {
            toReturn = Level.INFO;
        } else if (mavenLog.isWarnEnabled()) {
            toReturn = Level.WARNING;
        }

        // All Done.
        return toReturn;
    }

    /**
     * Retrieves a java.util.Logging filter used to ensure that only LogRecords whose
     * logger names start with any of the required prefixes are logged.
     *
     * @param requiredPrefixes A non-null list of prefixes to be matched with the LogRecord logger names.
     * @return A java.util.logging Filter that only permits logging LogRecords whose
     * logger names start with any of the required prefixes.
     */
    public static Filter getLoggingFilter(final String... requiredPrefixes) {

        // Check sanity
        Validate.notNull(requiredPrefixes, "requiredPrefixes");

        // All done.
        return new Filter() {

            // Internal state
            private List<String> requiredPrefs = Arrays.asList(requiredPrefixes);

            @Override
            public boolean isLoggable(final LogRecord record) {

                final String loggerName = record.getLoggerName();
                for (String current : requiredPrefs) {
                    if (loggerName.startsWith(current)) {
                        return true;
                    }
                }

                // No matches found.
                return false;
            }
        };
    }
}
