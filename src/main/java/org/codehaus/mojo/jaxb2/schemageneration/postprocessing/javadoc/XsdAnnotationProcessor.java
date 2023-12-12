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

import java.util.SortedMap;

import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.NodeProcessor;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.ClassLocation;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.FieldLocation;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.MethodLocation;
import org.codehaus.mojo.jaxb2.shared.Validate;
import org.w3c.dom.Node;

/**
 * <p>Node processor that injects XSD documentation annotations consisting of JavaDoc harvested Java source code
 * into ComplexTypes, Elements and Attributes. The documentation is injected as follows:</p>
 * <ol>
 * <li><strong>ComplexType</strong>: Class-level JavaDoc from the corresponding type is injected as an
 * annotation directly inside the complexType.</li>
 * <li><strong>Element</strong>: Field-level JavaDoc (or getter Method-level JavaDoc, in case the Field does
 * not contain a JavaDoc annotation) from the corresponding member is injected as an
 * annotation directly inside the element.</li>
 * <li><strong>Attribute</strong>: Field-level JavaDoc (or getter Method-level JavaDoc, in case the Field does
 * not contain a JavaDoc annotation) from the corresponding member is injected as an
 * annotation directly inside the element.</li>
 * </ol>
 * <p>Thus, the following 'vanilla'-generated XSD:</p>
 * <pre>
 *     <code>
 *         &lt;xs:complexType name="somewhatNamedPerson"&gt;
 *             &lt;xs:sequence&gt;
 *                 &lt;xs:element name="firstName" type="xs:string" nillable="true" minOccurs="0"/&gt;
 *                 &lt;xs:element name="lastName" type="xs:string"/&gt;
 *             &lt;/xs:sequence&gt;
 *             &lt;xs:attribute name="age" type="xs:int" use="required"/&gt;
 *         &lt;/xs:complexType&gt;
 *     </code>
 * </pre>
 * <p>... would be converted to the following annotated XSD, given a DefaultJavaDocRenderer:</p>
 * <pre>
 *     <code>
 *         &lt;xs:complexType name="somewhatNamedPerson"&gt;
 *             &lt;xs:annotation&gt;
 *                 &lt;xs:documentation&gt;&lt;![CDATA[Definition of a person with lastName and age, and optionally a firstName as well...
 *
 *                 (author): &lt;a href="mailto:lj@jguru.se"&gt;Lennart J&ouml;relid&lt;/a&gt;, jGuru Europe AB
 *                 (custom): A custom JavaDoc annotation.]]&gt;&lt;/xs:documentation&gt;
 *             &lt;/xs:annotation&gt;
 *             &lt;xs:sequence&gt;
 *                 &lt;xs:element minOccurs="0" name="firstName" nillable="true" type="xs:string"&gt;
 *                     &lt;xs:annotation&gt;
 *                         &lt;xs:documentation&gt;&lt;![CDATA[The first name of the SomewhatNamedPerson.]]&gt;&lt;/xs:documentation&gt;
 *                     &lt;/xs:annotation&gt;
 *                 &lt;/xs:element&gt;
 *                 &lt;xs:element name="lastName" type="xs:string"&gt;
 *                     &lt;xs:annotation&gt;
 *                         &lt;xs:documentation&gt;&lt;![CDATA[The last name of the SomewhatNamedPerson.]]&gt;&lt;/xs:documentation&gt;
 *                     &lt;/xs:annotation&gt;
 *                 &lt;/xs:element&gt;
 *            &lt;/xs:sequence&gt;
 *            &lt;xs:attribute name="age" type="xs:int" use="required"&gt;
 *                &lt;xs:annotation&gt;
 *                    &lt;xs:documentation&gt;&lt;![CDATA[The age of the SomewhatNamedPerson. Must be positive.]]&gt;&lt;/xs:documentation&gt;
 *                &lt;/xs:annotation&gt;
 *            &lt;/xs:attribute&gt;
 *          &lt;/xs:complexType&gt;
 *     </code>
 * </pre>
 * <p>... given that the Java class <code>SomewhatNamedPerson</code> has JavaDoc on its class and fields
 * corresponding to the injected XSD annotation/documentation elements.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.JavaDocRenderer
 * @since 2.0
 */
public class XsdAnnotationProcessor implements NodeProcessor {

    // Internal state
    private SortedMap<ClassLocation, JavaDocData> classJavaDocs;
    private SortedMap<FieldLocation, JavaDocData> fieldJavaDocs;
    private SortedMap<MethodLocation, JavaDocData> methodJavaDocs;
    private JavaDocRenderer renderer;

    /**
     * Creates an XsdAnnotationProcessor that uses the supplied/generated SearchableDocumentation to read all
     * JavaDoc structures and the supplied JavaDocRenderer to render JavaDocs into XSD documentation annotations.
     *
     * @param docs     A non-null SearchableDocumentation, produced from the source code of the JAXB compilation unit.
     * @param renderer A non-null JavaDocRenderer, used to render the JavaDocData within the SearchableDocumentation.
     */
    public XsdAnnotationProcessor(final SearchableDocumentation docs, final JavaDocRenderer renderer) {

        // Check sanity
        Validate.notNull(docs, "docs");
        Validate.notNull(renderer, "renderer");

        // Assign internal state
        this.classJavaDocs = docs.getAll(ClassLocation.class);
        this.fieldJavaDocs = docs.getAll(FieldLocation.class);
        this.methodJavaDocs = docs.getAll(MethodLocation.class);
        this.renderer = renderer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final Node aNode) {

        // Only deal with Element nodes with "name" attributes.
        if (!DomHelper.isNamedElement(aNode)) {
            return false;
        }

        /*
        <xs:complexType name="somewhatNamedPerson">
            <!-- ClassLocation JavaDocData insertion point -->

            <xs:sequence>

                <!-- FieldLocation or MethodLocation JavaDocData insertion point (within child) -->
                <xs:element name="firstName" type="xs:string" nillable="true" minOccurs="0"/>

                <!-- FieldLocation or MethodLocation JavaDocData insertion point (within child) -->
                <xs:element name="lastName" type="xs:string"/>
            </xs:sequence>

            <!-- FieldLocation or MethodLocation JavaDocData insertion point (within child) -->
            <xs:attribute name="age" type="xs:int" use="required"/>
        </xs:complexType>
        */

        // Only process nodes corresponding to Types we have any JavaDoc for.
        // TODO: How should we handle PackageLocations and package documentation?

        boolean toReturn = false;
        if (DomHelper.getMethodLocation(aNode, methodJavaDocs.keySet()) != null) {
            toReturn = true;
        } else if (DomHelper.getFieldLocation(aNode, fieldJavaDocs.keySet()) != null) {
            toReturn = true;
        } else if (DomHelper.getClassLocation(aNode, classJavaDocs.keySet()) != null) {
            toReturn = true;
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(final Node aNode) {
        DomHelper.insertXmlDocumentationAnnotationsFor(aNode, classJavaDocs, fieldJavaDocs, methodJavaDocs, renderer);
    }
}
