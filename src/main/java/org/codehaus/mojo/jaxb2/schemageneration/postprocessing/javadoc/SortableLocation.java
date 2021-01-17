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

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Common specification for a JavaDoc location which can be compared and sorted.
 * JavaDoc locations must be comparable and also convert-able to unique strings.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public interface SortableLocation extends Comparable<SortableLocation> {

    /**
     * Validates if the supplied path is equal to this SortableLocation.
     *
     * @param path The non-null path to compare to this SortableLocation.
     * @return {@code true} if this SortableLocation is equal to the supplied path.
     */
    boolean isEqualToPath(final String path);

    /**
     * Retrieves the path of this SortableLocation. The path must uniquely correspond to each unique SortableLocation,
     * implying that SortableLocations could be sorted and compared for equality using the path property.
     *
     * @return the path of this SortableLocation. Never null.
     */
    String getPath();

    /**
     * Retrieves the value of the name attribute provided by a JAXB annotation, implying that
     * the XSD type should use another name than the default.
     *
     * @return the value of the name attribute provided by a JAXB annotation relevant to this {@link SortableLocation}.
     * @see XmlElement#name()
     * @see XmlAttribute#name()
     * @see XmlType#name()
     */
    String getAnnotationRenamedTo();
}
