package org.codehaus.mojo.jaxb2.shared.filters;

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

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.jaxb2.shared.Validate;

/**
 * <p>Algorithm definitions for common operations using Filters.</p>
 * <p><strong>SPI Note:</strong>This class/these methods should be replaced
 * with stream operations when JDK8 is required to build/run this plugin.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see org.codehaus.mojo.jaxb2.shared.filters.Filter
 * @since 2.0
 */
@SuppressWarnings("all")
public final class Filters {

    /**
     * Algorithms for accepting the supplied object if at least one of the supplied Filters accepts it.
     *
     * @param object  The object to accept (or not).
     * @param filters The non-null list of Filters to examine the supplied object.
     * @param <T>     The Filter type.
     * @return {@code true} if at least one of the filters return true from its accept method.
     * @see Filter#accept(Object)
     */
    public static <T> boolean matchAtLeastOnce(final T object, final List<Filter<T>> filters) {

        // Check sanity
        Validate.notNull(filters, "filters");

        boolean acceptedByAtLeastOneFilter = false;
        for (Filter<T> current : filters) {
            if (current.accept(object)) {
                acceptedByAtLeastOneFilter = true;
                break;
            }
        }

        // All done.
        return acceptedByAtLeastOneFilter;
    }

    /**
     * Algorithms for rejecting the supplied object if at least one of the supplied Filters does not accept it.
     *
     * @param object  The object to reject (or not).
     * @param filters The non-null list of Filters to examine the supplied object.
     * @param <T>     The Filter type.
     * @return {@code true} if at least one of the filters returns false from its accept method.
     * @see Filter#accept(Object)
     */
    public static <T> boolean rejectAtLeastOnce(final T object, final List<Filter<T>> filters) {

        // Check sanity
        Validate.notNull(filters, "filters");

        boolean rejectedByAtLeastOneFilter = false;
        for (Filter<T> current : filters) {
            if (!current.accept(object)) {
                rejectedByAtLeastOneFilter = true;
                break;
            }
        }

        // All done.
        return rejectedByAtLeastOneFilter;
    }

    /**
     * Algorithms for rejecting the supplied object if at least one of the supplied Filters rejects it.
     *
     * @param object  The object to accept (or not).
     * @param filters The non-null list of Filters to examine the supplied object.
     * @param <T>     The Filter type.
     * @return {@code true} if at least one of the filters return false from its accept method.
     * @see Filter#accept(Object)
     */
    public static <T> boolean noFilterMatches(final T object, final List<Filter<T>> filters) {

        // Check sanity
        Validate.notNull(filters, "filters");

        boolean matchedAtLeastOnce = false;
        for (Filter<T> current : filters) {
            if (current.accept(object)) {
                matchedAtLeastOnce = true;
            }
        }

        // All done.
        return !matchedAtLeastOnce;
    }

    /**
     * Adapts the Filter specification to the FileFilter interface, to enable immediate use
     * for filtering File lists.
     *
     * @param toAdapt The non-null Filter which should be adapted to a FileFilter interface.
     * @return If the {@code toAdapt} instance already implements the FileFilter interface, simply return the toAdapt
     * instance. Otherwise, returns a FileFilter interface which delegates its execution to the wrapped Filter.
     */
    public static FileFilter adapt(final Filter<File> toAdapt) {

        // Check sanity
        Validate.notNull(toAdapt, "toAdapt");

        // Already a FileFilter?
        if (toAdapt instanceof FileFilter) {
            return (FileFilter) toAdapt;
        }

        // Wrap and return.
        return new FileFilter() {
            @Override
            public boolean accept(final File candidate) {
                return toAdapt.accept(candidate);
            }
        };
    }

    /**
     * Adapts the supplied List of Filter specifications to a List of FileFilters.
     *
     * @param toAdapt The List of Filters to adapts.
     * @return A List holding FileFilter instances. If {@code toAdapt} is {@code null} or empty, an empty list is
     * returned from this method. Thus, this method will never return a {@code null} value.
     */
    public static List<FileFilter> adapt(final List<Filter<File>> toAdapt) {

        final List<FileFilter> toReturn = new ArrayList<FileFilter>();
        if (toAdapt != null) {
            for (Filter<File> current : toAdapt) {
                toReturn.add(adapt(current));
            }
        }

        // All done.
        return toReturn;
    }

    /**
     * Initializes the supplied Filters by assigning the given Log.
     *
     * @param log     The active Maven Log.
     * @param filters The List of Filters to initialize.
     * @param <T>     The Filter type.
     */
    public static <T> void initialize(final Log log, final List<Filter<T>> filters) {

        // Check sanity
        Validate.notNull(log, "log");
        Validate.notNull(filters, "filters");

        for (Filter<T> current : filters) {
            current.initialize(log);
        }
    }

    /**
     * Initializes the supplied Filters by assigning the given Log.
     *
     * @param log     The active Maven Log.
     * @param filters The List of Filters to initialize.
     * @param <T>     The Filter type.
     */
    public static <T> void initialize(final Log log, final Filter<T>... filters) {

        // Check sanity
        Validate.notNull(log, "log");

        if (filters != null) {
            for (Filter<T> current : filters) {
                current.initialize(log);
            }
        }
    }
}
