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

import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.SortableLocation;
import org.codehaus.mojo.jaxb2.shared.Validate;

/**
 * Comparable path structure to locate a particular package within compilation unit.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public class PackageLocation implements SortableLocation {

    // Internal state
    private String packageName;

    /**
     * Creates a new PackageLocation with the supplied package name.
     *
     * @param packageName The name of the package potentially holding JavaDoc. Cannot be {@code null}.
     */
    public PackageLocation(final String packageName) {

        // Check sanity
        Validate.notNull(packageName, "packageName");

        // Assign internal state
        this.packageName = packageName;
    }

    /**
     * Retrieves the name of the package potentially holding JavaDoc.
     *
     * @return The name of the package potentially holding JavaDoc. Can be empty, but never {@code null}.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        // Check sanity
        if (obj == this) {
            return true;
        }

        // Delegate
        return obj instanceof PackageLocation && toString().equals(obj.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * <strong>Note:</strong> Packages cannot be renamed from a JAXB annotation.
     * {@inheritDoc}
     */
    @Override
    public String getAnnotationRenamedTo() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        return toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return packageName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEqualToPath(final String path) {

        // Check sanity
        Validate.notNull(path, "path");

        // All done.
        return toString().equals(path);
    }

    /**
     * <p>Compares the string representations of this PackageLocation and the supplied SortableLocation.</p>
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final SortableLocation that) {

        // Check sanity
        Validate.notNull(that, "that");
        if (this == that) {
            return 0;
        }

        // Delegate
        return this.toString().compareTo(that.toString());
    }
}
