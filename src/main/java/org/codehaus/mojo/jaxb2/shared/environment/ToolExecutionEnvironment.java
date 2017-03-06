package org.codehaus.mojo.jaxb2.shared.environment;

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
import org.codehaus.mojo.jaxb2.shared.Validate;
import org.codehaus.mojo.jaxb2.shared.environment.classloading.ThreadContextClassLoaderBuilder;
import org.codehaus.mojo.jaxb2.shared.environment.classloading.ThreadContextClassLoaderHolder;
import org.codehaus.mojo.jaxb2.shared.environment.locale.LocaleFacet;
import org.codehaus.mojo.jaxb2.shared.environment.logging.LoggingHandlerEnvironmentFacet;

import java.util.ArrayList;
import java.util.List;

/**
 * Compound EnvironmentFacet implementation which is used to set up and use a collection
 * of other EnvironmentFacet instances during the run of the JAXB2 Maven Plugin.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ToolExecutionEnvironment extends AbstractLogAwareFacet {

    // Internal state
    private ThreadContextClassLoaderBuilder builder;
    private ThreadContextClassLoaderHolder holder;
    private LoggingHandlerEnvironmentFacet loggingHandlerEnvironmentFacet;
    private List<EnvironmentFacet> extraFacets;
    private LocaleFacet localeFacet;

    /**
     * Creates a new ToolExecutionEnvironment object wrapping the supplied Maven Log.
     *
     * @param mavenLog            The active Maven Log.
     * @param builder             The ThreadContextClassLoaderBuilder used to set up a ThreadContext ClassLoader for
     *                            this tool execution environment.
     * @param localeFacet         An optional LocaleFacet to alter the Locale for the tool execution environment. If
     *                            the localeFacet is {@code null}, the locale will not be changed.
     * @param loggingHandlerFacet The EnvironmentFacet for replacing Handlers from Java Util Logging with a Maven Log.
     */
    public ToolExecutionEnvironment(final Log mavenLog,
                                    final ThreadContextClassLoaderBuilder builder,
                                    final LoggingHandlerEnvironmentFacet loggingHandlerFacet,
                                    final LocaleFacet localeFacet) {
        super(mavenLog);

        // Check sanity
        Validate.notNull(builder, "builder");
        Validate.notNull(loggingHandlerFacet, "loggingHandlerFacet");

        // Assign internal state
        this.builder = builder;
        this.loggingHandlerEnvironmentFacet = loggingHandlerFacet;
        this.localeFacet = localeFacet;
        extraFacets = new ArrayList<EnvironmentFacet>();
    }

    /**
     * Adds the supplied EnvironmentFacet to this ToolExecutionEnvironment.
     *
     * @param facet the non-null EnvironmentFacet to add to this ToolExecutionEnvironment.
     */
    public void add(final EnvironmentFacet facet) {

        // Check sanity
        Validate.notNull(facet, "facet");

        // All done.
        extraFacets.add(facet);
    }

    /**
     * Delegate method retrieving the classpath as argument from the underlying ThreadContextClassLoaderHolder.
     * Note that the setup method must be invoked before this one is.
     *
     * @return the ClassPath as an argument to external processes such as XJC.
     * @see ThreadContextClassLoaderHolder#getClassPathAsArgument()
     */
    public String getClassPathAsArgument() {

        // Check sanity
        if (holder == null) {
            throw new IllegalStateException("Cannot retrieve the classpath argument before calling 'setup'");
        }

        // Delegate and return
        return holder.getClassPathAsArgument();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setup() {

        // Setup mandatory environment facets
        try {

            if (log.isDebugEnabled()) {
                log.debug("ToolExecutionEnvironment setup -- Starting.");
            }

            // Build the ClassLoader as required for the JAXB tools
            holder = builder.buildAndSet();

            // Redirect the JUL logging handler used by the tools to the Maven log.
            loggingHandlerEnvironmentFacet.setup();

            // If requested, switch the locale
            if (localeFacet != null) {
                localeFacet.setup();
            }

            // Setup optional/extra environment facets
            for (EnvironmentFacet current : extraFacets) {
                try {
                    current.setup();
                } catch (Exception e) {
                    throw new IllegalStateException("Could not setup() EnvironmentFacet of type ["
                            + current.getClass().getName() + "]", e);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("ToolExecutionEnvironment setup -- Done.");
            }

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Could not setup mandatory ToolExecutionEnvironment.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void restore() {

        try {

            if (log.isDebugEnabled()) {
                log.debug("ToolExecutionEnvironment restore -- Starting.");
            }

            for (EnvironmentFacet current : extraFacets) {
                try {
                    current.restore();
                } catch (Exception e) {
                    throw new IllegalStateException("Could not restore() EnvironmentFacet of type ["
                            + current.getClass().getName() + "]", e);
                }
            }
        } finally {

            // Restore the logging handler structure.
            loggingHandlerEnvironmentFacet.restore();

            // Restore the original locale
            if (localeFacet != null) {
                localeFacet.restore();
            }

            if (holder != null) {
                // Restore the original ClassLoader
                holder.restoreClassLoaderAndReleaseThread();
            }

            if (log.isDebugEnabled()) {
                log.debug("ToolExecutionEnvironment restore -- Done.");
            }
        }
    }
}
