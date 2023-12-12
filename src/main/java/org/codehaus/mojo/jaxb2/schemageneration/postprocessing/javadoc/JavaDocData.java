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

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.thoughtworks.qdox.model.DocletTag;
import org.codehaus.mojo.jaxb2.AbstractJaxbMojo;

/**
 * Simplified structure containing comments and tags read from a JavaDoc comment block.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public class JavaDocData {

    /**
     * Substitution value for when no JavaDoc comment text was found within a JavaDoc comment block.
     */
    public static final String NO_COMMENT = "";

    // Internal state
    private String comment;
    private SortedMap<String, String> tag2ValueMap;

    /**
     * Creates a JavaDocData for a particular entry with the supplied JavaDoc comment and List of DocletTags.
     *
     * @param comment The actual comment in the JavaDoc. Null values are replaced with the value {@code NO_COMMENT},
     *                to ensure that the {@code getComment() } method does not return null values.
     * @param tags    The DocletTags of the JavaDoc entry. Can be null or empty.
     */
    public JavaDocData(final String comment, final List<DocletTag> tags) {

        // Assign internal state
        this.comment = comment == null ? NO_COMMENT : comment;
        this.tag2ValueMap = new TreeMap<String, String>();

        // Parse, and assign internal state
        for (DocletTag current : tags) {

            final String tagName = current.getName();
            String tagValue = current.getValue();

            // Handle the case of multi-valued tags, such as
            //
            // @author SomeAuthor
            // @author AnotherAuthor
            //
            // which becomes [author] --> [SomeAuthor, AnotherAuthor]
            String currentValue = tag2ValueMap.get(tagName);
            if (currentValue != null) {
                tagValue = currentValue + ", " + current.getValue();
            }

            tag2ValueMap.put(tagName, tagValue);
        }
    }

    /**
     * Retrieves the comment/text in the JavaDoc structure, minus the names and values of any given JavaDoc tags.
     *
     * @return the comment/text in the JavaDoc structure, or {@code NO_COMMENT} if no JavaDoc was provided.
     * Never returns a {@code null} value.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Retrieves the names and values of all JavaDoc tags found.
     * If two tags were found (such as two {@code @author} tags, the
     * value contains all found
     *
     * @return A non-null Map relating the names of all supplied JavaDoc Tags to their value(s).
     */
    public SortedMap<String, String> getTag2ValueMap() {
        return tag2ValueMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        StringBuilder toReturn = new StringBuilder();

        toReturn.append("\n+=================\n");
        toReturn.append("| Comment: ").append(comment).append("\n");

        if (tag2ValueMap.size() == 0) {
            toReturn.append("| No JavaDoc tags.\n");
        } else {

            toReturn.append("| ").append(tag2ValueMap.size()).append(" JavaDoc tags ...\n");
            for (Map.Entry<String, String> current : tag2ValueMap.entrySet()) {
                toReturn.append("| ")
                        .append(current.getKey())
                        .append(": ")
                        .append(current.getValue())
                        .append("\n");
            }
        }
        toReturn.append("+=================\n\n");
        return toReturn.toString().replace("\n", AbstractJaxbMojo.NEWLINE);
    }
}
