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

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.jaxb2.AbstractJaxbMojo;
import org.codehaus.mojo.jaxb2.shared.Validate;
import org.codehaus.mojo.jaxb2.shared.environment.AbstractLogAwareFacet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EnvironmentFacet for replacing Handlers from Java Util Logging with a Maven Log.
 * This is required as an environment facet for capturing log statements from tools
 * that use the Java Util Logging system internally - such as the JDK SchemaGen tool.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.1
 */
public class LoggingHandlerEnvironmentFacet extends AbstractLogAwareFacet {

    /**
     * Standard logger names/categories for the java.util.Logger.
     */
    public static final String[] DEFAULT_LOGGER_NAMES = new String[]{"com.sun", "javax.xml", "javax.tools"};

    // Internal state
    private boolean restored;
    private Logger rootLogger;
    private Level originalRootLoggerLevel;
    private List<Handler> originalHandlers;
    private MavenLogHandler mavenLogHandler;

    private String logPrefix;
    private String encoding;
    private String[] loggerNamePrefixes;

    /**
     * Creates a new JavaLoggingEnvironment, using the supplied variables to set up a MavenLogHandler.
     * The MavenLogHandler is then assigned to the root logger.
     *
     * @param logPrefix          The prefix to use for the logger, indicating which tool is used by the log. Example: "XJC"
     *                           or "SchemaGen".
     * @param mavenLog           The active Maven Log.
     * @param encoding           The configured encoding.
     * @param loggerNamePrefixes The prefixes of the Logger names to be permitted logging.
     */
    public LoggingHandlerEnvironmentFacet(final String logPrefix,
                                          final Log mavenLog,
                                          final String encoding,
                                          final String[] loggerNamePrefixes) {

        super(mavenLog);

        // Check sanity
        Validate.notEmpty(encoding, "encoding");
        Validate.notNull(loggerNamePrefixes, "loggerNamePrefixes");

        // Assign internal state
        this.originalHandlers = new ArrayList<Handler>();
        this.logPrefix = logPrefix;
        rootLogger = Logger.getLogger("");
        originalRootLoggerLevel = rootLogger.getLevel();
        this.encoding = encoding;
        this.loggerNamePrefixes = loggerNamePrefixes;
    }

    /**
     * {@inheritDoc}
     * <p>Redirects JUL logging statements to the Maven Log.</p>
     */
    @Override
    public void setup() {

        // Redirect the JUL Logging statements to the Maven Log.
        rootLogger.setLevel(MavenLogHandler.getJavaUtilLoggingLevelFor(log));
        this.mavenLogHandler = new MavenLogHandler(log, logPrefix, encoding, loggerNamePrefixes);

        for (Handler current : rootLogger.getHandlers()) {

            // Stash the original handlers from the RootLogger.
            originalHandlers.add(current);

            // Remove the original handler from the RootLogger.
            rootLogger.removeHandler(current);
        }

        // Add the new Maven Log handler.
        rootLogger.addHandler(this.mavenLogHandler);
    }

    /**
     * Restores the original root Logger state, including Level and Handlers.
     */
    public void restore() {

        if (!restored) {

            // Remove the extra Handler from the RootLogger
            rootLogger.removeHandler(mavenLogHandler);

            // Restore the original state to the Root logger
            rootLogger.setLevel(originalRootLoggerLevel);
            for (Handler current : originalHandlers) {
                rootLogger.addHandler(current);
            }

            // All done.
            restored = true;
        }
    }

    /**
     * Factory method creating a new LoggingHandlerEnvironmentFacet wrapping the supplied properties.
     *
     * @param mavenLog The active Maven Log.
     * @param caller   The AbstractJaxbMojo subclass which invoked this LoggingHandlerEnvironmentFacet factory method.
     * @param encoding The encoding used by the Maven Mojo subclass.
     * @return A fully set up LoggingHandlerEnvironmentFacet
     */
    public static LoggingHandlerEnvironmentFacet create(final Log mavenLog,
                                                        final Class<? extends AbstractJaxbMojo> caller,
                                                        final String encoding) {

        // Check sanity
        Validate.notNull(mavenLog, "mavenLog");
        Validate.notNull(caller, "caller");
        Validate.notEmpty(encoding, "encoding");

        // Find the standard log prefix for the tool in question.
        final String logPrefix = caller.getClass().getCanonicalName().toUpperCase().contains("XJC")
                ? "XJC"
                : "SchemaGen";

        // All done.
        return new LoggingHandlerEnvironmentFacet(logPrefix, mavenLog, encoding, DEFAULT_LOGGER_NAMES);
    }
}
