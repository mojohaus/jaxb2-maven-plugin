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
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;

/**
 * <p><code>NodeProcessor</code> which alters the namespace prefix for all relevant Nodes within an XML
 * document Node. It alters namespace prefixes in the following logical places:</p>
 * <dl>
 * <dt>Schema Namespace Definition</dt>
 * <dd>xmlns:oldPrefix="http://some/namespace" is altered to xmlns:newPrefix="http://some/namespace"</dd>
 * <dt>Elements Namespace Prefix</dt>
 * <dd>&lt;oldPrefix:someElement ... &gt; is altered to &lt;newPrefix:someElement ... &gt;</dd>
 * <dt>Element Reference</dt>
 * <dd><code>&lt;xs:element ref="oldPrefix:aRequiredElementInTheOldPrefixNamespace"/&gt;</code> is altered to
 * <code>&lt;xs:element ref="newPrefix:aRequiredElementInTheOldPrefixNamespace"/&gt;</code></dd>
 * <dt>Type Attribute</dt>
 * <dd><code>&lt;xs:element type="oldPrefix:something"/&gt;</code> is altered to
 * <code>&lt;xs:element type="newPrefix:something"/&gt;</code></dd>
 * <dt>Type Extension</dt>
 * <dd><code>&lt;xs:extension base="oldPrefix:something"/&gt;</code> is altered to
 * <code>&lt;xs:extension base="newPrefix:something"/&gt;</code></dd>
 * </dl>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 * @since 1.4
 */
public class ChangeNamespacePrefixProcessor implements NodeProcessor {

    // Constants
    // <xs:extension base="tns:importItem">
    private static final String EXTENSION_ELEMENT_NAME = "extension";
    private static final String EXTENSION_BASE_ATTRIBUTE_NAME = "base";
    private static final String REFERENCE_ATTRIBUTE_NAME = "ref";
    private static final String TYPE_ATTRIBUTE_NAME = "type";
    private static final String SCHEMA = "schema";
    private static final String XMLNS = "xmlns:";

    // <xs:element name="someOtherImportItem" type="tns:someOtherImportItem"/>
    // private static final String ELEMENT_NAME = "element";

    // Internal state
    private String oldPrefix;
    private String newPrefix;

    /**
     * Creates a new ChangeNamespacePrefixProcessor providing the oldPrefix which should be replaced by the newPrefix.
     *
     * @param oldPrefix The old/current namespace prefix
     * @param newPrefix The new/substituted namespace prefix
     */
    public ChangeNamespacePrefixProcessor(final String oldPrefix, final String newPrefix) {
        this.oldPrefix = oldPrefix;
        this.newPrefix = newPrefix;
    }

    /**
     * {@inheritDoc}
     */
    public boolean accept(final Node aNode) {

        if (oldPrefix.equals(aNode.getPrefix())) {
            // Process any nodes on the form [oldPrefix]:something.
            return true;
        }

        if (aNode instanceof Attr) {

            // These cases are defined by attribute properties.
            final Attr attribute = (Attr) aNode;

            if (isNamespaceDefinition(attribute)
                    || isElementReference(attribute)
                    || isTypeAttributeWithPrefix(attribute)
                    || isExtension(attribute)) {
                return true;
            }
        }

        // Nopes.
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void process(final Node aNode) {

        if (aNode instanceof Attr) {

            final Attr attribute = (Attr) aNode;
            final Element parentElement = attribute.getOwnerElement();

            if (isNamespaceDefinition(attribute)) {

                // Use the incredibly smooth DOM way to rename an attribute...
                parentElement.setAttributeNS(attribute.getNamespaceURI(), XMLNS + newPrefix, aNode.getNodeValue());
                parentElement.removeAttribute(XMLNS + oldPrefix);

            } else if (isElementReference(attribute)
                    || isTypeAttributeWithPrefix(attribute)
                    || isExtension(attribute)) {

                // Simply alter the value of the reference
                final String value = attribute.getValue();
                final String elementName = value.substring(value.indexOf(":") + 1);
                attribute.setValue(newPrefix + ":" + elementName);
            }
        }

        if (oldPrefix.equals(aNode.getPrefix())) {
            // Simply change the prefix to the new one.
            aNode.setPrefix(newPrefix);
        }
    }

    //
    // Private helpers
    //

    /**
     * Discovers if the provided attribute is the oldPrefix namespace definition, i.e. if the given attribute is the
     * xmlns:[oldPrefix] within the schema Element.
     *
     * @param attribute the attribute to test.
     * @return <code>true</code> if the provided attribute is the oldPrefix namespace definition, i.e. if the given
     * attribute is the xmlns:[oldPrefix] within the schema Element.
     */
    private boolean isNamespaceDefinition(final Attr attribute) {

        final Element parent = attribute.getOwnerElement();

        return (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(parent.getNamespaceURI())
                && SCHEMA.equalsIgnoreCase(parent.getLocalName())
                && oldPrefix.equals(attribute.getLocalName()));
    }

    /**
     * Discovers if the provided attribute is a namespace reference to the oldPrefix namespace, on the form
     * <code>&lt;xs:element ref="oldPrefix:anElementInTheOldPrefixNamespace"/&gt;</code>
     *
     * @param attribute the attribute to test.
     * @return <code>true</code> if the provided attribute is named "ref" and starts with <code>[oldPrefix]:</code>, in
     * which case it is a reference to the oldPrefix namespace.
     */
    private boolean isElementReference(final Attr attribute) {
        return REFERENCE_ATTRIBUTE_NAME.equals(attribute.getName())
                && attribute.getValue().startsWith(oldPrefix + ":");
    }

    /**
     * Discovers if the provided attribute is a type attribute using the oldPrefix namespace, on the form
     * <code>&lt;xs:element type="oldPrefix:anElementInTheOldPrefixNamespace"/&gt;</code>
     *
     * @param attribute the attribute to test.
     * @return <code>true</code> if the provided attribute is named "type" and starts with <code>[oldPrefix]:</code>, in
     * which case it is a type in the oldPrefix namespace.
     */
    private boolean isTypeAttributeWithPrefix(final Attr attribute) {
        return TYPE_ATTRIBUTE_NAME.equals(attribute.getName()) && attribute.getValue().startsWith(oldPrefix + ":");
    }

    /**
     * Discovers if the provided attribute is a namespace reference to the oldPrefix namespace, on the form
     * <p/>
     * <pre>
     *     <code>&lt;xs:extension base="[oldPrefix]:importItem"&gt;</code>
     * </pre>
     *
     * @param attribute the attribute to test.
     * @return <code>true</code> if the provided attribute is named "extension" and starts with
     * <code>[oldPrefix]:</code>, in which case it is a reference to the oldPrefix namespace.
     */
    private boolean isExtension(final Attr attribute) {

        final Element parent = attribute.getOwnerElement();

        return (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(parent.getNamespaceURI()))
                && EXTENSION_ELEMENT_NAME.equalsIgnoreCase(parent.getLocalName())
                && EXTENSION_BASE_ATTRIBUTE_NAME.equalsIgnoreCase(attribute.getName())
                && attribute.getValue().startsWith(oldPrefix + ":");
    }
}