package org.codehaus.mojo.jaxb2.shared.filters;

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
     * @return {@code true} if this Filter has been properly initialized (by a call to the {@code initialize} method).
     */
    boolean isInitialized();

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
