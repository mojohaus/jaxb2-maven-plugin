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

        // First, render the JavaDoc comment.
        builder.append(renderJavaDocComment(nonNullData.getComment(), location)).append("\n");

        // Then, render each JavaDoc tag.
        for (Map.Entry<String, String> current : nonNullData.getTag2ValueMap().entrySet()) {

            final String tagXsdDoc = renderJavaDocTag(current.getKey(), current.getValue(), location);
            if (tagXsdDoc != null && !tagXsdDoc.isEmpty()) {
                builder.append(tagXsdDoc);
            }
        }

        // All done.
        return builder.toString();
    }

    /**
     * Override this method to yield another rendering of the javadoc comment.
     *
     * @param comment  The comment to render.
     * @param location the SortableLocation where the JavaDocData was harvested. Never {@code null}.
     * @return The XSD documentation for the supplied JavaDoc comment. A null or empty value will not be rendered.
     */
    protected String renderJavaDocComment(final String comment, final SortableLocation location) {
        return harmonizeNewlines(comment);
    }

    /**
     * Override this method to yield another
     *
     * @param name     The name of a JavaDoc tag.
     * @param value    The value of a JavaDoc tag.
     * @param location the SortableLocation where the JavaDocData was harvested. Never {@code null}.
     * @return The XSD documentation for the supplied JavaDoc tag.
     */
    protected String renderJavaDocTag(final String name, final String value, final SortableLocation location) {
        final String nameKey = name != null ? name.trim() : "";
        final String valueKey = value != null ? value.trim() : "";

        // All Done.
        return "(" + nameKey + "): " + harmonizeNewlines(valueKey);
    }

    /**
     * Squashes newline characters into
     *
     * @param original the original string, potentially containing newline characters.
     * @return A string where all newline characters are removed
     */
    protected String harmonizeNewlines(final String original) {

        final String toReturn = original.trim().replaceAll("[\r\n]+", "\n");
        return toReturn.endsWith("\n") ? toReturn : toReturn + "\n";
    }
}
