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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
    private static final String TARGET_NAMESPACE_PREFIX = "tns";
    private static final String SCHEMA = "schema";

    // Internal state
    private String sourceFilename;
    private String localNamespaceURI;
    private Map<String, String> prefix2Uri = new HashMap<String, String>();
    private Map<String, String> uri2Prefix = new HashMap<String, String>();
    private Map<String, Set<String>> uri2Prefixes = new LinkedHashMap<String, Set<String>>();

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

        final Set<String> prefixes = uri2Prefixes.get(namespaceURI);
        return prefixes == null
                ? Collections.<String>emptyList().iterator()
                : Collections.unmodifiableSet(prefixes).iterator();
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

        // Reduce each URI to its canonical prefix, which can only be decided once every prefix bound to
        // that URI is known. Attributes are not necessarily visited in the order they are declared, so
        // deciding this while collecting would make the outcome depend on the order of the DOM traversal.
        for (Map.Entry<String, Set<String>> current : uri2Prefixes.entrySet()) {
            uri2Prefix.put(current.getKey(), getCanonicalPrefix(current.getKey(), current.getValue()));
        }
    }

    /**
     * Selects the prefix representing the supplied namespace URI, out of all prefixes bound to it within the
     * source file. The "tns" prefix wins whenever it is present, since a prefix supplied through
     * <code>@XmlSchema(xmlns=...)</code> is emitted in addition to it rather than instead of it.
     *
     * @param namespaceUri The namespace URI for which to select a prefix.
     * @param prefixes     All prefixes bound to the namespaceUri, in the order they were found.
     * @return The single prefix representing the namespaceUri.
     * @throws IllegalStateException if several prefixes are bound to the namespaceUri and none of them is "tns",
     *                               in which case there is no ground to prefer one over another.
     */
    private static String getCanonicalPrefix(final String namespaceUri, final Set<String> prefixes) {

        if (prefixes.contains(TARGET_NAMESPACE_PREFIX)) {
            return TARGET_NAMESPACE_PREFIX;
        }

        final Iterator<String> it = prefixes.iterator();
        final String firstPrefix = it.next();

        if (it.hasNext()) {
            throw new IllegalStateException(
                    "Replaced prefix [" + firstPrefix + "] with [" + it.next() + "] for URI [" + namespaceUri + "]");
        }

        return firstPrefix;
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

            // A prefix binds to exactly one URI. Rebinding it to another one is a genuine conflict,
            // whereas repeating the binding it already has is a no-op rather than a replacement.
            final String boundUri = prefix2Uri.get(cacheKey);
            if (boundUri != null && !boundUri.equals(nodeValue)) {
                throw new IllegalStateException(
                        "Replaced URI [" + boundUri + "] with [" + nodeValue + "] for prefix [" + cacheKey + "]");
            }
            prefix2Uri.put(cacheKey, nodeValue);

            // A URI, on the other hand, may be bound to several prefixes. Collect them all; which one
            // represents the URI is decided in getCanonicalPrefix once the whole file has been read.
            uri2Prefixes
                    .computeIfAbsent(nodeValue, key -> new LinkedHashSet<String>())
                    .add(cacheKey);
        }
    }
}
