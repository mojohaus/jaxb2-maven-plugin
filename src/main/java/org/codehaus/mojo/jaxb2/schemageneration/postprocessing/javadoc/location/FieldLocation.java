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

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
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
    private String memberXmlName;

    /**
     * Creates a new FieldLocation with the supplied package, class and member names.
     *
     * @param packageName   The name of the package for a class potentially holding JavaDoc. Cannot be {@code null}.
     * @param className     The (simple) name of a class. Cannot be null or empty.
     * @param memberName    The name of a (method or) field. Cannot be null or empty.
     * @param classXmlName  The name given as the {@link XmlType#name()} value of an annotation placed on the Class,
     *                      or {@code  null} if none is provided.
     * @param memberXmlName The name given as the {@link XmlElement#name()} or {@link XmlAttribute#name()} value of
     *                      an annotation placed on this Field, or {@code null} if none is provided.
     */
    public FieldLocation(
            final String packageName,
            final String className,
            final String classXmlName,
            final String memberName,
            final String memberXmlName) {

        // Delegate
        super(packageName, className, classXmlName);

        // Check sanity
        Validate.notEmpty(memberName, "memberName");

        // Assign internal state
        this.memberName = memberName;
        this.memberXmlName = memberXmlName;
    }

    /**
     * Retrieves the name of the member (i.e. method or field), potentially holding JavaDoc.
     *
     * @return The name of the member (i.e. method or field), potentially holding JavaDoc.
     * Never null or empty.
     */
    public String getMemberName() {
        return memberXmlName == null ? memberName : memberXmlName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAnnotationRenamedTo() {
        return memberXmlName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        return super.getPath() + "#" + getMemberName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        final String xmlOverriddenFrom =
                memberXmlName != null && !memberName.equals(memberXmlName) ? " (from: " + memberName + ")" : "";

        return super.toString() + "#" + getMemberName() + xmlOverriddenFrom;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
