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

import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.NodeProcessor;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.ClassLocation;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.FieldLocation;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.javadoc.location.MethodLocation;
import org.codehaus.mojo.jaxb2.shared.Validate;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.SortedMap;

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

    /**
     * The namespace schema prefix for the URI {@code http://www.w3.org/2001/XMLSchema}
     * (i.e. {@code XMLConstants.W3C_XML_SCHEMA_NS_URI}).
     *
     * @see javax.xml.XMLConstants#W3C_XML_SCHEMA_NS_URI
     */
    public static final String XSD_SCHEMA_NAMESPACE_PREFIX = "xs";

    /**
     * The name of the annotation element.
     */
    public static final String ANNOTATION_ELEMENT_NAME = "annotation";

    /**
     * The name of the documentation element.
     */
    public static final String DOCUMENTATION_ELEMENT_NAME = "documentation";

    // Internal state
    private static final List<String> FIELD_METHOD_ELEMENT_NAMES = Arrays.<String>asList("element", "attribute");
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

        // Only deal with Element nodes.
        if (aNode.getNodeType() != Node.ELEMENT_NODE || getName(aNode) == null) {
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
        // TODO: How should we handle PackageLocations and package documentation.
        boolean toReturn = false;
        if (getMethodLocation(aNode, methodJavaDocs.keySet()) != null) {
            toReturn = true;
        } else if (getFieldLocation(aNode, fieldJavaDocs.keySet()) != null) {
            toReturn = true;
        } else if (getClassLocation(aNode, classJavaDocs.keySet()) != null) {
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

        JavaDocData javaDocData = null;
        SortableLocation location = null;

        // Insert the documentation annotation into the current Node.
        final ClassLocation classLocation = getClassLocation(aNode, classJavaDocs.keySet());
        if (classLocation != null) {
            javaDocData = classJavaDocs.get(classLocation);
            location = classLocation;
        } else {

            final FieldLocation fieldLocation = getFieldLocation(aNode, fieldJavaDocs.keySet());
            if (fieldLocation != null) {
                javaDocData = fieldJavaDocs.get(fieldLocation);
                location = fieldLocation;
            } else {

                final MethodLocation methodLocation = getMethodLocation(aNode, methodJavaDocs.keySet());
                if (methodLocation != null) {
                    javaDocData = methodJavaDocs.get(methodLocation);
                    location = methodLocation;
                }
            }
        }

        // We should have a JavaDocData here.
        if (javaDocData == null) {
            throw new IllegalStateException("Could not find JavaDocData for XSD node [" + getName(aNode)
                    + "] with XPath [" + getXPathFor(aNode) + "]");
        }

        //
        // 1. Append the JavaDoc data Nodes, on the form below
        // 2. Append the JavaDoc data Nodes only if the renderer yields a non-null/non-empty javadoc.
        //
        /*
        <xs:annotation>
            <xs:documentation>(JavaDoc here, within a CDATA section)</xs:documentation>
        </xs:annotation>

        where the "xs" namespace prefix maps to "http://www.w3.org/2001/XMLSchema"
         */
        final String processedJavaDoc = renderer.render(javaDocData, location).trim();
        if (!processedJavaDoc.isEmpty()) {

            final String standardXsPrefix = "xs";
            final Document doc = aNode.getOwnerDocument();
            final Element annotation = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, ANNOTATION_ELEMENT_NAME);
            final Element docElement = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, DOCUMENTATION_ELEMENT_NAME);
            final CDATASection xsdDocumentation = doc.createCDATASection(renderer.render(javaDocData, location).trim());

            annotation.setPrefix(standardXsPrefix);
            docElement.setPrefix(standardXsPrefix);

            annotation.appendChild(docElement);
            final Node firstChildOfCurrentNode = aNode.getFirstChild();
            if (firstChildOfCurrentNode == null) {
                aNode.appendChild(annotation);
            } else {
                aNode.insertBefore(annotation, firstChildOfCurrentNode);
            }

            docElement.appendChild(xsdDocumentation);
        }
    }

    //
    // Private helpers
    //

    private static MethodLocation getMethodLocation(final Node aNode, final Set<MethodLocation> methodLocations) {

        MethodLocation toReturn = null;

        if (aNode != null && FIELD_METHOD_ELEMENT_NAMES.contains(aNode.getLocalName().toLowerCase())) {

            final MethodLocation validLocation = getFieldOrMethodLocationIfValid(aNode,
                    getContainingClassOrNull(aNode),
                    methodLocations);

            // The MethodLocation should represent a normal getter; no arguments should be present.
            if (validLocation != null
                    && MethodLocation.NO_PARAMETERS.equalsIgnoreCase(validLocation.getParametersAsString())) {
                toReturn = validLocation;
            }
        }

        // All done.
        return toReturn;
    }

    private static FieldLocation getFieldLocation(final Node aNode, final Set<FieldLocation> fieldLocations) {

        FieldLocation toReturn = null;

        if (aNode != null && FIELD_METHOD_ELEMENT_NAMES.contains(aNode.getLocalName().toLowerCase())) {
            toReturn = getFieldOrMethodLocationIfValid(aNode, getContainingClassOrNull(aNode), fieldLocations);
        }

        // All done.
        return toReturn;
    }

    private static <T extends FieldLocation> T getFieldOrMethodLocationIfValid(
            final Node aNode,
            final Node containingClassNode,
            final Set<? extends FieldLocation> locations) {

        T toReturn = null;

        if (containingClassNode != null) {

            // Do we have a FieldLocation corresponding to the supplied Node?
            for (FieldLocation current : locations) {

                // Validate that the field and class names match the FieldLocation's corresponding values.
                // Note that we cannot match package names here, as the generated XSD does not contain package
                // information directly. Instead, we must get the Namespace for the generated Class, and compare
                // it to the effective Namespace of the current Node.
                //
                // However, this is a computational-expensive operation, implying we would rather
                // do it at processing time when the number of nodes are (considerably?) reduced.

                final String fieldName = current.getMemberName();
                final String className = current.getClassName();

                try {
                    if (fieldName.equalsIgnoreCase(getName(aNode))
                            && className.equalsIgnoreCase(getName(containingClassNode))) {
                        toReturn = (T) current;
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("Could not acquire FieldLocation for fieldName ["
                            + fieldName + "] and className [" + className + "]", e);
                }
            }
        }

        // All done.
        return toReturn;
    }

    private static ClassLocation getClassLocation(final Node aNode, final Set<ClassLocation> classLocations) {

        if (aNode != null && "complexType".equalsIgnoreCase(aNode.getLocalName())) {

            final String nodeClassName = getName(aNode);
            for (ClassLocation current : classLocations) {

                // TODO: Ensure that the namespace of the supplied aNode matches the expected namespace.
                if (current.getClassName().equalsIgnoreCase(nodeClassName)) {
                    return current;
                }
            }
        }

        // Nothing found
        return null;
    }

    private static String getName(final Node aNode) {

        final NamedNodeMap attributes = aNode.getAttributes();
        if (attributes != null) {

            final Node nameNode = attributes.getNamedItem("name");
            if (nameNode != null) {
                return nameNode.getNodeValue().trim();
            }
        }

        // No name found
        return null;
    }

    private static Node getContainingClassOrNull(final Node aNode) {

        for (Node current = aNode.getParentNode(); current != null; current = current.getParentNode()) {

            final String localName = current.getLocalName();
            if ("complexType".equalsIgnoreCase(localName)) {
                return current;
            }
        }

        // No parent Node found.
        return null;
    }

    private static String getXPathFor(final Node aNode) {

        List<String> nodeNameList = new ArrayList<String>();

        for (Node current = aNode; current != null; current = current.getParentNode()) {
            nodeNameList.add(current.getNodeName() + "[@name='" + getName(current) + "]");
        }

        StringBuilder builder = new StringBuilder();
        for (ListIterator<String> it = nodeNameList.listIterator(nodeNameList.size()); it.hasPrevious(); ) {
            builder.append(it.previous());
            if (it.hasPrevious()) {
                builder.append("/");
            }
        }

        return builder.toString();
    }
}
