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

/**
 * <p>Specification for how to convert/render JavaDocData into an XML annotation. As an example, let us
 * assume that a class contains the following field and JavaDoc:</p>
 * <pre>
 *     <code>
 *         {@literal /}**
 *         The last name of the SomewhatNamedPerson.
 *         *{@literal /}
 *         {@literal @}XmlElement(nillable = false, required = true)
 *         private String lastName;
 *     </code>
 * </pre>
 * <p>The standard SchemaGen generation creates a complex type with the following element declaration:</p>
 * <pre>
 *     <code>
 *         &lt;xs:element name="lastName" type="xs:string" /&gt;
 *     </code>
 * </pre>
 * <p>However, if we use the jaxb2-maven-plugin for post-processing, we can inject the javadoc as an XSD
 * annotation into the resulting schema, yielding the following result:</p>
 * <pre>
 *     <code>
 *         &lt;xs:element name="lastName" type="xs:string">
 *             &lt;xs:annotation>
 *                 &lt;xs:documentation>&lt;![CDATA[The last name of the SomewhatNamedPerson.]]>&lt;/xs:documentation>
 *             &lt;/xs:annotation>
 *         &lt;/xs:element>
 *     </code>
 * </pre>
 * <p>The JavaDocRenderer will create the content of the CDATA element within the XSD documentation annotation,
 * given the JavaDocData for each field, such as the <em>lastName</em> in our example.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.DefaultJavaDocRenderer
 * @since 2.0
 */
public interface JavaDocRenderer {

    /**
     * <p>Renders the supplied JavaDocData structure as text to be used within an XSD documentation annotation.
     * The XSD documentation annotation will contain a CDATA section to which the rendered JavaDocData is
     * emitted.</p>
     *
     * @param nonNullData the JavaDocData instance to render as XSD documentation. Will never be {@code null}.
     * @param location    the SortableLocation where the JavaDocData was harvested. Never {@code null}.
     * @return The rendered text contained within the XML annotation.
     */
    String render(JavaDocData nonNullData, SortableLocation location);
}
