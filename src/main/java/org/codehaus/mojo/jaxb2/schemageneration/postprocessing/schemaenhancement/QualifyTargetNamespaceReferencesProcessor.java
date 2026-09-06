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

import javax.xml.XMLConstants;

import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.NodeProcessor;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <p><code>NodeProcessor</code> which prepends the target namespace prefix to any unprefixed
 * <code>&lt;xs:element ref="..."/&gt;</code> element references within a generated XML schema document.</p>
 *
 * <p>Schemagen may emit unprefixed <code>ref</code> attributes when referencing elements in the target namespace
 * without declaring a default namespace (<code>xmlns="..."</code>) on the root <code>&lt;xs:schema&gt;</code> element.
 * According to W3C XML Schema rules, unprefixed QNames in attribute values resolve against the default namespace,
 * so omitting both a prefix and a default namespace causes downstream validators (such as XJC) to report
 * <code>src-resolve.4.1: Error resolving component ... It was detected that ... has no namespace</code>.
 * This processor qualifies those references with the target namespace's prefix.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 * @since 4.1.1
 */
public class QualifyTargetNamespaceReferencesProcessor implements NodeProcessor {

    // Constants
    private static final String REFERENCE_ATTRIBUTE_NAME = "ref";
    private static final String ELEMENT_LOCAL_NAME = "element";

    // Internal state
    private final String targetNamespacePrefix;
    private final boolean hasDefaultNamespace;
    private int modifiedCount = 0;

    /**
     * Creates a new QualifyTargetNamespaceReferencesProcessor.
     *
     * @param targetNamespacePrefix The prefix associated with the schema's targetNamespace.
     * @param hasDefaultNamespace   <code>true</code> if the root <code>&lt;xs:schema&gt;</code> element declares
     *                              a default namespace (i.e. <code>xmlns="..."</code>), in which case unprefixed
     *                              QNames already legally resolve to it and must not be modified.
     */
    public QualifyTargetNamespaceReferencesProcessor(
            final String targetNamespacePrefix, final boolean hasDefaultNamespace) {
        this.targetNamespacePrefix = targetNamespacePrefix;
        this.hasDefaultNamespace = hasDefaultNamespace;
    }

    /**
     * @return The number of element reference nodes modified by this processor.
     */
    public int getModifiedCount() {
        return modifiedCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final Node aNode) {
        if (hasDefaultNamespace
                || targetNamespacePrefix == null
                || targetNamespacePrefix.trim().isEmpty()) {
            return false;
        }

        if (aNode instanceof Attr) {
            return isUnprefixedElementReference((Attr) aNode);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(final Node aNode) {
        final Attr attribute = (Attr) aNode;
        attribute.setValue(targetNamespacePrefix + ":" + attribute.getValue());
        modifiedCount++;
    }

    //
    // Private helpers
    //

    /**
     * Discovers if the provided attribute is an element reference without a namespace prefix,
     * on the form <code>&lt;xs:element ref="someElementInTargetNamespace"/&gt;</code>.
     *
     * @param attribute the attribute to test.
     * @return <code>true</code> if the attribute is named "ref", has no colon in its value, and belongs to an
     *         <code>xs:element</code> element.
     */
    private boolean isUnprefixedElementReference(final Attr attribute) {
        if (!REFERENCE_ATTRIBUTE_NAME.equals(attribute.getName())
                || attribute.getValue().contains(":")) {
            return false;
        }

        final Element owner = attribute.getOwnerElement();
        if (owner == null) {
            return false;
        }

        final String localName = owner.getLocalName();
        final String nodeName = owner.getNodeName();

        final boolean isSchemaNs = XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(owner.getNamespaceURI())
                || (owner.getNamespaceURI() == null && nodeName != null && nodeName.contains("element"));

        return isSchemaNs
                && (ELEMENT_LOCAL_NAME.equalsIgnoreCase(localName)
                        || (nodeName != null && nodeName.endsWith(":" + ELEMENT_LOCAL_NAME)));
    }
}
