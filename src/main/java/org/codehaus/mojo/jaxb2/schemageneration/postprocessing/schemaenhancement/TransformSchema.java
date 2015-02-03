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

import org.codehaus.mojo.jaxb2.shared.Validate;

/**
 * <p>Data holder for schema transformation operations, to permit customization of the
 * schema namespace prefix and file name of generated schema. As the <code>schemagen</code>
 * tool has no mechanics to control namespace prefix and file name of generated schema,
 * the Jaxb2-Maven-plugin must supply a post-processing step to work around this situation.</p>
 * <p>Each <code>TransformSchema</code> object holds data pertaining to changes for one namespace
 * URI - either namespace prefix used within the generated XSD or resulting filename for
 * the schema definition.
 * <ol>
 * <li><strong>Namespace prefix</strong>. Each XML element within a namespace uses the supplied
 * prefix. (As a reference, in the XML element <code>&lt;foo:bar/&gt;</code>, the namespace prefix
 * is "foo" and the element name is "bar"). The Schemagen tool by default only generates namespace
 * prefixes on the form "ns1", "ns2" etc., which means that the generated schema will contain elements
 * on the form <code>&lt;xs:extension base="ns1:nazgulEntity"></code>.
 * Use a <code>toPrefix</code> element to change the namespace prefix of a particular XML URI to
 * simplify understanding the schema.</li>
 * <li><strong>Filename</strong>. By default, the Schemagen tool creates files called "schema1.xsd",
 * "schema2.xsd" etc. Since the XSD imports one another, simply changing the filename will frequently
 * break the schema structure - you will need to change all import statements in all generated XSD files
 * to match the new file names. The Jaxb2 Maven plugin can do all this housekeeping automatically, if you
 * create a transformSchema element containing a <code>toFile</code> element to change the filename for a
 * particular XML URI. Changing the file names frequently improves overview and usability of the generated schema
 * files.</li>
 * </ol>
 * </p>
 * <h2>Example TransformSchemas</h2>
 * <p>The URI element is mandatory for each TransformSchema element. The first example illustrates how
 * to use the TransformSchema element to change the prefix and file name of 3 XML URIs. This is the recommended
 * use of a TransformSchema - change both prefix and filename to something meaningful for each URI:</p>
 * <p>
 * <pre>
 * &lt;transformSchemas&gt;
 *      &lt;transformSchema&gt;
 *          &lt;uri&gt;http://some/namespace&lt;/uri&gt;
 *          &lt;toPrefix&gt;some&lt;/toPrefix&gt;
 *          &lt;toFile&gt;some_schema.xsd&lt;/toFile&gt;
 *      &lt;/transformSchema&gt;
 *      &lt;transformSchema&gt;
 *          &lt;uri&gt;http://another/namespace&lt;/uri&gt;
 *          &lt;toPrefix&gt;another&lt;/toPrefix&gt;
 *          &lt;toFile&gt;another_schema.xsd&lt;/toFile&gt;
 *      &lt;/transformSchema&gt;
 *      &lt;transformSchema&gt;
 *          &lt;uri&gt;http://yet/another/namespace&lt;/uri&gt;
 *          &lt;toPrefix&gt;yetAnother&lt;/toPrefix&gt;
 *          &lt;toFile&gt;yet_another_schema.xsd&lt;/toFile&gt;
 *      &lt;/transformSchema&gt;
 * &lt;/transformSchemas&gt;
 * </pre>
 * </p>
 * <p>The URI element is mandatory for each TransformSchema element, along with at least one of the other two
 * elements in the TransformSchema. This implies that partial configuration for TransformSchema can be used,
 * although <em>this is not recommended</em> since the readability and usability of the automatically generated
 * namespace prefixes and file names are poor. The second example illustrates how to use the TransformSchema element
 * to change either prefix or file name for 2 XML URIs:</p>
 * <p>
 * <pre>
 * &lt;transformSchemas&gt;
 *      &lt;transformSchema&gt;
 *          &lt;uri&gt;http://another/namespace&lt;/uri&gt;
 *          &lt;toPrefix&gt;another&lt;/toPrefix&gt;
 *      &lt;/transformSchema&gt;
 *      &lt;transformSchema&gt;
 *          &lt;uri&gt;http://yet/another/namespace&lt;/uri&gt;
 *          &lt;toFile&gt;yet_another_schema.xsd&lt;/toFile&gt;
 *      &lt;/transformSchema&gt;
 * &lt;/transformSchemas&gt;
 * </pre>
 * </p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 * @since 1.4
 */
public class TransformSchema {

    /**
     * The empty XML Namespace.
     */
    public static final String EMPTY_NAMESPACE = "";

    // Internal state
    private String uri = EMPTY_NAMESPACE;
    private String toPrefix;
    private String toFile;

    /**
     * Default constructor.
     */
    public TransformSchema() {
    }

    /**
     * Compound constructor, creating a TransformSchema instruction wrapping the supplied data.
     *
     * @param uri      The URI of this Schema, such as
     *                 <code>http://www.jguru.se/some/namespace</code>. Cannot be null or empty.
     * @param toPrefix The new namespace prefix for this Schema. Optional.
     * @param toFile   The new name of the generated schema file.
     */
    public TransformSchema(final String uri, final String toPrefix, final String toFile) {
        this.uri = uri;
        this.toPrefix = toPrefix;
        this.toFile = toFile;
    }

    /**
     * @return The URI of this Schema, such as <code>http://www.jguru.se/some/namespace</code>.
     * The namespace URI is mapped to its prefix in the schema element, i.e:
     * <code>xmlns:xs="http://www.w3.org/2001/XMLSchema"</code> or
     * <code>xmlns:foo="http://www.acme.com/xml/schema/foo"</code>.
     */
    public String getUri() {
        return uri;
    }

    /**
     * @return The namespace prefix of this Schema. Each schema element is related to its namespace using the prefix.
     * For an XML element <code>&lt;foo:bar/&gt;</code>, the prefix is "foo" (and the element name is "bar").
     */
    public String getToPrefix() {
        return toPrefix;
    }

    /**
     * @return the name of the target file if/when renamed.
     */
    public String getToFile() {
        return toFile;
    }

    /**
     * Assigns the URI of this Schema, such as <code>http://www.jguru.se/some/namespace</code>.
     * The namespace URI is mapped to its prefix in the schema element, i.e:
     * <code>xmlns:xs="http://www.w3.org/2001/XMLSchema"</code> or
     * <code>xmlns:foo="http://www.acme.com/xml/schema/foo"</code>.
     *
     * @param uri The non-empty uri of this Schema.
     */
    public void setUri(final String uri) {

        // Check sanity
        Validate.notEmpty(uri, "uri");

        // Assign internal state
        this.uri = uri;
    }

    /**
     * Assigns the namespace prefix of this Schema. Each schema element is related to its namespace
     * using the prefix. For an XML element <code>&lt;foo:bar/&gt;</code>, the prefix is "foo"
     * (and the element name is "bar").
     *
     * @param toPrefix The non-empty prefix to assign.
     */
    public void setToPrefix(final String toPrefix) {

        // Check sanity
        Validate.notEmpty(toPrefix, "toPrefix");

        // Assign internal state
        this.toPrefix = toPrefix;
    }

    /**
     * Assigns the the name of the target file if/when renamed.
     *
     * @param toFile The non-empty filename to assign.
     */
    public void setToFile(final String toFile) {

        // Check sanity
        Validate.notEmpty(toFile, "toFile");

        // Assign internal state
        this.toFile = toFile;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "[ uri: " + uri + " --> prefix: " + toPrefix + ", file: " + toFile + " ]";
    }
}