package org.codehaus.mojo.jaxb2;

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
 * Object containing parameters to transform a schema.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 * @since 1.4
 */
public class TransformSchema
{
    private String uri;

    private String toPrefix;

    private String toFile;

    /**
     * Default constructor.
     */
    public TransformSchema()
    {
    }

    /**
     * Compound constructor.
     *
     * @param uri      The URI of this Schema.
     * @param toPrefix The new namespace prefix for this Schema.
     * @param toFile   The new name of the generated schema file.
     */
    public TransformSchema( final String uri, final String toPrefix, final String toFile )
    {
        this.uri = uri;
        this.toPrefix = toPrefix;
        this.toFile = toFile;
    }

    /**
     * @return The URI of this Schema, such as <code>http://www.jguru.se/some/namespace</code>.
     *         The namespace URI is mapped to its prefix in the schema element, i.e:
     *         <code>xmlns:xs="http://www.w3.org/2001/XMLSchema"</code> or
     *         <code>xmlns:foo="http://www.acme.com/xml/schema/foo"</code>.
     */
    public String getUri()
    {
        return uri;
    }

    /**
     * 
     * @param uri the uri
     */
    public void setUri( String uri )
    {
        this.uri = uri;
    }
    
    /**
     * @return The namespace prefix of this Schema. Each schema element is related to its namespace using the prefix.
     *         For an XML element <code>&lt;foo:bar/&gt;</code>, the prefix is "foo" (and the element name is "bar").
     */
    public String getToPrefix()
    {
        return toPrefix;
    }
    
    /**
     * 
     * @param toPrefix the prefix
     */
    public void setToPrefix( String toPrefix )
    {
        this.toPrefix = toPrefix;
    }

    /**
     * @return the name of the target file if when renamed
     */
    public String getToFile()
    {
        return toFile;
    }
    
    /**
     * 
     * @param toFile the new filename
     */
    public void setToFile( String toFile )
    {
        this.toFile = toFile;
    }

    /** {@inheritDoc}*/
    public String toString()
    {
        return "[ uri: " + uri + " --> prefix: " + toPrefix + ", file: " + toFile + " ]";
    }
}