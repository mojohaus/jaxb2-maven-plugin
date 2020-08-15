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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * This is a JAXB-annotated class where not all properties are JavaDoc'ed.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement
@XmlType(namespace = SomewhatNamedPerson.NAMESPACE, propOrder = {"documented", "unDocumented"})
@XmlAccessorType(XmlAccessType.FIELD)
public class SemiDocumentedClass {

    /**
     * The XML namespace of this SomewhatNamedPerson.
     */
    public static final String NAMESPACE = "http://some/namespace";

    /**
     * This is a documented property.
     */
    @XmlElement(nillable = true, required = false)
    private String documented;

    @XmlElement(nillable = false, required = true)
    private String unDocumented;

    /**
     * JAXB-friendly constructor.
     */
    public SemiDocumentedClass() {
    }

    public String getDocumented() {
        return documented;
    }

    public String getUnDocumented() {
        return unDocumented;
    }
}
