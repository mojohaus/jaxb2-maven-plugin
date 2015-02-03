package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location;

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

import org.codehaus.mojo.jaxb2.shared.Validate;

/**
 * Comparable path structure to locate a particular class within compilation unit.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public class ClassLocation extends PackageLocation {

    // Internal state
    private String className;

    /**
     * Creates a new ClassLocation with the supplied package and class names.
     *
     * @param packageName The name of the package for a class potentially holding JavaDoc. Cannot be {@code null}.
     * @param className   The (simple) name of a class. Cannot be null or empty.
     */
    public ClassLocation(final String packageName,
                         final String className) {

        super(packageName);

        // Check sanity
        Validate.notEmpty(className, "className");

        // Assign internal state
        this.className = className;
    }

    /**
     * Retrieves the simple class name for the class potentially holding JavaDoc. Never {@code null} or empty.
     *
     * @return The simple class name for the class potentially holding JavaDoc. Never {@code null} or empty.
     */
    public String getClassName() {
        return className;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + "." + className;
    }
}
