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
 * Comparable path structure to locate a particular field within compilation unit.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public class FieldLocation extends ClassLocation {

    // Internal state
    private String memberName;

    /**
     * Creates a new FieldLocation with the supplied package, class and member names.
     *
     * @param packageName The name of the package for a class potentially holding JavaDoc. Cannot be {@code null}.
     * @param className   The (simple) name of a class. Cannot be null or empty.
     * @param memberName  The name of a (method or) field. Cannot be null or empty.
     */
    public FieldLocation(final String packageName,
                         final String className,
                         final String memberName) {

        // Delegate
        super(packageName, className);

        // Check sanity
        Validate.notEmpty(memberName, "memberName");

        // Assign internal state
        this.memberName = memberName;
    }

    /**
     * Retrieves the name of the member (i.e. method or field), potentially holding JavaDoc.
     *
     * @return The name of the member (i.e. method or field), potentially holding JavaDoc.
     * Never null or empty.
     */
    public String getMemberName() {
        return memberName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + "#" + memberName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
