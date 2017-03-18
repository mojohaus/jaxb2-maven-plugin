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

/**
 * <p>Specification for an Environment controller, which can infer a temporary and reversible change
 * to the environment of an executing task. Any changes performed by this Environment
 * must be reversible, and should be restored to their original values in the {@code restore()} method.</p>
 * <p>EnvironmentFacets are required since the JDK tools (XJC, SchemaGen, JXC) expect certain configuration
 * or setup to be present during their execution. For improved usability within the JAXB2-Maven-Plugin, we
 * would like to supply all configuration to the plugin, and delegate the setting of various system-, thread-,
 * logging- or environment properties to explicit EnvironmentFacet implementations.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.1
 */
public interface EnvironmentFacet {

    /**
     * Sets up this Environment, inferring temporary changes to environment variables or conditions.
     * The changes must be reversible, and should be restored to their original values in the {@code restore()} method.
     */
    void setup();

    /**
     * Restores the original Environment, implying that the change performed in {@code setup()}
     * method are restored to the state before the setup method was called.
     */
    void restore();
}
