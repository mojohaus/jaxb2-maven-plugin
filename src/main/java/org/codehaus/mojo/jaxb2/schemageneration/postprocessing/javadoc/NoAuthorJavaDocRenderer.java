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

/**
 * <p>Default JavaDocRenderer implementation which provides a plain JavaDocData rendering, while skipping
 * output from the {@code author} tag, on the form:</p>
 * <pre>
 *     [JavaDoc comment]
 *     (tag1): [tag1 value]
 *     (tag2): [tag2 value]
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.3
 */
public class NoAuthorJavaDocRenderer extends DefaultJavaDocRenderer {

    /**
     * The author key.
     */
    private static final String AUTHOR_KEY = "author";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String renderJavaDocTag(final String name, final String value, final SortableLocation location) {

        // Don't render the author
        if(AUTHOR_KEY.equalsIgnoreCase(name)) {
            return "";
        }

        // Delegate.
        return super.renderJavaDocTag(name, value, location);
    }
}
