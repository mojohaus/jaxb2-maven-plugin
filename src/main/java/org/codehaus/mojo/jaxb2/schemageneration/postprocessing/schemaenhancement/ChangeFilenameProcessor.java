package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.schemaenhancement;

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
import org.codehaus.mojo.jaxb2.shared.Validate;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import java.util.Map;

/**
 * <p>NodeProcessor which alters the filename for generated XML schema files.
 * The ChangeNamespacePrefixProcessor alters the following:</p>
 * <dl>
 * <dt>Schema Import Definitions</dt>
 * <dd>&lt;xs:import namespace="http://some/namespace" schemaLocation="<strong>schema2.xsd</strong>"/&gt; is
 * altered to
 * &lt;xs:import namespace="http://some/namespace" schemaLocation="<strong>anotherFile.xsd</strong>"/&gt;</dd>
 * </dl>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 * @since 1.4
 */
public class ChangeFilenameProcessor implements NodeProcessor {

    // Constants
    private static final String SCHEMA_LOCATION = "schemaLocation";
    private static final String IMPORT = "import";
    private static final String NAMESPACE = "namespace";

    // Internal state
    private Map<String, String> namespaceUriToNewFilenameMap;

    /**
     * <p>Creates a new ChangeFilenameProcessor using the provided map relating namespace URIs
     * to desired new file names.</p>
     *
     * @param namespaceUriToNewFilenameMap A map relating namespace URIs [key] to
     *                                     new/desired schema filenames [value].
     */
    public ChangeFilenameProcessor(final Map<String, String> namespaceUriToNewFilenameMap) {

        // Check sanity
        Validate.notNull(namespaceUriToNewFilenameMap, "namespaceUriToNewFilenameMap");

        // Assign internal state
        this.namespaceUriToNewFilenameMap = namespaceUriToNewFilenameMap;
    }

    /**
     * {@inheritDoc}
     */
    public boolean accept(final Node aNode) {
        return aNode instanceof Attr && isSchemaLocationAttributeForKnownNamespaceUri((Attr) aNode);
    }

    /**
     * {@inheritDoc}
     */
    public void process(final Node aNode) {

        // Only attributes are permitted here.
        Attr attribute = (Attr) aNode;

        // Change the fileName.
        String newFilename = namespaceUriToNewFilenameMap.get(getNamespace(attribute));
        attribute.setValue(newFilename);
    }

    //
    // Private helpers
    //

    /**
     * Discovers if the provided attribute is a schemaLocation definition, which should
     * be changed by this ChangeFilenameProcessor. Such an attribute is on the form
     * <code>&lt;xs:import namespace="http://a/registered/namespace" schemaLocation="schema1.xsd"/&gt;</code>.
     *
     * @param attribute the attribute to test.
     * @return <code>true</code> if the provided attribute is a schemaLocation definition
     * whose namespace is known to this ChangeFilenameProcessor.
     */
    private boolean isSchemaLocationAttributeForKnownNamespaceUri(final Attr attribute) {

        final Element parent = attribute.getOwnerElement();

        // <xs:import namespace="http://yet/another/namespace" schemaLocation="schema1.xsd"/>
        return XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(parent.getNamespaceURI())
                && IMPORT.equalsIgnoreCase(parent.getLocalName())
                && namespaceUriToNewFilenameMap.containsKey(getNamespace(attribute))
                && SCHEMA_LOCATION.equals(attribute.getName());
    }

    /**
     * Retrieves the value of the "namespace" attribute found within the parent element of the provided attribute.
     *
     * @param attribute An attribute defined within the parent holding the "namespace" attribute.
     * @return The value of the "namespace" attribute.
     */
    private String getNamespace(final Attr attribute) {
        final Element parent = attribute.getOwnerElement();
        return parent.getAttribute(NAMESPACE);
    }
}