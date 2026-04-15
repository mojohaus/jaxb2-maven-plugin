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
import javax.xml.namespace.NamespaceContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.mojo.jaxb2.schemageneration.XsdGeneratorHelper;
import org.codehaus.mojo.jaxb2.schemageneration.postprocessing.NodeProcessor;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <p>Namespace resolver for XML documents, which relates XML Namespace Prefixes to XML Namespace URIs.
 * Doubles as a JAXB NamespaceContext, if we decide to use JAXB instead of DOM to parse our generated
 * schema files.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 * @since 1.4
 */
public class SimpleNamespaceResolver implements NamespaceContext {

    // Constants
    private static final String DEFAULT_NS = "DEFAULT";
    private static final String TARGET_NAMESPACE = "targetNamespace";
    private static final String SCHEMA = "schema";

    // Internal state
    private String sourceFilename;
    private String localNamespaceURI;
    private Map<String, String> prefix2Uri = new HashMap<String, String>();
    private Map<String, String> uri2Prefix = new HashMap<String, String>();

    /**
     * Creates a new SimpleNamespaceResolver which collects namespace data
     * from the provided XML file.
     *
     * @param xmlFile The XML file from which to collect namespace data, should not be null.
     */
    public SimpleNamespaceResolver(final File xmlFile) {
        this.sourceFilename = xmlFile.getName();

        try (Reader reader = new FileReader(xmlFile)) {
            initialize(reader);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File [" + xmlFile + "] could not be found.");
        } catch (IOException e) {
            throw new IllegalArgumentException("This should never happen...", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getNamespaceURI(final String prefix) {
        if (prefix == null) {
            // Be compliant with the JAXB contract for NamespaceResolver.
            throw new IllegalArgumentException("Cannot handle null prefix argument.");
        }

        return prefix2Uri.get(XMLConstants.DEFAULT_NS_PREFIX.equals(prefix) ? DEFAULT_NS : prefix);
    }

    /**
     * {@inheritDoc}
     */
    public String getPrefix(final String namespaceURI) {
        if (namespaceURI == null) {
            // Be compliant with the JAXB contract for NamespaceResolver.
            throw new IllegalArgumentException("Cannot acquire prefix for null namespaceURI.");
        }

        return uri2Prefix.get(namespaceURI);
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<String> getPrefixes(final String namespaceURI) {
        if (namespaceURI == null) {
            // Be compliant with the JAXB contract for NamespaceResolver.
            throw new IllegalArgumentException("Cannot acquire prefixes for null namespaceURI.");
        }

        return Collections.singletonList(uri2Prefix.get(namespaceURI)).iterator();
    }

    /**
     * @return A readonly map relating namespace URIs to namespace prefixes.
     */
    public Map<String, String> getNamespaceURI2PrefixMap() {
        return Collections.unmodifiableMap(uri2Prefix);
    }

    /**
     * @return The namespace URI of the default namespace within the sourceFile of this SimpleNamespaceResolver.
     */
    public String getLocalNamespaceURI() {
        return localNamespaceURI;
    }

    /**
     * @return The name of the source file used for this SimpleNamespaceResolver.
     */
    public String getSourceFilename() {
        return sourceFilename;
    }

    //
    // Private helpers
    //

    /**
     * Initializes this SimpleNamespaceResolver to collect namespace data from the provided stream.
     *
     * @param xmlFileStream A Reader connected to the XML file from which we should read namespace data.
     */
    private void initialize(final Reader xmlFileStream) {

        // Build a DOM model.
        final Document parsedDocument = XsdGeneratorHelper.parseXmlStream(xmlFileStream);

        // Process the DOM model.
        XsdGeneratorHelper.process(parsedDocument.getFirstChild(), true, new NamespaceAttributeNodeProcessor());
    }

    private class NamespaceAttributeNodeProcessor implements NodeProcessor {
        /**
         * Defines if this visitor should process the provided node.
         *
         * @param aNode The DOM node to process.
         * @return <code>true</code> if the provided Node should be processed by this NodeProcessor.
         */
        public boolean accept(final Node aNode) {

            // Correct namespace?
            if (aNode.getNamespaceURI() != null
                    && XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(aNode.getNamespaceURI())) {
                return true;
            }

            // Is this Node the targetNamespace attribute?
            if (aNode instanceof Attr) {

                final Attr attribute = (Attr) aNode;
                final Element parent = attribute.getOwnerElement();
                if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(parent.getNamespaceURI())
                        && SCHEMA.equalsIgnoreCase(parent.getLocalName())
                        && TARGET_NAMESPACE.equals(attribute.getLocalName())) {

                    SimpleNamespaceResolver.this.localNamespaceURI = attribute.getNodeValue();
                }
            }

            // Ignore processing this Node.
            return false;
        }

        /**
         * Processes the provided DOM Node.
         *
         * @param aNode The DOM Node to process.
         */
        public void process(final Node aNode) {

            // If we have no namespace, use the DEFAULT_NS as the prefix
            final String cacheKey =
                    XMLConstants.XMLNS_ATTRIBUTE.equals(aNode.getNodeName()) ? DEFAULT_NS : aNode.getLocalName();
            final String nodeValue = aNode.getNodeValue();

            // Cache the namespace in both caches.
            // The "tns" prefix is special: once "tns" is the canonical prefix for a URI,
            // no other prefix should replace it. Conversely, "tns" is allowed to replace
            // a previously seen non-"tns" prefix for the same URI.
            final String oldPrefix = uri2Prefix.get(nodeValue);

            if (oldPrefix != null && oldPrefix.equals("tns")) {
                // "tns" already owns this URI — skip the new (non-tns) prefix entirely.
                // Do not add it to prefix2Uri to keep both maps consistent.
                return;
            }

            // Validate before mutating: ensure we are not overwriting an existing
            // mapping for a different URI under the same prefix key.
            final String existingUri = prefix2Uri.get(cacheKey);
            if (existingUri != null && !existingUri.equals(nodeValue)) {
                throw new IllegalStateException(
                        "Replaced URI [" + existingUri + "] with [" + nodeValue + "] for prefix [" + cacheKey + "]");
            }

            // Validate that we are not overwriting a non-tns prefix with another non-tns
            // prefix for the same URI (genuine conflict).
            if (oldPrefix != null
                    && !oldPrefix.equals(cacheKey)
                    && !cacheKey.equals("tns")) {
                throw new IllegalStateException(
                        "Replaced prefix [" + oldPrefix + "] with [" + cacheKey + "] for URI [" + nodeValue + "]");
            }

            // If "tns" is overriding a previously seen prefix, remove the stale
            // entry from prefix2Uri to keep both maps consistent.
            if (oldPrefix != null && !oldPrefix.equals(cacheKey)) {
                prefix2Uri.remove(oldPrefix);
            }

            // Now safe to mutate both maps.
            prefix2Uri.put(cacheKey, nodeValue);
            uri2Prefix.put(nodeValue, cacheKey);
        }
    }
}
