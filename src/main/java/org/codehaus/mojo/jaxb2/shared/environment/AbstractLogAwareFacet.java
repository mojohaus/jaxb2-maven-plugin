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

/**
 * Abstract EnvironmentFacet which sports a non-null Maven Log for use by subclasses.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.1
 */
public abstract class AbstractLogAwareFacet implements EnvironmentFacet {

    // Internal state
    protected Log log;

    /**
     * Creates an AbstractLogAwareFacet wrapping the supplied, non-null Log.
     *
     * @param log The active Maven Log.
     */
    public AbstractLogAwareFacet(final Log log) {

        // Check sanity
        Validate.notNull(log, "log");

        // Assign internal state
        this.log = log;
    }
}
