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

import java.util.SortedMap;
import java.util.SortedSet;

/**
 * <p>Specification for a Map of SortableLocations correlated to their respective JavaDocData.
 * To simplify searching and accessing within the JavaDocData, the paths of each SortableLocation
 * is exposed for searching and listing.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public interface SearchableDocumentation {

    /**
     * Retrieves all unique SortableLocation paths within this SearchableDocumentation.
     *
     * @return all unique SortableLocation paths within this SearchableDocumentation.
     * The result may be empty, but will never be {@code null}.
     */
    SortedSet<String> getPaths();

    /**
     * Convenience method to acquire the JavaDocData for a SortableLocation with the supplied path.
     *
     * @param path A non-null path for which the harvested JavaDocData should be retrieved.
     * @return The JavaDocData matching the SortableLocation with the supplied path, or {@code null} if no
     * SortableLocation with the supplied path was found within this SearchableDocumentation.
     */
    JavaDocData getJavaDoc(String path);

    /**
     * Convenience method to acquire the SortableLocation corresponding to the given path.
     *
     * @param path The path of a SortableLocation, which is retrieved by a call to its {@code toString()} method.
     * @param <T>  The SortableLocation subtype.
     * @return the SortableLocation corresponding to the given path, or {@code null} if this SearchableDocumentation
     * does not contain a SortableLocation with the provided path.
     */
    <T extends SortableLocation> T getLocation(String path);

    /**
     * The full map relating each SortableLocation subclass to its corresponding JavaDocData.
     *
     * @return The full map relating each SortableLocation subclass to its corresponding JavaDocData. Never null.
     */
    SortedMap<SortableLocation, JavaDocData> getAll();

    /**
     * Convenience method which retrieves a SortedMap relating all SortableLocations of a particular type
     * to their JavaDocData, respectively.
     *
     * @param type The exact type of SortableLocation which should be filtered from the result and returned in the
     *             form of a SortedMap, along with its respective JavaDocData.
     * @param <T>  The SortableLocation subtype for which all JavaDocData should be retrieved.
     * @return a SortedMap relating all SortableLocations of a particular (exact) type (i.e. any subclass types will
     * <strong>not</strong> be returned) to their JavaDocData, respectively.
     * May return empty Maps, but never {@code null}.
     */
    <T extends SortableLocation> SortedMap<T, JavaDocData> getAll(Class<T> type);
}
