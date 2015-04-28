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

import org.codehaus.mojo.jaxb2.AbstractJaxbMojo;

import java.util.Map;

/**
 * <p>Default JavaDocRenderer implementation which provides a plain JavaDocData rendering, on the form:</p>
 * <pre>
 *     [JavaDoc comment]
 *     (tag1): [tag1 value]
 *     (tag2): [tag2 value]
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public class DefaultJavaDocRenderer implements JavaDocRenderer {

    /**
     * {@inheritDoc}
     */
    @Override
    public String render(final JavaDocData nonNullData, final SortableLocation location) {

        // Compile the XSD documentation string for this Node.
        final StringBuilder builder = new StringBuilder();
        builder.append(nonNullData.getComment()).append("\n\n");
        for (Map.Entry<String, String> current : nonNullData.getTag2ValueMap().entrySet()) {

            final String tagDocumentation = "(" + current.getKey() + "): " + current.getValue() + "\n";
            builder.append(tagDocumentation);
        }

        // All done.
        return builder.toString().replace("\n", AbstractJaxbMojo.NEWLINE);
    }
}
