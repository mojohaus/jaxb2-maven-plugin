package org.codehaus.mojo.jaxb2.shared.environment.classloading;

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
import org.codehaus.mojo.jaxb2.shared.environment.AbstractLogAwareFacet;

/**
 * Adapter converting a ThreadContextClassLoaderHolder to the standard lifecycle
 * defined within the EnvironmentFacet.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ContextClassLoaderEnvironmentFacet extends AbstractLogAwareFacet {

    // Internal state
    private ThreadContextClassLoaderBuilder builder;
    private ThreadContextClassLoaderHolder holder;

    /**
     * Compound constructor creating a ContextClassLoaderEnvironmentFacet wrapping the supplied data.
     *
     * @param log     The active Maven Log.
     * @param builder A fully set up ThreadContextClassLoaderBuilder, where the {@code buildAndSet()} method
     *                should <strong>not</strong> be invoked yet.
     * @see ThreadContextClassLoaderBuilder
     */
    public ContextClassLoaderEnvironmentFacet(final Log log,
                                              final ThreadContextClassLoaderBuilder builder) {
        super(log);

        // Check sanity
        Validate.notNull(builder, "builder");

        // Assign internal state
        this.builder = builder;
    }

    /**
     * Delegate method retrieving the classpath as argument from the underlying ThreadContextClassLoaderHolder.
     * Note that the setup method must be invoked before this one is.
     *
     * @return the ClassPath as an argument to external processes such as XJC.
     */
    public String getClassPathAsArgument() {

        // Check sanity
        Validate.notNull(holder, "holder");

        // Delegate and return
        return holder.getClassPathAsArgument();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setup() {
        this.holder = builder.buildAndSet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restore() {
        holder.restoreClassLoaderAndReleaseThread();
    }
}
