package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc;

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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Definition of a person with lastName and age, and optionally a firstName as well...
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since Some version.
 */
@XmlRootElement
@XmlType(namespace = SomewhatNamedPerson.NAMESPACE, propOrder = {"firstName", "lastName", "age"})
@XmlAccessorType(XmlAccessType.FIELD)
public class SomewhatNamedPerson {

    /**
     * The XML namespace of this SomewhatNamedPerson.
     */
    public static final String NAMESPACE = "http://some/namespace";

    /**
     * The first name of the SomewhatNamedPerson.
     */
    @XmlElement(nillable = true, required = false)
    private String firstName;

    /**
     * The last name of the SomewhatNamedPerson.
     */
    @XmlElement(nillable = false, required = true)
    private String lastName;

    /**
     * The age of the SomewhatNamedPerson. Must be positive.
     */
    @XmlAttribute(required = true)
    private int age;

    /**
     * JAXB-friendly constructor.
     */
    public SomewhatNamedPerson() {
    }

    /**
     * Creates a SomewhatNamedPerson wrapping the supplied data.
     *
     * @param firstName The first name of the SomewhatNamedPerson.
     * @param lastName  The last name of the SomewhatNamedPerson. Cannot be null.
     * @param age       The age of the SomewhatNamedPerson. Must be positive.
     */
    public SomewhatNamedPerson(final String firstName, final String lastName, final int age) {

        // Check sanity
        Validate.notNull(lastName, "lastName");
        Validate.isTrue(age >= 0, "Cannot handle negative 'age' argument.");

        // Assign internal state
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    /**
     * Retrieves the first name of this SomewhatNamedPerson.
     *
     * @return The first name of this SomewhatNamedPerson, converting {@code null} values to empty strings.
     */
    public String getFirstName() {
        return getFirstName(true);
    }

    /**
     * Retrieves the first name of this SomewhatNamedPerson.
     *
     * @param replaceNull indicates if this method should replace null firstName values with an empty string.
     * @return The first name of this SomewhatNamedPerson, optionally converting {@code null} values to empty strings.
     */
    public String getFirstName(final boolean replaceNull) {
        return firstName == null && replaceNull ? "" : firstName;
    }

    /**
     * @return The last name of this SomewhatNamedPerson. Never null.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @return The age of this SomewhatNamedPerson.
     */
    public int getAge() {
        return age;
    }
}
