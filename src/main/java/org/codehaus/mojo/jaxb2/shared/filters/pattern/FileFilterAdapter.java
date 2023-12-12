package org.codehaus.mojo.jaxb2.shared.filters.pattern;

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

import org.codehaus.mojo.jaxb2.shared.Validate;
import org.codehaus.mojo.jaxb2.shared.filters.AbstractFilter;
import org.codehaus.mojo.jaxb2.shared.filters.Filter;

/**
 * Filter implementation adapting a FileFilter instance to the Filter interface.
 * Delegates the {@link #onCandidate(File)} call to the supplied {@link FileFilter} delegate.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.3
 */
public class FileFilterAdapter extends AbstractFilter<File> implements Filter<File>, FileFilter {

    // Internal state
    private FileFilter delegate;

    /**
     * Compound constructor, creating a FileFilterAdapter using the supplied {@link FileFilter} to determine if
     * candidate Files should be accepted.
     *
     * @param delegate The delegate FileFilter.
     */
    public FileFilterAdapter(final FileFilter delegate) {
        setDelegate(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInitialized() {
        return super.isInitialized() && delegate != null;
    }

    /**
     * Assigns the supplied FileFilter delegate.
     *
     * @param delegate A non-null FileFilter instance.
     */
    public void setDelegate(final FileFilter delegate) {

        // Check sanity
        Validate.notNull(delegate, "delegate");

        // Assign internal state
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean onCandidate(final File nonNullCandidate) {
        return delegate.accept(nonNullCandidate);
    }
}
