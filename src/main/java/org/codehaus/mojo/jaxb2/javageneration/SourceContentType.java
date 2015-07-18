package org.codehaus.mojo.jaxb2.javageneration;

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
 * The type of source input used by XJC.
 * The constants are duplicated in lowercase since Maven's Mojo argument matcher is case sensitive.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see <a href="https://jaxb.java.net/">The JAXB Reference Implementation</a>
 * @since 2.0
 */
public enum SourceContentType {

    /**
     * <p>Treat input as DTDs (i.e. <a href="http://en.wikipedia.org/wiki/Document_type_definition">Document Type
     * Definitions</a>). This option is labelled as "experimental,unsupported" in the XJC documentation.</p>
     */
    DTD("dtd"),

    /**
     * <p>Treat input as DTDs (i.e. <a href="http://en.wikipedia.org/wiki/Document_type_definition">Document Type
     * Definitions</a>). This option is labelled as "experimental,unsupported" in the XJC documentation.</p>
     *
     * @see #DTD
     */
    dtd("dtd"),

    /**
     * <p>Treat input as W3C XML Schema (i.e. <a href="http://www.w3.org/TR/xmlschema11-1/">Xml Schema Definitions</a>).
     * This is the standard mode of the XJC (and is recommended by the Codehaus Mojo team as well).</p>
     */
    XmlSchema("xmlschema"),

    /**
     * <p>Treat input as W3C XML Schema (i.e. <a href="http://www.w3.org/TR/xmlschema11-1/">Xml Schema Definitions</a>).
     * This is the standard mode of the XJC (and is recommended by the Codehaus Mojo team as well).</p>
     *
     * @see #XmlSchema
     */
    xmlschema("xmlschema"),

    /**
     * <p>Treat input as <a href="http://relaxng.org/">Relax NG</a>.
     * This option is labelled as "experimental,unsupported" in the XJC documentation.</p>
     *
     * @see <a href="http://en.wikipedia.org/wiki/RELAX_NG">Relax NG on WikiPedia</a>
     */
    RelaxNG("relaxng"),

    /**
     * <p>Treat input as <a href="http://relaxng.org/">Relax NG</a>.
     * This option is labelled as "experimental,unsupported" in the XJC documentation.</p>
     *
     * @see <a href="http://en.wikipedia.org/wiki/RELAX_NG">Relax NG on WikiPedia</a>
     * @see #RelaxNG
     */
    relaxng("relaxng"),

    /**
     * <p>Treat input as <a href="https://www.oasis-open.org/committees/relax-ng/compact-20021121.html">Relax
     * NG with Compact syntax</a>. This option is labelled as "experimental,unsupported" in the XJC documentation.</p>
     *
     * @see <a href="http://en.wikipedia.org/wiki/RELAX_NG">Relax NG on WikiPedia</a>
     */
    RelaxNGCompact("relaxng-compact"),

    /**
     * <p>Treat input as <a href="https://www.oasis-open.org/committees/relax-ng/compact-20021121.html">Relax
     * NG with Compact syntax</a>. This option is labelled as "experimental,unsupported" in the XJC documentation.</p>
     *
     * @see <a href="http://en.wikipedia.org/wiki/RELAX_NG">Relax NG on WikiPedia</a>
     * @see #RelaxNGCompact
     */
    relaxng_compact("relaxng-compact"),

    /**
     * <p>Treat input as <a href="https://www.oasis-open.org/committees/relax-ng/compact-20021121.html">Relax
     * NG with Compact syntax</a>. This option is labelled as "experimental,unsupported" in the XJC documentation.</p>
     *
     * @see <a href="http://en.wikipedia.org/wiki/RELAX_NG">Relax NG on WikiPedia</a>
     * @see #RelaxNGCompact
     */
    relaxngcompact("relaxng-compact"),

    /**
     * <p>Treat input as <a href="http://relaxng.org/">WSDL</a>, and compile schemas inside it.
     * This option is labelled as "experimental,unsupported" in the XJC documentation.</p>
     *
     * @see <a href="http://www.w3.org/TR/wsdl">Web Services Description Language (WSDL) 1.1</a>
     */
    WSDL("wsdl"),

    /**
     * <p>Treat input as <a href="http://relaxng.org/">WSDL</a>, and compile schemas inside it.
     * This option is labelled as "experimental,unsupported" in the XJC documentation.</p>
     *
     * @see <a href="http://www.w3.org/TR/wsdl">Web Services Description Language (WSDL) 1.1</a>
     * @see #WSDL
     */
    wsdl("wsdl");

    // Internal state
    private String xjcArgument;

    SourceContentType(final String xjcArgument) {
        this.xjcArgument = xjcArgument;
    }

    /**
     * @return The XJC argument flag corresponding to this InputType.
     */
    public String getXjcArgument() {
        return xjcArgument;
    }
}
